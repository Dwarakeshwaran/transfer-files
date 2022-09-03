package config;

import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;
import org.hibernate.jpa.boot.internal.PersistenceUnitInfoDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.google.gson.Gson;

import utils.TransferFilesConstant;

public class FittleEntityManagerFactory {

	private static final Logger logger = LoggerFactory.getLogger(FittleEntityManagerFactory.class);

	private Class<Object>[] entityClasses;

	public FittleEntityManagerFactory(Class<Object>[] entityClasses) {
		this.entityClasses = entityClasses;
	}

	public EntityManager getEntityManager() {
		return getEntityManagerFactory().createEntityManager();
	}

	protected EntityManagerFactory getEntityManagerFactory() {
		PersistenceUnitInfo persistenceUnitInfo = getPersistenceUnitInfo(getClass().getSimpleName());
		Map<String, Object> configuration = new HashMap<>();
		return new EntityManagerFactoryBuilderImpl(new PersistenceUnitInfoDescriptor(persistenceUnitInfo),
				configuration).build();
	}

	protected HibernatePersistenceUnitInfo getPersistenceUnitInfo(String name) {
		return new HibernatePersistenceUnitInfo(name, getEntityClassNames(), getProperties());
	}

	protected List<String> getEntityClassNames() {
		return Arrays.asList(getEntities()).stream().map(Class::getName).collect(Collectors.toList());
	}

	protected Properties getProperties() {
		Properties properties = new Properties();
		properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
		properties.put("hibernate.id.new_generator_mappings", false);
		properties.put("hibernate.show_sql", true);
		properties.put("hibernate.connection.datasource", getDataSource());
		properties.put("hibernate.default_schema", "public");
		properties.put("hibernate.jdbc.batch_size", "100");
		properties.put("hibernate.order_inserts", "true");
		properties.put("hibernate.order_updates", "true");
		return properties;
	}

	protected Class<Object>[] getEntities() {
		return entityClasses;
	}

	@SuppressWarnings("unchecked")
	protected DataSource getDataSource() {
		BasicDataSource dataSource = new BasicDataSource();

		String secret = getSecret(TransferFilesConstant.FITTLE_SECRET);

		Gson gson = new Gson();
		Map<String, String> secretMap = null;

		try {
			secretMap = gson.fromJson(secret, Map.class);

			String dbUrl = secretMap.get(TransferFilesConstant.DB_URL);
			String dbUserName = secretMap.get(TransferFilesConstant.DB_USERNAME);
			String dbPassword = secretMap.get(TransferFilesConstant.DB_PASSWORD);

			dataSource.setUrl(dbUrl);
			dataSource.setUsername(dbUserName);
			dataSource.setPassword(dbPassword);

		} catch (Exception e) {
			logger.error("Wrong secret Key {}", e.getMessage());
		}

		return dataSource;
	}

	private static String getSecret(String secretName) {

		logger.debug("Inside getSecret method {}", secretName);

		String region = System.getenv(TransferFilesConstant.REGION);

		AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard()
				.withCredentials(new DefaultAWSCredentialsProviderChain()).withRegion(region).build();

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
