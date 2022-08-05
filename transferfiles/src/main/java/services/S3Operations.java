package services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import config.S3Config;
import model.FileInfo;
import utils.TransferFilesConstant;

public class S3Operations {

	private static final Logger logger = LoggerFactory.getLogger(S3Operations.class);

	public List<FileInfo> getS3SourceFile(String sourceCredentials, String sourceBucketName, String sourcePath,
			String fileExtension) throws IOException {

		List<S3ObjectSummary> s3Objects = null;
		String key = null;
		String fileName = null;
		GetObjectRequest getObjectRequest = null;
		InputStream s3ObjectStream = null;
		File s3File = null;
		AmazonS3 s3Client = new S3Config().getS3Config(sourceCredentials);
		List<FileInfo> fileList = new ArrayList<>();

		s3Objects = s3Client.listObjects(sourceBucketName, sourcePath).getObjectSummaries();
		s3Objects.sort((a, b) -> a.getLastModified().compareTo(b.getLastModified()));

		for (S3ObjectSummary s3Object : s3Objects) {

			FileInfo fileInfo = new FileInfo();

			/*
			 * 1. Get all the file info from sourceBucketName and sourcePath and store it in
			 * the FileInfo class List.
			 */

			try {

				key = s3Object.getKey();
				fileName = getFileName(key, fileExtension);

				fileInfo.setFileName(fileName);
				fileInfo.setModifiedDate(s3Object.getLastModified());

			} catch (Exception e) {
				logger.error("Error occurred while getting File Name from S3 Bucket {}", e.getMessage());
				key = null;
			}

			/* 2. Download the S3 Object (In InputStream format) from BucketName and Key */

			if (fileName != null) {
				try {

					getObjectRequest = new GetObjectRequest(sourceBucketName, key);
					s3ObjectStream = s3Client.getObject(getObjectRequest).getObjectContent();

				} catch (Exception e) {
					logger.error("Error occurred while downloading the file from S3 Bucket {}", e.getMessage());
					s3ObjectStream = null;
				}
			}

			/*
			 * 3. Copy the contents of the Input Stream to a file and store it in /tmp/
			 * Folder
			 */

			if (s3ObjectStream != null) {

				logger.info("s3ObjectStream is successfully downloaded to Lambda's /tmp/ folder {}", s3ObjectStream);

				try {
					s3File = new File(TransferFilesConstant.TEMP_FOLDER_PATH + fileName);

					Files.copy(s3ObjectStream, s3File.toPath(), StandardCopyOption.REPLACE_EXISTING);
					IOUtils.closeQuietly(s3ObjectStream);

					fileInfo.setFile(s3File);
					logger.info("File Info {}", fileInfo);

					fileList.add(fileInfo);

				} catch (Exception e) {
					logger.error("Error occurred while writing the stream data to the file in temp folder {}",
							e.getMessage());
				}

			} else
				logger.info("s3ObjectStream is null File path is {}", key);

		}

		return fileList;

	}

	private String getFileName(String key, String fileExtension) {

		String[] pathNames = key.split("/");

		String fileName = pathNames[pathNames.length - 1];
		if (key.contains(fileExtension)) {
			logger.info("File Name {}", fileName);
			return fileName;
		} else
			return null;

	}

	public void sendToS3(List<FileInfo> sourceFile, String targetCredentials, String targetBucketName,
			String targetPath) {
		logger.info("Yet to be Coded {} {} {} {}", targetCredentials, sourceFile, targetBucketName, targetPath);
	}

}
