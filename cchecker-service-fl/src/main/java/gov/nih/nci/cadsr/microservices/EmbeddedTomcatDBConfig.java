/**
 * Copyright (C) 2019 FNLCR - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.jdbc.core.JdbcTemplate;

import gov.nih.nci.cadsr.formloader.repository.impl.LoadServiceRepositoryImpl;
/**
 * We use embedded Tomcat DB data source.
 * 
 * @author asafievan
 *
 */
@Configuration
@ConfigurationProperties(prefix = "spring.datasource")
public class EmbeddedTomcatDBConfig {
	private static final Logger logger = LoggerFactory.getLogger(EmbeddedTomcatDBConfig.class);

	@Autowired
	DataSource dataSource;

	@Primary
	@Bean(name = "jdbcTemplate")
	public JdbcTemplate getJdbcTemplate() {
		logger.debug("jdbcTemplate loading, dataSource defined? " + (dataSource != null));
		JdbcTemplate templ = new JdbcTemplate(dataSource);
		return templ;
	}
	@Primary
	@Bean(name = "loadServiceRepositoryImpl")
	public LoadServiceRepositoryImpl getLoadServiceRepositoryImpl() {
		logger.debug("loadServiceRepositoryImpl loading, dataSource defined? " + (dataSource != null));
		LoadServiceRepositoryImpl templ = new LoadServiceRepositoryImpl(dataSource);
		templ.setDataSource(dataSource);
		return templ;
	}
}
