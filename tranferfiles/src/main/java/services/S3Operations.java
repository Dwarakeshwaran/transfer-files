package services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import config.S3Config;
import utils.TransferFilesConstant;

public class S3Operations {

	private static final Logger logger = LoggerFactory.getLogger(S3Operations.class);

	private static AmazonS3 s3Client = new S3Config().getS3Config();

	public File getS3SourceFile(String sourceBucketName, String sourcePath) throws IOException {

		List<S3ObjectSummary> s3Objects = null;
		String key = null;
		String fileName = null;
		GetObjectRequest getObjectRequest = null;
		InputStream s3ObjectStream = null;
		File s3File = null;

		/*
		 * 1. Get the latest file name from sourceBucketName and sourcePath
		 */

		try {
			s3Objects = s3Client.listObjects(sourceBucketName, sourcePath).getObjectSummaries();
			s3Objects.sort((a, b) -> a.getLastModified().compareTo(b.getLastModified()));

			key = s3Objects.get(s3Objects.size() - 1).getKey();
			fileName = getFileName(key);

			logger.info("File Name: {}", fileName);
		} catch (Exception e) {
			logger.error("Error occurred while getting File Name from S3 Bucket {}", e.getMessage());
			key = null;
		}

		/* 2. Download the S3 Object (In InputStream format) from BucketName and Key */

		if (key != null) {
			try {

				getObjectRequest = new GetObjectRequest(sourceBucketName, key);
				s3ObjectStream = s3Client.getObject(getObjectRequest).getObjectContent();

			} catch (Exception e) {
				logger.error("Error occurred while downloading the file from S3 Bucket {}", e.getMessage());
				s3ObjectStream = null;
			}
		} else
			logger.error("Key is null");

		/*
		 * 3. Writes the contents of the Input Stream to a file and store it in /tmp/
		 * Folder
		 */

		if (s3ObjectStream != null) {

			try {
				s3File = new File(TransferFilesConstant.TEMP_FOLDER_PATH + fileName);

				try (FileOutputStream out = new FileOutputStream(s3File)) {
					out.write(s3ObjectStream.readAllBytes());
				}
			} catch (Exception e) {
				logger.error("Error occurred while writing the stream data to the file in temp folder ");
			}

		} else
			logger.error("s3ObjectStream is null");

		return s3File;

	}

	private String getFileName(String key) {

		String[] pathNames = key.split("/");

		return pathNames[pathNames.length - 1];
	}

	public void sendToS3(File sourceFile, String targetBucketName, String targetPath) {
		logger.info("Yet to be Coded {} {} {}", sourceFile, targetBucketName, targetPath);
	}

}
