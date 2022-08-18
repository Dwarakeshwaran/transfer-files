package services;

import java.io.File;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import model.FileInfo;
import utils.TransferFilesConstant;

public class SFTPOperations {

	private static final Logger logger = LoggerFactory.getLogger(SFTPOperations.class);

	private Date getDate(Integer epochTime) {

		long milliseconds = Long.parseLong(epochTime.toString());
		Date date = new Date(milliseconds * 1000L);

		String pattern = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

		try {
			return new SimpleDateFormat(pattern).parse(simpleDateFormat.format(date));
		} catch (ParseException e) {
			logger.error("Error occurred while getting the Modified Date from the file {}", e.getMessage());
			return null;
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<FileInfo> getSftpSourceFileList(ChannelSftp sftpChannel, String sftpPath, String fileExtension) {

		logger.debug("Inside getSftpSourceFileList method {} {}", sftpChannel, sftpPath);

		List<FileInfo> fileList = new ArrayList<>();

		if (sftpChannel != null) {

			/*
			 * 1. Download all the files from SFTP sftp Path to Lambda's /tmp/ Folder
			 */

			List<ChannelSftp.LsEntry> sftpFiles = new ArrayList();
			try {
				sftpChannel.connect();
				sftpFiles = sftpChannel.ls(sftpPath);

			} catch (SftpException | JSchException e) {
				logger.error("Error While Downloading Folder to /tmp/ folder of Lambda");
				e.printStackTrace();
			}

			/*
			 * 2. Read the file names from the /tmp/ folder in Lambda and get all the files
			 * in a File Object and store it in FileInfo list object.
			 */

			for (ChannelSftp.LsEntry sftpFile : sftpFiles) {

				if (sftpFile.getFilename().contains(fileExtension)) {

					FileInfo fileInfo = new FileInfo();

					try {

						String fileName = sftpFile.getFilename();
						sftpChannel.get(sftpPath + fileName, TransferFilesConstant.TEMP_FOLDER_PATH);
						logger.info("File {}{} downloaded to {}", sftpPath, fileName,
								TransferFilesConstant.TEMP_FOLDER_PATH);

						fileInfo.setFileName(fileName);
						fileInfo.setProcessingStartTimestamp(new Timestamp(System.currentTimeMillis()));
						fileInfo.setModifiedDate(getDate(sftpFile.getAttrs().getMTime()));
						fileInfo.setFile(new File(TransferFilesConstant.TEMP_FOLDER_PATH + fileName));
						logger.info("File Info {}", fileInfo);

						fileList.add(fileInfo);

					} catch (SftpException ioException) {

						logger.error(
								"Error occurred while downloading files from SFTP Server to Lamda's /tmp/ folder Exception {}",
								ioException.getLocalizedMessage());
					}
				}

			}

		} else
			logger.error("sftpChannel in getSftpSourceFileList is null");

		return fileList;

	}

	public boolean archiveSftpFiles(ChannelSftp sftpChannel, List<FileInfo> sftpFileList, String sftpArchivePath) {

		logger.debug("Inside archiveSftpFiles method: {} {} {}", sftpFileList, sftpChannel, sftpArchivePath);

		int flag = 0;

		if (sftpChannel != null) {

			for (FileInfo fileInfo : sftpFileList) {

				try {

					if (fileInfo.getFileTransferStatus().equals(TransferFilesConstant.TRANSFER_SUCCESS)) {
						sftpChannel.put(fileInfo.getFile().getPath(), sftpArchivePath + fileInfo.getFileName());
						fileInfo.setProcessingEndTimestamp(new Timestamp(System.currentTimeMillis()));
						fileInfo.setSourceFileArchivalStatus(TransferFilesConstant.TRANSFER_SUCCESS);
						logger.info("{} Archived to {}", fileInfo.getFileName(), sftpArchivePath);
					} else {
						flag = 1;
						fileInfo.setProcessingEndTimestamp(new Timestamp(System.currentTimeMillis()));
						fileInfo.setSourceFileArchivalStatus(TransferFilesConstant.TRANSFER_FAILED);
						logger.error("{} is not uploaded to Destination, the file tranfer status is failed",
								fileInfo.getFileName());
					}

				} catch (Exception e) {
					flag = 1;
					fileInfo.setProcessingEndTimestamp(new Timestamp(System.currentTimeMillis()));
					fileInfo.setSourceFileArchivalStatus(TransferFilesConstant.TRANSFER_FAILED);
					logger.error("Error occurred while archiving file to SFTP Server {}", e.getMessage());
				}
			}

		} else {
			flag = 1;
			logger.error("sftpChannel in archiveSftpFiles is null");
		}

		return flag == 0;
	}

	public void deleteSftpFiles(ChannelSftp sftpChannel, String sftpPath, List<FileInfo> sftpFilesList) {

		logger.debug("Inside deleteSftpFiles method: {} {} {}", sftpChannel, sftpPath, sftpFilesList);

		if (sftpChannel != null) {

			for (FileInfo fileInfo : sftpFilesList) {

				try {

					if (fileInfo.getSourceFileArchivalStatus().equals(TransferFilesConstant.TRANSFER_SUCCESS)) {
						sftpChannel.rm(sftpPath + fileInfo.getFileName());
						fileInfo.setProcessingEndTimestamp(new Timestamp(System.currentTimeMillis()));
						fileInfo.setSourceFileDeletionStatus(TransferFilesConstant.TRANSFER_SUCCESS);
						logger.info("{}{} Deleted", sftpPath, fileInfo.getFileName());
					} else {
						fileInfo.setProcessingEndTimestamp(new Timestamp(System.currentTimeMillis()));
						fileInfo.setSourceFileDeletionStatus(TransferFilesConstant.TRANSFER_FAILED);
						logger.error("{} is not yet Archived, the archival status is failed", fileInfo.getFileName());
					}

				} catch (Exception e) {
					fileInfo.setProcessingEndTimestamp(new Timestamp(System.currentTimeMillis()));
					fileInfo.setSourceFileDeletionStatus(TransferFilesConstant.TRANSFER_FAILED);
					logger.error("Error occurred while deleting file from SFTP Server {}", e.getMessage());
				}

			}
		} else
			logger.error("sftpChannel in deleteSftpFiles is null");

	}

	public boolean sendToSftp(ChannelSftp sftpChannel, List<FileInfo> sftpFileList, String sftpPath) {

		logger.debug("Inside sendToSftp method: {} {} {}", sftpFileList, sftpChannel, sftpPath);

		int flag = 0;

		if (sftpChannel != null) {

			try {
				sftpChannel.connect();
			} catch (JSchException e) {
				logger.error("Error While connecting to SFTP Channel");
				e.printStackTrace();
			}

			for (FileInfo fileInfo : sftpFileList) {
				try {

					sftpChannel.put(fileInfo.getFile().getPath(), sftpPath + fileInfo.getFileName());
					fileInfo.setProcessingEndTimestamp(new Timestamp(System.currentTimeMillis()));
					fileInfo.setFileTransferStatus(TransferFilesConstant.TRANSFER_SUCCESS);
					logger.info("{} Uploaded to {}", fileInfo.getFileName(), sftpPath);

				} catch (Exception e) {
					flag = 1;
					fileInfo.setProcessingEndTimestamp(new Timestamp(System.currentTimeMillis()));
					fileInfo.setFileTransferStatus(TransferFilesConstant.TRANSFER_FAILED);
					logger.error("Error occurred while sending file to SFTP Server {}", e.getMessage());
				}
			}

		} else {
			flag = 1;
			logger.error("sftpChannel is null");
		}

		return flag == 0;
	}

}
