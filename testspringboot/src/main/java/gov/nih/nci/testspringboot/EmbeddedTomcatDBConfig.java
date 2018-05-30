package gov.nih.nci.testspringboot;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class EmbeddedTomcatDBConfig {
	private final Logger logger =  Logger.getLogger(EmbeddedTomcatDBConfig.class.getName());
	private DataSource ds;
	  @Primary
	  @Bean(name = "dataSource")
	  @ConfigurationProperties(prefix = "spring.datasource")
	  public DataSource dataSource() {
		logger.debug("spring.datasource loading");
		ds = DataSourceBuilder.create().build();
	    return ds;
	  }
	  @Primary
	  @Bean(name = "jdbcTemplate")
	  public JdbcTemplate getJdbcTemplate() {
		logger.debug("jdbcTemplate loading");
		JdbcTemplate templ = new JdbcTemplate(ds);
	    return templ;
	  }
}
