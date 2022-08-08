package services;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import entity.FittleFileConfigEntity;
import model.FileInfo;
import utils.TransferFilesConstant;

public class TransferFileService {

	private static final Logger logger = LoggerFactory.getLogger(TransferFileService.class);

	private static S3Operations s3Operations = new S3Operations();
	private static SFTPOperations sftpOperations = new SFTPOperations();
	private static FTPSOperations ftpsOperations = new FTPSOperations();

	public void transferFiles(FittleFileConfigEntity fileConfig) throws IOException {

		/*
		 * 1. Get File from the sourceProtocol by using sourceHostName and sourcePath
		 */

		List<FileInfo> sourceFilesList = null;

		sourceFilesList = getSourceFiles(fileConfig);

		for (FileInfo file : sourceFilesList)
			logger.info("File Info {}", file);

		/*
		 * 2. Send the File to their TargetPath using targetProtocol and targetHostName
		 */

		if (sourceFilesList != null)
			sendSourceFiles(fileConfig, sourceFilesList);
		else
			logger.error("Source File is Null");

		/*
		 * 3. Archive files in the Archival location
		 */

		boolean archivalStatus = archiveFiles(fileConfig, sourceFilesList);

		/*
		 * 4. If Archival Done successfully, delete the files from the source location
		 */
		String deleteAfterSuccess = fileConfig.getDeleteAfterSuccess();

		if (archivalStatus && deleteAfterSuccess.equals("Y"))
			deleteSourceFiles(fileConfig);

		/*
		 * 5. Delete files in the Lambda's /tmp/ folder to free up Lamda's memory
		 */

		cleanTempFolder(TransferFilesConstant.TEMP_FOLDER_PATH);

	}

	private List<FileInfo> getSourceFiles(FittleFileConfigEntity fileConfig) throws IOException {

		String sourceProtocol = fileConfig.getSourceServerProtocol();
		String sourceCredentials = fileConfig.getSourceServerCredentials();
		String sourceHostName = fileConfig.getSourceServerHostName();
		String sourcePath = fileConfig.getSourceFilePath();
		String fileExtension = fileConfig.getFileExtension();

		List<FileInfo> sourceFilesList = null;

		if (sourceProtocol != null && sourceHostName != null && sourcePath != null) {

			if (sourceProtocol.equals(TransferFilesConstant.S3_PROTOCOL))
				sourceFilesList = s3Operations.getS3SourceFileList(sourceCredentials, sourceHostName, sourcePath,
						fileExtension);
			if (sourceProtocol.equals(TransferFilesConstant.SFTP_PROTOCOL))
				sourceFilesList = sftpOperations.getSftpSourceFileList(sourceCredentials, sourceHostName, sourcePath,
						fileExtension);
			if (sourceProtocol.equals(TransferFilesConstant.FTPS_PROTOCOL))
				sourceFilesList = ftpsOperations.getFtpsSourceFileList(sourceCredentials, sourceHostName, sourcePath);

			return sourceFilesList;

		} else {

			logger.error("One of the Source Config value is Null");
			return Collections.emptyList();
		}

	}

	private void sendSourceFiles(FittleFileConfigEntity fileConfig, List<FileInfo> sourceFilesList) throws IOException {

		String targetProtocol = fileConfig.getTargetServerProtocol();
		String targetCredentials = fileConfig.getTargetServerCredentials();
		String targetHostName = fileConfig.getTargetServerHostName();
		String targetPath = fileConfig.getTargetFilePath();

		if (targetProtocol != null && targetHostName != null && targetPath != null) {

			if (targetProtocol.equals(TransferFilesConstant.S3_PROTOCOL))
				s3Operations.sendToS3(sourceFilesList, targetCredentials, targetHostName, targetPath);
			if (targetProtocol.equals(TransferFilesConstant.SFTP_PROTOCOL))
				sftpOperations.sendToSftp(sourceFilesList, targetCredentials, targetHostName, targetPath);
			if (targetProtocol.equals(TransferFilesConstant.FTPS_PROTOCOL))
				ftpsOperations.sendToFtps(sourceFilesList, targetCredentials, targetHostName, targetPath);
		} else
			logger.error("One of the Target Config value is Null ");

	}

	private boolean archiveFiles(FittleFileConfigEntity fileConfig, List<FileInfo> sourceFilesList) throws IOException {

		String sourceArchivalPath = fileConfig.getSourceArchivalPath();
		String sourceProtocol = fileConfig.getSourceServerProtocol();
		String sourceCredentials = fileConfig.getSourceServerCredentials();
		String sourceHostName = fileConfig.getSourceServerHostName();

		boolean archivalStatus = false;

		if (sourceProtocol.equals(TransferFilesConstant.S3_PROTOCOL))
			archivalStatus = s3Operations.sendToS3(sourceFilesList, sourceCredentials, sourceHostName,
					sourceArchivalPath);
		if (sourceProtocol.equals(TransferFilesConstant.SFTP_PROTOCOL))
			archivalStatus = sftpOperations.sendToSftp(sourceFilesList, sourceCredentials, sourceHostName,
					sourceArchivalPath);
		if (sourceProtocol.equals(TransferFilesConstant.FTPS_PROTOCOL))
			archivalStatus = ftpsOperations.sendToFtps(sourceFilesList, sourceCredentials, sourceHostName,
					sourceArchivalPath);

		return archivalStatus;
	}

	private void deleteSourceFiles(FittleFileConfigEntity fileConfig) {

		String sourceArchivalPath = fileConfig.getSourceArchivalPath();
		String sourceProtocol = fileConfig.getSourceServerProtocol();
		String sourceCredentials = fileConfig.getSourceServerCredentials();
		String sourceHostName = fileConfig.getSourceServerHostName();

		if (sourceProtocol.equals(TransferFilesConstant.S3_PROTOCOL))
			s3Operations.deleteS3Files(sourceCredentials, sourceHostName, sourceArchivalPath);
		if (sourceProtocol.equals(TransferFilesConstant.SFTP_PROTOCOL))
			sftpOperations.deleteSftpFiles(sourceCredentials, sourceHostName, sourceArchivalPath);
		if (sourceProtocol.equals(TransferFilesConstant.FTPS_PROTOCOL))
			ftpsOperations.deleteFtpsFiles(sourceCredentials, sourceHostName, sourceArchivalPath);

	}

	private void cleanTempFolder(String tempFolderPath) {

		File tempDirectory = new File(tempFolderPath);
		try {
			FileUtils.cleanDirectory(tempDirectory);
		} catch (IOException e) {
			logger.error("Error occurred while deleting the content in Temp Folder.");
		}

	}

}
