package services;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import entity.FittleFileConfigEntity;
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

		File sourceFile = null;

		String sourceProtocol = fileConfig.getSourceServerProtocol();
		String sourceHostName = fileConfig.getSourceServerHostName();
		String sourcePath = fileConfig.getSourceFilePath();

		sourceFile = getSourceFile(sourceProtocol, sourceHostName, sourcePath);

		/*
		 * 2. Send the File to their TargetPath using targetProtocol and targetHostName
		 */

		String targetProtocol = fileConfig.getTargetServerProtocol();
		String targetHostName = fileConfig.getTargetServerHostName();
		String targetPath = fileConfig.getTargetFilePath();

		if (sourceFile != null)
			sendSourceFile(sourceFile, targetProtocol, targetHostName, targetPath);
		else
			logger.error("Source File is Null");

	}

	private void sendSourceFile(File sourceFile, String targetProtocol, String targetHostName, String targetPath) throws IOException {
		if (targetProtocol != null && targetHostName != null && targetPath != null) {

			if (targetProtocol.equals(TransferFilesConstant.S3_PROTOCOL))
				s3Operations.sendToS3(sourceFile, targetHostName, targetPath);
			if (targetProtocol.equals(TransferFilesConstant.SFTP_PROTOCOL))
				sftpOperations.sendToSftp(sourceFile, targetHostName, targetPath);
			if (targetProtocol.equals(TransferFilesConstant.FTPS_PROTOCOL))
				ftpsOperations.sendToFtps(sourceFile, targetHostName, targetPath);
		} else
			logger.error("One of the Target Config value is Null ");

	}

	private File getSourceFile(String sourceProtocol, String sourceHostName, String sourcePath) throws IOException {

		File sourceFile = null;

		if (sourceProtocol != null && sourceHostName != null && sourcePath != null) {

			if (sourceProtocol.equals(TransferFilesConstant.S3_PROTOCOL))
				sourceFile = s3Operations.getS3SourceFile(sourceHostName, sourcePath);
			if (sourceProtocol.equals(TransferFilesConstant.SFTP_PROTOCOL))
				sourceFile = sftpOperations.getSftpSourceFile(sourceHostName, sourcePath);
			if (sourceProtocol.equals(TransferFilesConstant.FTPS_PROTOCOL))
				sourceFile = ftpsOperations.getFtpsSourceFile(sourceHostName, sourcePath);

			return sourceFile;

		} else {

			logger.error("One of the Source Config value is Null");
			return null;
		}

	}

}
