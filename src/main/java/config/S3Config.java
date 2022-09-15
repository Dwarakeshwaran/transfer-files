package config;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import utils.TransferFilesConstant;

public class S3Config {

	public AmazonS3 getS3Config(String system) {

		String region = "us-east-1";

		if (system.equals(TransferFilesConstant.INTERNAL_SYSTEM))
			return AmazonS3ClientBuilder.standard().withCredentials(new DefaultAWSCredentialsProviderChain())
					.withRegion(region).build();
		else
			return null;

	}

}
