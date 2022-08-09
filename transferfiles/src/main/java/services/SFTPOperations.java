package services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.FileInfo;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPEngine;
import net.schmizz.sshj.sftp.SFTPFileTransfer;
import utils.TransferFilesConstant;

public class SFTPOperations {

	private static final Logger logger = LoggerFactory.getLogger(SFTPOperations.class);

	public List<FileInfo> getSftpSourceFileList(SFTPFileTransfer fileTransfer, String sftpPath, String fileExtension)
			throws IOException {

		logger.debug("Values {} {}", fileTransfer, sftpPath);

		List<FileInfo> fileList = new ArrayList<>();

		logger.info("All files from {} have been downloaded to the /tmp/ folder of Lambda", sftpPath);

		/*
		 * 1. Download all the files from SFTP sftp Path to Lambda's /tmp/ Folder
		 */

		fileTransfer.download(sftpPath, TransferFilesConstant.TEMP_FOLDER_PATH);

		/*
		 * 2. Read the file names from the /tmp/ folder in Lambda and get all the files
		 * in a File Object and store it in FileInfo list object.
		 */

		try (Stream<Path> paths = Files.walk(Paths.get(TransferFilesConstant.TEMP_FOLDER_PATH + sftpPath))) {

			paths.filter(Files::isRegularFile).forEach(filePath -> {

				if (filePath.getFileName().toString().contains(fileExtension)) {

					FileInfo fileInfo = new FileInfo();

					try {
						BasicFileAttributes fileAttributes = Files.readAttributes(filePath, BasicFileAttributes.class);

						String fileName = filePath.getFileName().toString();
						fileTransfer.download(sftpPath + fileName, TransferFilesConstant.TEMP_FOLDER_PATH);
						logger.info("File {} downloaded to {}", sftpPath + fileName,
								TransferFilesConstant.TEMP_FOLDER_PATH);

						fileInfo.setFileName(fileName);
						fileInfo.setProcessingStartTimestamp(new Timestamp(System.currentTimeMillis()));
						fileInfo.setModifiedDate(getDate(fileAttributes));
						fileInfo.setFile(new File(TransferFilesConstant.TEMP_FOLDER_PATH + fileName));
						logger.info("File Info {}", fileInfo);

						fileList.add(fileInfo);

					} catch (IOException ioException) {

						logger.error(
								"Error occurred while downloading files from SFTP Server to Lamda's /tmp/ folder Exception {}",
								ioException.getLocalizedMessage());
					}
				}

			});
			return fileList;
		} catch (Exception exception) {
			logger.error("Error while reading Files from Lamda's /tmp/ folder {}{} Exception {}",
					TransferFilesConstant.TEMP_FOLDER_PATH, sftpPath, exception);
			return Collections.emptyList();

		}

	}

	private Date getDate(BasicFileAttributes fileAttributes) {
		Date date = new Date(fileAttributes.lastModifiedTime().toMillis());

		String pattern = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

		try {
			return new SimpleDateFormat(pattern).parse(simpleDateFormat.format(date));
		} catch (ParseException e) {
			logger.error("Error occurred while getting the Modified Date from the file {}", e.getMessage());
			return null;
		}

	}

	public boolean sendToSftp(List<FileInfo> sftpFileList, SFTPFileTransfer fileTransfer, String sftpPath)
			throws IOException {

		logger.debug("Values: {} {} {}", sftpFileList, fileTransfer, sftpPath);

		int flag = 0;

		for (FileInfo fileInfo : sftpFileList) {
			try {

				fileTransfer.upload(fileInfo.getFile().getPath(), sftpPath + fileInfo.getFileName());

				fileInfo.setFileTransferStatus(TransferFilesConstant.TRANSFER_SUCCESS);
				logger.info("{} Uploaded", fileInfo.getFileName());

			} catch (Exception e) {
				flag = 1;

				fileInfo.setFileTransferStatus(TransferFilesConstant.TRANSFER_FAILED);
				logger.error("Error occurred while sending file to SFTP Server {}", e.getMessage());
			}
		}

		return flag == 0;
	}

	public boolean archiveSftpFiles(List<FileInfo> sftpFileList, SFTPFileTransfer fileTransfer, String sftpPath) {

		logger.debug("Values: {} {} {}", sftpFileList, fileTransfer, sftpPath);

		int flag = 0;

		for (FileInfo fileInfo : sftpFileList) {
			try {

				fileTransfer.upload(fileInfo.getFile().getPath(), sftpPath + fileInfo.getFileName());
				fileInfo.setProcessingEndTimestamp(new Timestamp(System.currentTimeMillis()));
				fileInfo.setSourceFileArchivalStatus(TransferFilesConstant.TRANSFER_SUCCESS);
				logger.info("{} Uploaded", fileInfo.getFileName());

			} catch (Exception e) {
				flag = 1;
				fileInfo.setProcessingEndTimestamp(new Timestamp(System.currentTimeMillis()));
				fileInfo.setSourceFileArchivalStatus(TransferFilesConstant.TRANSFER_FAILED);
				logger.error("Error occurred while sending file to SFTP Server {}", e.getMessage());
			}
		}

		return flag == 0;
	}

	@SuppressWarnings("resource")
	public void deleteSftpFiles(SSHClient sshClient, String sftpPath, List<FileInfo> sftpFilesList) throws IOException {

		if (sshClient != null && sshClient.isConnected()) {
			if (sshClient.isAuthenticated()) {
				SFTPEngine engine = new SFTPEngine(sshClient).init();

				for (FileInfo fileInfo : sftpFilesList) {

					try {

						engine.remove(sftpPath + fileInfo.getFileName());
						fileInfo.setProcessingEndTimestamp(new Timestamp(System.currentTimeMillis()));
						fileInfo.setSourceFileDeletionStatus(TransferFilesConstant.TRANSFER_SUCCESS);
						logger.info("{}{} Deleted", sftpPath, fileInfo.getFileName());

					} catch (Exception e) {
						fileInfo.setProcessingEndTimestamp(new Timestamp(System.currentTimeMillis()));
						fileInfo.setSourceFileDeletionStatus(TransferFilesConstant.TRANSFER_FAILED);
						logger.error("Error occurred while deleting file from SFTP Server {}", e.getMessage());
					}

				}

			} else
				logger.error("SSH is Not authenticated");
		} else
			logger.error("SSH is Not connected SSH Client {}", sshClient);

	}

}
