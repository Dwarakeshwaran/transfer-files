package handler;

import java.io.IOException;
import java.util.Map;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;

import config.FittleEntityManagerFactory;
import config.S3Config;
import config.SFTPServerConfig;
import entity.FittleFileAuditHistoryEntity;
import entity.FittleFileConfigEntity;
import services.TransferFileService;

public class TransferFilesHandler implements RequestHandler<Map<String, String>, Object> {

	private static final Logger logger = LoggerFactory.getLogger(TransferFilesHandler.class);
	private static TransferFileService service = new TransferFileService();

	public static void main(String[] args) throws IOException {

//		FTPServerConfig ftpConfig = new FTPServerConfig();
//		ftpConfig.getConnection("localhost", 4567, "root", "pass");

		S3Config s3Config = new S3Config();
		AmazonS3 s3Client = s3Config.getS3Config();

		logger.info("S3 Client {}", s3Client);

		SFTPServerConfig sftpConfig = new SFTPServerConfig();
		sftpConfig.getSSHConnection("s-52abc61a9b794409b.server.transfer.us-east-1.amazonaws.com", "fittle-test-user");
		
		/*
		 * Get File job Id from fileConfig Object and redirect it to it's respective
		 * service class
		 */

		EntityManager entityManager = getEntityManager();

		FittleFileConfigEntity fileConfig = entityManager.find(FittleFileConfigEntity.class, "s3-to-sftp");

		logger.info("File Config {}", fileConfig);

		if (fileConfig != null)
			service.transferFiles(fileConfig);
		else
			logger.error("File Config Object is null");

	}

	@Override
	public Object handleRequest(Map<String, String> input, Context context) {

		String jobId = input.get("file_job_id");

		EntityManager entityManager = getEntityManager();

		FittleFileConfigEntity fileConfig = entityManager.find(FittleFileConfigEntity.class, jobId);

		logger.info("File Config {}", fileConfig);

		return null;
	}

	@SuppressWarnings("unchecked")
	public static EntityManager getEntityManager() {
		return new FittleEntityManagerFactory(
				new Class[] { FittleFileConfigEntity.class, FittleFileAuditHistoryEntity.class }).getEntityManager();
	}

}
