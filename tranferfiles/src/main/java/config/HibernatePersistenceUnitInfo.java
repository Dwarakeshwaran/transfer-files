package config;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

import lombok.ToString;

@ToString
public class HibernatePersistenceUnitInfo implements PersistenceUnitInfo {

	private static final Logger logger = (Logger) LoggerFactory.getLogger(HibernatePersistenceUnitInfo.class);

	private String persistenceUnitName;
	private PersistenceUnitTransactionType transactionType = PersistenceUnitTransactionType.RESOURCE_LOCAL;
	private List<String> managedClassNames;
	private Properties properties;

	public HibernatePersistenceUnitInfo(String persistenceUnitName, List<String> managedClassNames,
			Properties properties) {
		this.persistenceUnitName = persistenceUnitName;
		this.managedClassNames = managedClassNames;
		this.properties = properties;
	}

	@Override
	public String getPersistenceUnitName() {
		return persistenceUnitName;
	}

	@Override
	public PersistenceUnitTransactionType getTransactionType() {
		return transactionType;
	}

	@Override
	public Properties getProperties() {
		return properties;
	}

	@Override
	public List<String> getManagedClassNames() {
		return managedClassNames;
	}

	@Override
	public String getPersistenceProviderClassName() {
		return null;
	}

	@Override
	public DataSource getJtaDataSource() {
		return null;
	}

	@Override
	public DataSource getNonJtaDataSource() {
		return null;
	}

	@Override
	public List<String> getMappingFileNames() {
		return Collections.emptyList();
	}

	@Override
	public List<URL> getJarFileUrls() {
		return Collections.emptyList();
	}

	@Override
	public URL getPersistenceUnitRootUrl() {
		return null;
	}

	@Override
	public boolean excludeUnlistedClasses() {
		return false;
	}

	@Override
	public SharedCacheMode getSharedCacheMode() {
		return null;
	}

	@Override
	public ValidationMode getValidationMode() {
		return null;
	}

	@Override
	public String getPersistenceXMLSchemaVersion() {
		return null;
	}

	@Override
	public ClassLoader getClassLoader() {
		return null;
	}

	@Override
	public void addTransformer(ClassTransformer transformer) {
		logger.info("ClassTransformer");
	}

	@Override
	public ClassLoader getNewTempClassLoader() {
		return null;
	}

}
