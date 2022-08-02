package services;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.SFTPServerConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.sftp.SFTPEngine;
import net.schmizz.sshj.sftp.SFTPFileTransfer;

public class SFTPOperations {

	private static final Logger logger = LoggerFactory.getLogger(SFTPOperations.class);

	private static SFTPServerConfig sftpConfig = new SFTPServerConfig();

	public File getSftpSourceFile(String sourceHostName, String sourcePath) {

		logger.info("Yet to be Coded {} {}", sourceHostName, sourcePath);

		return null;

	}

	@SuppressWarnings("resource")
	public void sendToSftp(File sourceFile, String targetHostName, String targetPath) throws IOException {

		logger.info("Values: {} {} {}", sourceFile, targetHostName, targetPath);

		SSHClient sshClient = new SSHClient();
		sshClient = sftpConfig.getSSHConnection(sshClient, targetHostName, "fittle-test-user");
		
		SFTPClient sftpClient = sshClient.newSFTPClient();
		SFTPEngine engine = new SFTPEngine(sshClient).init();

		if (sshClient.isConnected()) {
			if (sshClient.isAuthenticated()) {
				try {

					SFTPFileTransfer fileTransfer = new SFTPFileTransfer(engine);
					fileTransfer.setPreserveAttributes(false);
					fileTransfer.upload(sourceFile.getPath(), targetPath + sourceFile.getName());
					logger.info("File Sent");

				} catch (Exception e) {
					logger.error("Error occurred while sending file to SFTP Server {}", e.getMessage());
				} finally {
					sshClient.close();
					sftpClient.close();
					engine.close();
				}

			} else
				logger.error("SSH is Not authenticated");
		} else
			logger.error("SSH is Not connected");

	}

}
