package config;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketException;
import java.util.Base64;
import java.util.Map;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.google.gson.Gson;

public class FTPServerConfig {

	private static final Logger logger = LoggerFactory.getLogger(FTPServerConfig.class);

	public FTPClient getConnection(String credentials, String ftpsHostName) throws IOException {

		FTPClient ftpClient = null;

		String secret = getSecret(credentials);
		int port = 0;
		String username = null;
		String password = null;

		Gson gson = new Gson();
		Map<String, String> secretMap = null;

		try {
			secretMap = gson.fromJson(secret, Map.class);
		} catch (Exception e) {
			logger.error("Wrong secret Key {}", e.getMessage());
		}

		ftpClient = new FTPClient();

		ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));

		ftpClient.connect(ftpsHostName, port);
		int reply = ftpClient.getReplyCode();
		if (!FTPReply.isPositiveCompletion(reply)) {
			ftpClient.disconnect();
			throw new IOException("Exception in connecting to FTP Server");
		}

		ftpClient.login(username, password);
		System.out.println("Connected to FTP server " + ftpsHostName + ":" + port + " Successfully!");

		return ftpClient;

	}

	private static String getSecret(String secretName) {

		String region = "us-east-1";

		AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard().withRegion(region).build();

		String secret = null;
		String decodedBinarySecret = null;
		GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(secretName);
		GetSecretValueResult getSecretValueResult = null;

		try {
			getSecretValueResult = client.getSecretValue(getSecretValueRequest);
		} catch (Exception e) {
			logger.error("Error while fetching Secret from AWS Secret Manager {}", e.getMessage());
		}

		if (getSecretValueResult != null) {
			if (getSecretValueResult.getSecretString() != null) {
				secret = getSecretValueResult.getSecretString();
				return secret;
			} else {
				decodedBinarySecret = new String(
						Base64.getDecoder().decode(getSecretValueResult.getSecretBinary()).array());
				return decodedBinarySecret;
			}
		} else {
			return null;
		}

	}

}
