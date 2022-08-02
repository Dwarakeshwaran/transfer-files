package config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Base64;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;

public class SFTPServerConfig {

	public SSHClient getSSHConnection(String remoteHost, String username) throws IOException, URISyntaxException {

		SSHClient client = new SSHClient();
		client.addHostKeyVerifier(new PromiscuousVerifier());
		client.connect(remoteHost);

//		System.out.println("Secret: " + getSecret());
		
		FileOutputStream outputStream = new FileOutputStream(new File("key.ppk"));
		byte[] keyBytes = getSecret().getBytes();
		outputStream.write(keyBytes);
		
		KeyProvider key = client.loadKeys("key.ppk");
		client.authPublickey(username, key);

		System.out.println("Connected to SFTP server " + remoteHost + " Successfully!");

		return client;

	}

	public static String getSecret() {

		String secretName = "sftpServerKey";
		String region = "us-east-1";

		AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard().withRegion(region).build();

		String secret = null, decodedBinarySecret = null;
		GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(secretName);
		GetSecretValueResult getSecretValueResult = null;

		try {
			getSecretValueResult = client.getSecretValue(getSecretValueRequest);
		} catch (Exception e) {
			System.out.println(e);
		}

		if (getSecretValueResult.getSecretString() != null) {
			secret = getSecretValueResult.getSecretString();
			return secret;
		} else {
			decodedBinarySecret = new String(
					Base64.getDecoder().decode(getSecretValueResult.getSecretBinary()).array());
			return decodedBinarySecret;
		}

	}

}
