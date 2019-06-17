/*
 * Copyright (C) 2019 FNLCR - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Update NRDS CDEs and Classified CDEs
 * We schedule it at 4AM daily.
 * 
 * @author asafievan
 *
 */
@Component
public class ScheduleUpdateCache {
	private static final Logger logger = LoggerFactory.getLogger(ScheduleUpdateCache.class.getName());
	@Scheduled(cron = "0 0 4 * * *")
	public void updateCdeCache() {
		try {
			logger.info("Update CDE Cache started");
			ReportGeneratorFeed.initCache();
			logger.info("Update CDE Cache done");
		}
		catch (Exception e) {
			logger.error("Error in scheduled updateCdeCache", e);
		}
	}
}
