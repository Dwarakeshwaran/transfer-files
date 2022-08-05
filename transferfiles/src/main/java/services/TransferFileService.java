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

		String sourceProtocol = fileConfig.getSourceServerProtocol();
		String sourceCredentials = fileConfig.getSourceServerCredentials();
		String sourceHostName = fileConfig.getSourceServerHostName();
		String sourcePath = fileConfig.getSourceFilePath();
		String fileExtension = fileConfig.getFileExtension();

		sourceFilesList = getSourceFile(sourceProtocol, sourceCredentials, sourceHostName, sourcePath, fileExtension);

		for (FileInfo file : sourceFilesList)
			logger.info("File Info {}", file);

		/*
		 * 2. Send the File to their TargetPath using targetProtocol and targetHostName
		 */

		String targetProtocol = fileConfig.getTargetServerProtocol();
		String targetCredentials = fileConfig.getTargetServerCredentials();
		String targetHostName = fileConfig.getTargetServerHostName();
		String targetPath = fileConfig.getTargetFilePath();

		if (sourceFilesList != null)
			sendSourceFile(sourceFilesList, targetProtocol, targetCredentials, targetHostName, targetPath);
		else
			logger.error("Source File is Null");

		cleanTempFolder(TransferFilesConstant.TEMP_FOLDER_PATH);

	}

	private void cleanTempFolder(String tempFolderPath) {

		File tempDirectory = new File(tempFolderPath);
		try {
			FileUtils.cleanDirectory(tempDirectory);
		} catch (IOException e) {
			logger.error("Error occurred while deleting the content in Temp Folder.");
		}

	}

	private void sendSourceFile(List<FileInfo> sourceFileList, String targetProtocol, String targetCredentials,
			String targetHostName, String targetPath) throws IOException {
		if (targetProtocol != null && targetHostName != null && targetPath != null) {

			if (targetProtocol.equals(TransferFilesConstant.S3_PROTOCOL))
				s3Operations.sendToS3(sourceFileList, targetCredentials, targetHostName, targetPath);
			if (targetProtocol.equals(TransferFilesConstant.SFTP_PROTOCOL))
				sftpOperations.sendToSftp(sourceFileList, targetCredentials, targetHostName, targetPath);
			if (targetProtocol.equals(TransferFilesConstant.FTPS_PROTOCOL))
				ftpsOperations.sendToFtps(sourceFileList, targetCredentials, targetHostName, targetPath);
		} else
			logger.error("One of the Target Config value is Null ");

	}

	private List<FileInfo> getSourceFile(String sourceProtocol, String sourceCredentials, String sourceHostName,
			String sourcePath, String fileExtension) throws IOException {

		List<FileInfo> sourceFilesList = null;

		if (sourceProtocol != null && sourceHostName != null && sourcePath != null) {

			if (sourceProtocol.equals(TransferFilesConstant.S3_PROTOCOL))
				sourceFilesList = s3Operations.getS3SourceFile(sourceCredentials, sourceHostName, sourcePath,
						fileExtension);
			if (sourceProtocol.equals(TransferFilesConstant.SFTP_PROTOCOL))
				sourceFilesList = sftpOperations.getSftpSourceFile(sourceCredentials, sourceHostName, sourcePath,
						fileExtension);
			if (sourceProtocol.equals(TransferFilesConstant.FTPS_PROTOCOL))
				sourceFilesList = ftpsOperations.getFtpsSourceFile(sourceCredentials, sourceHostName, sourcePath);

			return sourceFilesList;

		} else {

			logger.error("One of the Source Config value is Null");
			return Collections.emptyList();
		}

	}

}
