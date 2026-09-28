package com.upc.webworksbackend.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
		basePackages = "com.upc.webworksbackend.repository",
		entityManagerFactoryRef = "firstEntityManagerFactory",
		transactionManagerRef = "firstTransactionManager")
public class FirstConf {

	@Bean
	@Primary
	@ConfigurationProperties(prefix = "spring.datasource.first")
	public DataSource firstDataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean(name = "firstEntityManagerFactory")
	@Primary
	public LocalContainerEntityManagerFactoryBean firstEntityManagerFactory(
			@Qualifier("firstDataSource") DataSource firstDataSource) {

		LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
		emf.setDataSource(firstDataSource);
		emf.setPackagesToScan("com.upc.webworksbackend.model");
		emf.setPersistenceUnitName("first");
		emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

		Properties jpaProperties = new Properties();
		jpaProperties.setProperty("hibernate.hbm2ddl.auto", "update");
		jpaProperties.setProperty("hibernate.show_sql", "false");
		emf.setJpaProperties(jpaProperties);

		return emf;
	}

	@Bean(name = "firstTransactionManager")
	@Primary
	public PlatformTransactionManager firstTransactionManager(
			@Qualifier("firstEntityManagerFactory") LocalContainerEntityManagerFactoryBean firstEntityManagerFactory) {
        assert firstEntityManagerFactory.getObject() != null;
        return new JpaTransactionManager(firstEntityManagerFactory.getObject());
	}
}
