package com.redhat.rest.example.demorest;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.InputStream;

@SpringBootApplication
@RestController
public class DemoRestApplication {

	public static void main(String[] args) {

		SpringApplication.run(DemoRestApplication.class, args);
	}

	@Value("${businessCentralUserName}")
	private String businessCentralUserName;

	@Value("${businessCentralPassword}")
	private String businessCentralPassword;

	@Value("${businessCentralUrl}")
	private String businessCentralUrl;



	@Bean
	public RouteBuilder routeBuilder() {
		return new RouteBuilder() {


			private String host = "host="+businessCentralUserName+":"+businessCentralPassword+"@"+businessCentralUrl;


			@Override
			public void configure() throws Exception {

			String startCase = "rest:post:/kie-server/services/rest/server/containers/ComplaintsManagementSystem_1.0.0/cases/" +
						"ComplaintsManagementWorkflow/instances?" +
						host +
						"&produces=application/json";

				System.out.println("start case::::::"+startCase);

				restConfiguration()
						.component("servlet")
						.bindingMode(RestBindingMode.auto)
						.producerComponent("http4").host("localhost:8090");

				rest("/businessCentral").get().
						to(
				"rest:get:/kie-server/services/rest/server/containers/ComplaintsManagementSystem/cases/" +
						"ComplaintsManagementWorkflow/instances?bridgeEndpoint=true&" +
						host);


				//start case in Case Manager
				rest("/complaints/online").post()
						.type(CaseData.class).enableCORS(true)
						.route().log("${body}")
						.removeHeaders("*") // strip all headers (for this example) so that the received message HTTP headers do not confuse the REST producer when POSTing
						.bean(TransformerBean.class,"transformOnlineResponse")
						.to(startCase).endRest();

				//start case in Case Manager
				rest("/complaints/branch").post()
						.type(CaseData.class).enableCORS(true)
						.route().log("${body}")
						.removeHeaders("*") // strip all headers (for this example) so that the received message HTTP headers do not confuse the REST producer when POSTing
						.bean(TransformerBean.class,"transformBranchBanking")
						.to(startCase).endRest();

				//Batch Processing Mode

				ProducerTemplate template = this.getContext().createProducerTemplate();
				InputStream orderxml = new FileInputStream("src/main/resources/order.xml");

				

			}
		};
	}


}
