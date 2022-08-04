package handler;

import java.io.IOException;
import java.util.Map;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import config.FittleEntityManagerFactory;
import entity.FittleFileAuditHistoryEntity;
import entity.FittleFileConfigEntity;
import services.TransferFileService;

public class TransferFilesHandler implements RequestHandler<Map<String, String>, Object> {

	private static final Logger logger = LoggerFactory.getLogger(TransferFilesHandler.class);
	private static TransferFileService service = new TransferFileService();

	public static void main(String[] args) throws IOException, InterruptedException {

		/*
		 * Get File job Id from fileConfig Object and redirect it to it's respective
		 * service class
		 */

		EntityManager entityManager = getEntityManager();

		FittleFileConfigEntity fileConfig = entityManager.find(FittleFileConfigEntity.class, "sftp-to-s3");

		logger.info("File Config {}", fileConfig);

		if (fileConfig != null)
			service.transferFiles(fileConfig);
		else
			logger.error("File Config Object is null");

	}

	@Override
	public Object handleRequest(Map<String, String> input, Context context) {

		/*
		 * Get File job Id from fileConfig Object and redirect it to it's respective
		 * service class
		 */

		String jobId = input.get("file_job_id");

		EntityManager entityManager = getEntityManager();

		FittleFileConfigEntity fileConfig = entityManager.find(FittleFileConfigEntity.class, jobId);

		logger.info("File Config {}", fileConfig);

		if (fileConfig != null)
			try {
				service.transferFiles(fileConfig);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		else
			logger.error("File Config Object is null");

		return null;
	}

	@SuppressWarnings("unchecked")
	public static EntityManager getEntityManager() {
		return new FittleEntityManagerFactory(
				new Class[] { FittleFileConfigEntity.class, FittleFileAuditHistoryEntity.class }).getEntityManager();
	}

}
