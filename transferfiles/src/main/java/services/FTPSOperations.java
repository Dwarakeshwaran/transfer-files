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

	public List<FileInfo> getFtpsSourceFile(String sourceCredentials, String sourceHostName, String sourcePath) {

		return null;

	}

	public void sendToFtps(List<FileInfo> sourceFile, String targetCredentials, String targetHostName, String targetPath) {

	}

}
