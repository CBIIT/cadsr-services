package gov.nih.nci.cadsr.cchecker;
/*
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
//FIXME exclude this class
//Swagger added to Application class.
//	@Configuration
//	@EnableSwagger2
//	@EnableAutoConfiguration
	public class SwaggerConfig 
	//extends WebMvcConfigurationSupport 
	{
	@Bean
	public Docket productApi() {
		return new Docket(DocumentationType.SWAGGER_2)
				.select()
				.apis(RequestHandlerSelectors.basePackage("gov.nih.nci.cadsr.cchecker"))
				.paths(PathSelectors.any())
					.build().pathMapping("/");
	}
	//@Override
	protected void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");

		registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
	}	
	}	