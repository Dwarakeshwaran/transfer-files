package services;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.AmazonS3;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;

import config.FTPServerConfig;
import config.S3Config;
import config.SFTPServerConfig;
import entity.FittleFileAuditHistoryEntity;
import entity.FittleFileConfigEntity;
import model.FileInfo;
import utils.TransferFilesConstant;

public class TransferFileService {

	private static final Logger logger = LoggerFactory.getLogger(TransferFileService.class);

	private static S3Operations s3Operations = new S3Operations();
	private static SFTPOperations sftpOperations = new SFTPOperations();
	private static FTPSOperations ftpsOperations = new FTPSOperations();

	private static SFTPServerConfig sftpConfig = new SFTPServerConfig();
	private static FTPServerConfig ftpConfig = new FTPServerConfig();

	private AmazonS3 s3Client = null;

	private ChannelSftp sourceSftpChannel = null;
	private ChannelSftp targetSftpChannel = null;

	private FTPClient ftpClient = null;

	public void transferFiles(FittleFileConfigEntity fileConfig, String jobId, EntityManager entityManager)
			throws IOException {

		/*
		 * 1. Get Files from the sourceProtocol by using sourceHostName and sourcePath
		 */

		List<FileInfo> sourceFilesList = null;

		sourceFilesList = getSourceFiles(fileConfig);

		if (sourceFilesList != null) {
			for (FileInfo file : sourceFilesList) {

				logger.info("File Info {}", file);
				file.setJobId(jobId);

			}
		}

		/*
		 * 2. Send the Files to their TargetPath using targetProtocol and targetHostName
		 */

		if (sourceFilesList != null) {
			if (!sourceFilesList.isEmpty()) {
				sendSourceFiles(fileConfig, sourceFilesList);

				/*
				 * 3. Archive files in the Archival location
				 */
				archiveFiles(fileConfig, sourceFilesList);

				/*
				 * 4. If Archival Done successfully, delete the files from the source location
				 */
				String deleteAfterSuccess = fileConfig.getDeleteAfterSuccess();

				if (deleteAfterSuccess.equals("Y"))
					deleteSourceFiles(fileConfig, sourceFilesList);

				/*
				 * 5. Delete files in the Lambda's /tmp/ folder to free up Lamda's memory and
				 * Close the server
				 */

				cleanTempFolder(TransferFilesConstant.TEMP_FOLDER_PATH);

				disconnectSessions(sourceSftpChannel, targetSftpChannel, ftpClient);

				for (FileInfo file : sourceFilesList)
					logger.info("File Info {}", file);

				storeDataInAuditTable(sourceFilesList, entityManager);

			} else
				logger.error("Source File List is Empty");
		} else
			logger.error("Source File is Null");

	}

	private void disconnectSessions(ChannelSftp sourceSftpChannel, ChannelSftp targetSftpChannel, FTPClient ftpClient) {
		if (sourceSftpChannel != null) {
			try {
				sourceSftpChannel.disconnect();
				sourceSftpChannel.getSession().disconnect();

			} catch (JSchException e) {

				logger.error("Error occurred while disconnecting sourceSftpChannel {}", e.getMessage());

				e.printStackTrace();
			}
		}
		if (targetSftpChannel != null) {
			try {

				targetSftpChannel.disconnect();
				targetSftpChannel.getSession().disconnect();

			} catch (JSchException e) {

				logger.error("Error occurred while disconnecting targetSftpChannel {}", e.getMessage());

				e.printStackTrace();
			}
		}
		if (ftpClient != null)
			try {
				ftpClient.abort();
			} catch (IOException e) {
				logger.error("Error occurred while disconnecting ftpClient {}", e.getMessage());
				e.printStackTrace();
			}

	}

	private void storeDataInAuditTable(List<FileInfo> sourceFilesList, EntityManager entityManager) {

		FittleFileAuditHistoryEntity auditHistory = null;

		try {

			for (FileInfo file : sourceFilesList) {

				auditHistory = new FittleFileAuditHistoryEntity();

				entityManager.getTransaction().begin();

				auditHistory.setFileJobId(file.getJobId());
				auditHistory.setFileName(file.getFileName());
				auditHistory.setFileTransferStatus(file.getFileTransferStatus());
				auditHistory.setProcessingStartTimestamp(file.getProcessingStartTimestamp());
				auditHistory.setProcessingEndTimestamp(file.getProcessingEndTimestamp());
				auditHistory.setSourceFileArchivalStatus(file.getSourceFileArchivalStatus());
				auditHistory.setSourceFileDeletionStatus(file.getSourceFileDeletionStatus());

				entityManager.merge(auditHistory);
				entityManager.getTransaction().commit();

			}

		} catch (Exception e) {
			logger.info("Error occurred while saving values in audit history table {}", e.getMessage());
		}

	}

