package services;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.FTPServerConfig;
import model.FileInfo;

public class FTPSOperations {

	private static final Logger logger = LoggerFactory.getLogger(FTPSOperations.class);

	private static FTPServerConfig ftpConfig = new FTPServerConfig();

	public List<FileInfo> getFtpsSourceFileList(String ftpsCredentials, String ftpsHostName, String ftpsPath) {

		return null;

	}

	public boolean sendToFtps(List<FileInfo> ftpsFile, String ftpsCredentials, String ftpsHostName, String ftpsPath) {

		return false;
	}

	public void deleteFtpsFiles(String sourceCredentials, String sourceHostName, String sourceArchivalPath) {
		// TODO Auto-generated method stub
		
	}

}
