package com.redhat.rest.example.demorest;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class DemoRestApplication {

	public static void main(String[] args) {

		SpringApplication.run(DemoRestApplication.class, args);
	}



	@Bean
	public RouteBuilder routeBuilder() {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				restConfiguration()
						.component("servlet")
						.bindingMode(RestBindingMode.auto)
						.producerComponent("http4").host("localhost:8090");

				rest("/businessCentral").get().
						to(
				"rest:get:/kie-server/services/rest/server/containers/ComplaintsManagementSystem_1.0.0/cases/" +
						"ComplaintsManagementWorkflow/instances?bridgeEndpoint=true" +
						"&host=agentlogin:Lost2018@localhost:8080")
				;
				

				rest("/businessCentral").post().type(CaseData.class).
						to("rest:post:/kie-server/services/rest/server/containers/ComplaintsManagementSystem_1.0.0/cases/" +
				"ComplaintsManagementWorkflow/instances?bridgeEndpoint=true" +
						"&host=agentlogin:Lost2018@localhost:8080");



			}
		};
	}


}
