package config;

import java.util.Arrays;
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

public class FittleEntityManagerFactory{
	

	private String DB_URL = "jdbc:postgresql://fittle-db.czivym8uhcuy.us-east-1.rds.amazonaws.com:5432/postgres";
	private String DB_USER_NAME = "fittle_user";
	private String DB_PASSWORD = "password123";
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

	protected DataSource getDataSource() {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setUrl(DB_URL);
		dataSource.setUsername(DB_USER_NAME);
		dataSource.setPassword(DB_PASSWORD);
		return dataSource;
	}
	
	

}
