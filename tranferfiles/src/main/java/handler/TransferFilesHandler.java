package handler;

import java.io.IOException;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.util.Map;

import javax.persistence.EntityManager;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;

import config.FittleEntityManagerFactory;
import config.S3Config;
import config.SFTPServerConfig;
import entity.FittleFileAuditHistoryEntity;
import entity.FittleFileConfigEntity;

public class TransferFilesHandler implements RequestHandler<Map<String, String>, Object> {

	public static void main(String[] args) throws SocketException, IOException, URISyntaxException {

//		FTPServerConfig ftpConfig = new FTPServerConfig();
//		ftpConfig.getConnection("localhost", 4567, "root", "pass");

		S3Config s3Config = new S3Config();
		AmazonS3 s3Client = s3Config.getS3Config();

		System.out.println(s3Client.doesBucketExist("dwaki-transfer-files"));

		SFTPServerConfig sftpConfig = new SFTPServerConfig();
		sftpConfig.getSSHConnection("s-52abc61a9b794409b.server.transfer.us-east-1.amazonaws.com", "fittle-test-user");

		EntityManager entityManager = getEntityManager();

		FittleFileConfigEntity fileConfig = entityManager.find(FittleFileConfigEntity.class, "s3-to-sftp");

		System.out.println(fileConfig);

	}

	@Override
	public Object handleRequest(Map<String, String> input, Context context) {

		String jobId = input.get("file_job_id");

		EntityManager entityManager = getEntityManager();

		FittleFileConfigEntity fileConfig = entityManager.find(FittleFileConfigEntity.class, jobId);

		System.out.println(fileConfig);

		return null;
	}

	public static EntityManager getEntityManager() {
		return new FittleEntityManagerFactory(
				new Class[] { FittleFileConfigEntity.class, FittleFileAuditHistoryEntity.class }).getEntityManager();
	}

}
