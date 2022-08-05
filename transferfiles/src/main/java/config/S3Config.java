package config;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class S3Config {

	public AmazonS3 getS3Config(String credentials) {

		if (credentials.equals("internal"))
			return AmazonS3ClientBuilder.standard().withCredentials(new DefaultAWSCredentialsProviderChain())
					.withRegion(Regions.US_EAST_1).build();
		else
			return null;

	}

}
