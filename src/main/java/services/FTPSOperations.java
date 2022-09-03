package services;

import java.io.File;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.FTPServerConfig;
import model.FileInfo;

public class FTPSOperations {

	private static final Logger logger = LoggerFactory.getLogger(FTPSOperations.class);

	public List<FileInfo> getFtpsSourceFileList(FTPClient ftpClient, String sourcePath) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean sendToFtps(List<FileInfo> sourceFilesList, FTPClient ftpClient, String targetPath) {
		// TODO Auto-generated method stub
		return false;
	}

	public void deleteFtpsFiles(FTPClient ftpClient, String sourcePath) {
		// TODO Auto-generated method stub
		
	}

	public boolean archiveFtpsFiles(List<FileInfo> sourceFilesList, FTPClient ftpClient, String sourceArchivalPath) {
		// TODO Auto-generated method stub
		return false;
	}

}