	private List<FileInfo> getSourceFiles(FittleFileConfigEntity fileConfig) throws IOException {

		String sourceProtocol = fileConfig.getSourceServerProtocol();
		String sourceSystem = fileConfig.getSourceServerSystem();
		String sourceHostName = fileConfig.getSourceServerHostName();
		String sourcePath = fileConfig.getSourceFilePath();
		String fileExtension = fileConfig.getFileExtension();

		logger.info("Inside getSourceFiles method {}:{}/{}", sourceProtocol, sourceHostName, sourcePath);

		List<FileInfo> sourceFilesList = null;

		try {

			if (sourceProtocol != null && sourceHostName != null && sourcePath != null) {

				if (sourceProtocol.equals(TransferFilesConstant.S3_PROTOCOL)) {

					s3Client = new S3Config().getS3Config(sourceSystem);

					sourceFilesList = s3Operations.getS3SourceFileList(s3Client, sourceHostName, sourcePath,
							fileExtension);

				} else if (sourceProtocol.equals(TransferFilesConstant.SFTP_PROTOCOL)) {

					sourceSftpChannel = sftpConfig.getSSHConnection(new JSch(), sourceSystem, sourceHostName);

					sourceFilesList = sftpOperations.getSftpSourceFileList(sourceSftpChannel, sourcePath,
							fileExtension);

				} else if (sourceProtocol.equals(TransferFilesConstant.FTPS_PROTOCOL)) {

					ftpClient = ftpConfig.getConnection(sourceSystem, sourceHostName);
					sourceFilesList = ftpsOperations.getFtpsSourceFileList(ftpClient, sourcePath);
				}

				return sourceFilesList;

			} else {

				logger.error("One of the Source Config value is Null");
				return Collections.emptyList();
			}

		} catch (Exception exception) {
			logger.error("Error occurred while connecting to {} {}", sourceProtocol, exception.getMessage());
			return Collections.emptyList();
		}

	}

	private boolean sendSourceFiles(FittleFileConfigEntity fileConfig, List<FileInfo> sourceFilesList)
			throws IOException {

		String targetProtocol = fileConfig.getTargetServerProtocol();
		String targetSystem = fileConfig.getTargetServerSystem();
		String targetHostName = fileConfig.getTargetServerHostName();
		String targetPath = fileConfig.getTargetFilePath();

		logger.info("Inside sendSourceFiles method {}:{}/{}", targetProtocol, targetHostName, targetPath);

		boolean sentStatus = false;

		try {

			if (targetProtocol != null && targetHostName != null && targetPath != null) {

				if (targetProtocol.equals(TransferFilesConstant.S3_PROTOCOL)) {

					s3Client = new S3Config().getS3Config(targetSystem);
					sentStatus = s3Operations.sendToS3(sourceFilesList, s3Client, targetHostName, targetPath);

				} else if (targetProtocol.equals(TransferFilesConstant.SFTP_PROTOCOL)) {

					targetSftpChannel = sftpConfig.getSSHConnection(new JSch(), targetSystem, targetHostName);

					sentStatus = sftpOperations.sendToSftp(targetSftpChannel, sourceFilesList, targetPath);

				} else if (targetProtocol.equals(TransferFilesConstant.FTPS_PROTOCOL)) {

					ftpClient = ftpConfig.getConnection(targetSystem, targetHostName);
					sentStatus = ftpsOperations.sendToFtps(sourceFilesList, ftpClient, targetPath);

				}

			} else
				logger.error("One of the Target Config value is Null ");

		} catch (Exception exception) {
			logger.error("Error occurred while connecting to {} {}", targetProtocol, exception.getMessage());

		}

		logger.info("Sent Status {}", sentStatus);

		return sentStatus;

	}

	private void archiveFiles(FittleFileConfigEntity fileConfig, List<FileInfo> sourceFilesList) {

		String sourceArchivalPath = fileConfig.getSourceArchivalPath();
		String sourceProtocol = fileConfig.getSourceServerProtocol();
		String sourceHostName = fileConfig.getSourceServerHostName();

		logger.info("Inside archiveFiles method {}:{}/{}", sourceProtocol, sourceHostName, sourceArchivalPath);

		boolean archivalStatus = false;

		if (sourceProtocol.equals(TransferFilesConstant.S3_PROTOCOL))
			archivalStatus = s3Operations.archiveS3Files(sourceFilesList, s3Client, sourceHostName, sourceArchivalPath);
		if (sourceProtocol.equals(TransferFilesConstant.SFTP_PROTOCOL))
			archivalStatus = sftpOperations.archiveSftpFiles(sourceSftpChannel, sourceFilesList, sourceArchivalPath);
		if (sourceProtocol.equals(TransferFilesConstant.FTPS_PROTOCOL))
			archivalStatus = ftpsOperations.archiveFtpsFiles(sourceFilesList, ftpClient, sourceArchivalPath);

		logger.info("Archival Status {}", archivalStatus);

	}

	private void deleteSourceFiles(FittleFileConfigEntity fileConfig, List<FileInfo> sourceFilesList) {

		String sourceProtocol = fileConfig.getSourceServerProtocol();
		String sourceHostName = fileConfig.getSourceServerHostName();
		String sourcePath = fileConfig.getSourceFilePath();

		logger.info("Inside deleteSourceFiles method {}:{}/{}", sourceProtocol, sourceHostName, sourcePath);

		if (sourceProtocol.equals(TransferFilesConstant.S3_PROTOCOL))
			s3Operations.deleteS3Files(s3Client, sourceHostName, sourcePath, sourceFilesList);
		if (sourceProtocol.equals(TransferFilesConstant.SFTP_PROTOCOL))
			sftpOperations.deleteSftpFiles(sourceSftpChannel, sourcePath, sourceFilesList);
		if (sourceProtocol.equals(TransferFilesConstant.FTPS_PROTOCOL))
			ftpsOperations.deleteFtpsFiles(ftpClient, sourcePath);

	}

	private void cleanTempFolder(String tempFolderPath) {

		File tempDirectory = new File(tempFolderPath);
		try {
			FileUtils.cleanDirectory(tempDirectory);
			logger.info("All the files inside Lambda's temp folder has been deleted Successfully");
		} catch (IOException e) {
			logger.error("Error occurred while deleting the content in Temp Folder.");
		}

	}

}
