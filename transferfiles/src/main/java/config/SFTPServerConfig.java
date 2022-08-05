package config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.google.gson.Gson;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import utils.TransferFilesConstant;

public class SFTPServerConfig {

	private static final Logger logger = LoggerFactory.getLogger(SFTPServerConfig.class);

	@SuppressWarnings("unchecked")
	public SSHClient getSSHConnection(SSHClient client, String credentials, String remoteHost) throws IOException {

		String secret = getSecret(credentials);
		String username = null;
		String keyString = null;

		Gson gson = new Gson();
		Map<String, String> secretMap = null;

		try {
			secretMap = gson.fromJson(secret, Map.class);
		} catch (Exception e) {
			logger.error("Wrong secret Key {}", e.getMessage());
		}

		try {

			client.addHostKeyVerifier(new PromiscuousVerifier());
			client.connect(remoteHost, 22);

			try (FileOutputStream outputStream = new FileOutputStream(
					new File(TransferFilesConstant.TEMP_FOLDER_PATH + "key.ppk"))) {

				if (secretMap != null) {

					username = secretMap.get("username");
					keyString = secretMap.get("key");

					byte[] keyBytes = keyString.getBytes();
					outputStream.write(keyBytes);
				}

			}

			KeyProvider key = client.loadKeys(TransferFilesConstant.TEMP_FOLDER_PATH + "key.ppk");
			client.authPublickey(username, key);
			logger.info("Connected to SFTP server {} Successfully!", remoteHost);

			return client;
		} catch (Exception e) {
			logger.error("Error Occurred While Connecting to SFTP Server {}", e.getMessage());
			return null;
		}

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
