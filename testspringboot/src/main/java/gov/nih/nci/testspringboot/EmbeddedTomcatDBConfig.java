package gov.nih.nci.testspringboot;

import java.util.Objects;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class EmbeddedTomcatDBConfig {
	private static final Logger logger = LoggerFactory.getLogger(EmbeddedTomcatDBConfig.class);
	@Autowired
	DataSource dataSource;

	  @Primary
	  @Bean(name = "jdbcTemplate")
	  public JdbcTemplate getJdbcTemplate() {
		Objects.requireNonNull(dataSource, "dataSource is null !!!");
		logger.debug("jdbcTemplate loading");
		JdbcTemplate templ = new JdbcTemplate(dataSource);
	    return templ;
	  }
}
