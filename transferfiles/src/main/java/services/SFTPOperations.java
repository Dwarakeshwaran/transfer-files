package services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.SFTPServerConfig;
import model.FileInfo;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPEngine;
import net.schmizz.sshj.sftp.SFTPFileTransfer;
import utils.TransferFilesConstant;

public class SFTPOperations {

	private static final Logger logger = LoggerFactory.getLogger(SFTPOperations.class);

	private static SFTPServerConfig sftpConfig = new SFTPServerConfig();

	@SuppressWarnings("resource")
	public List<FileInfo> getSftpSourceFileList(String sftpCredentials, String sftpHostName, String sftpPath,
			String fileExtension) throws IOException {

		logger.debug("Values {} {} {}", sftpCredentials, sftpHostName, sftpPath);

		List<FileInfo> fileList = new ArrayList<>();
		SSHClient sshClient = new SSHClient();

		SFTPEngine engine = null;
		sshClient = sftpConfig.getSSHConnection(sshClient, sftpCredentials, sftpHostName);

		if (sshClient != null) {
			if (sshClient.isConnected() && sshClient.isAuthenticated()) {

				/*
				 * 1. Download all the files from SFTP sftp Path to Lambda's /tmp/ Folder
				 */

				engine = new SFTPEngine(sshClient).init();
				SFTPFileTransfer fileTransfer = new SFTPFileTransfer(engine);

				fileTransfer.download(sftpPath, TransferFilesConstant.TEMP_FOLDER_PATH);

				logger.info("All files from {} have been downloaded to the /tmp/ folder of Lambda", sftpPath);

				/*
				 * 2. Read the file names from the /tmp/ folder in Lambda and get all the files
				 * in a File Object and store it in FileInfo list object.
				 */

				try (Stream<Path> paths = Files.walk(Paths.get(TransferFilesConstant.TEMP_FOLDER_PATH + sftpPath))) {
					paths.filter(Files::isRegularFile).forEach(filePath -> {

						if (filePath.getFileName().toString().contains(fileExtension)) {

							FileInfo fileInfo = new FileInfo();

							try {
								BasicFileAttributes fileAttributes = Files.readAttributes(filePath,
										BasicFileAttributes.class);

								String fileName = filePath.getFileName().toString();
								fileTransfer.download(sftpPath + fileName, TransferFilesConstant.TEMP_FOLDER_PATH);
								logger.info("File {} downloaded to {}", sftpPath + fileName,
										TransferFilesConstant.TEMP_FOLDER_PATH);

								fileInfo.setFileName(fileName);
								fileInfo.setModifiedDate(getDate(fileAttributes));
								fileInfo.setFile(new File(TransferFilesConstant.TEMP_FOLDER_PATH + fileName));
								logger.info("File Info {}", fileInfo);

								fileList.add(fileInfo);

							} catch (IOException e) {

								logger.error(
										"Error occurred while downloading files from SFTP Server to Lamda's /tmp/ folder Exception {}",
										e.getLocalizedMessage());
							}
						}

					});
				}

				engine.close();

				return fileList;

			} else
				logger.error("SSH Connection {} SSH Authentication {}", sshClient.isConnected(),
						sshClient.isAuthenticated());

		} else
			logger.error("SSH Client Value {}", sshClient);

		return Collections.emptyList();

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

	@SuppressWarnings("resource")
	public boolean sendToSftp(List<FileInfo> sftpFileList, String sftpCredentials, String sftpHostName,
			String sftpPath) throws IOException {

		logger.debug("Values: {} {} {} {}", sftpFileList, sftpCredentials, sftpHostName, sftpPath);

		int flag = 0;

		SSHClient sshClient = new SSHClient();
		SFTPEngine engine = null;

		try {
			sshClient = sftpConfig.getSSHConnection(sshClient, sftpCredentials, sftpHostName);
		} catch (IOException exception) {
			logger.error("Error occurred while connecting to SFTP Server {}", exception.getMessage());
		}

		if (sshClient != null && sshClient.isConnected()) {
			if (sshClient.isAuthenticated()) {
				try {

					engine = new SFTPEngine(sshClient).init();

					SFTPFileTransfer fileTransfer = new SFTPFileTransfer(engine);
					fileTransfer.setPreserveAttributes(false);

					for (FileInfo fileInfo : sftpFileList) {
						fileTransfer.upload(fileInfo.getFile().getPath(), sftpPath + fileInfo.getFileName());

						logger.info("{} Uploaded", fileInfo.getFileName());
					}

					engine.close();

				} catch (Exception e) {
					flag = 1;
					logger.error("Error occurred while sending file to SFTP Server {}", e.getMessage());
				} finally {
					sshClient.close();
				}

			} else
				logger.error("SSH is Not authenticated");
		} else
			logger.error("SSH is Not connected SSH Client {}", sshClient);

		return flag == 0;
	}

	public void deleteSftpFiles(String sourceCredentials, String sourceHostName, String sourceArchivalPath) {
		// TODO Auto-generated method stub
		
	}

}
