package com.upc.webworksbackend.serviceimplements;

import javax.sql.DataSource;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;


@Component
public class ExecuteQuery implements ApplicationRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteQuery.class);

	private final DataSource firstDataSource;

	public ExecuteQuery(@Qualifier("firstDataSource") DataSource firstDataSource) {
		this.firstDataSource = firstDataSource;
	}

	@Override
	public void run(@NonNull ApplicationArguments args) {
		ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
		populator.addScript(new ClassPathResource("core/insert_money.sql"));
		populator.addScript(new ClassPathResource("core/insert_plan.sql"));
		populator.addScript(new ClassPathResource("core/insert_users.sql"));
		populator.addScript(new ClassPathResource("core/insert_company.sql"));
		populator.addScript(new ClassPathResource("pagos/insert_promotioncode.sql"));
		populator.addScript(new ClassPathResource("pagos/insert_methodpayment.sql"));
		populator.addScript(new ClassPathResource("pagos/insert_subscription.sql"));
		populator.addScript(new ClassPathResource("portafolio/insert_repository.sql"));
		populator.addScript(new ClassPathResource("portafolio/insert_project.sql"));
		populator.addScript(new ClassPathResource("empleo/insert_employment.sql"));
		populator.addScript(new ClassPathResource("empleo/insert_jobapplication.sql"));
		populator.addScript(new ClassPathResource("comunidad/insert_commentprofile.sql"));
		populator.addScript(new ClassPathResource("comunidad/insert_systemscore.sql"));

		try {
			populator.execute(firstDataSource);
			LOGGER.info("Archivos SQL ejecutado correctamente.");
		} catch (Exception exception) {
			LOGGER.error("Error al ejecutar archivos SQL", exception);
		}
	}
}
