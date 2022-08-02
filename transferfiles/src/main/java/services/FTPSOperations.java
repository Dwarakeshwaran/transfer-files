package services;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.FTPServerConfig;

public class FTPSOperations {
	
	private static final Logger logger = LoggerFactory.getLogger(FTPSOperations.class);
	
	private static FTPServerConfig ftpConfig = new FTPServerConfig(); 
	
	public File getFtpsSourceFile(String sourceHostName, String sourcePath) {
		
		return null;
		
	}

	public void sendToFtps(File sourceFile, String targetHostName, String targetPath) {

		
	}

}
