package com.redhat.rest.example.demorest;


import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.CxfEndpoint;
import org.apache.camel.component.cxf.DataFormat;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class ComplaintManagementApplication {

	public static void main(String[] args) {

		SpringApplication.run(ComplaintManagementApplication.class, args);
	}

	@Value("${businessCentralUserName}")
	private String businessCentralUserName;

	@Value("${businessCentralPassword}")
	private String businessCentralPassword;

	@Value("${businessCentralUrl}")
	private String businessCentralUrl;

	public static final String BODY = "${body}";


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


				restConfiguration()
						.component("servlet")
						.bindingMode(RestBindingMode.auto)
						.producerComponent("http4").host("localhost:8090");


				rest("/businessCentral").get().
						to(
				"rest:get:/kie-server/services/rest/server/containers/ComplaintsManagementSystem/cases/" +
						"ComplaintsManagementWorkflow/instances?bridgeEndpoint=true&" +
						host);

				//start case from the online banking website
				rest("/complaints/online").post()
						.type(CaseData.class).enableCORS(true)
						.route().log(BODY)
						.description("Start Case from Online Banking Flow")
						.removeHeaders("*") // strip all headers (for this example) so that the received message HTTP headers do not confuse the REST producer when POSTing
						.bean(TransformerBean.class,"transformOnlineResponse")
						.to(startCase).endRest();

				//start case from the branch banking website
				rest("/complaints/branch").post()
						.type(CaseData.class).enableCORS(true)
						.route().log(BODY)
						.description("Start Case from Branch Banking Flow")
						.removeHeaders("*") // strip all headers (for this example) so that the received message HTTP headers do not confuse the REST producer when POSTing
						.bean(TransformerBean.class,"transformBranchBanking")
						.to(startCase).endRest();


				//Batch Processing Mode - start cases from excel (scenario for By Phone and By POST)
				from("file:/Users/sadhananandakumar/Documents/Demos/test").
						bean(ExcelConverterBean.class,"process").log(BODY).
						description("Start Case for the Batch Flow").
						split(bodyAs(String.class).tokenize("CaseData")).log(BODY).choice()
						.when(simple("${property.CamelSplitIndex} > 0")).
						bean(TransformerBean.class,"transformExcelResponse").to(startCase).otherwise().end();



				//SOAP Scenario
				CxfEndpoint cxfEndpoint = new CxfEndpoint();
				cxfEndpoint.setAddress("http://localhost:8070/javainuse/ws/complaints");
				cxfEndpoint.setWsdlURL("http://localhost:8070/javainuse/ws/complaints.wsdl");
				cxfEndpoint.setDefaultOperationName("SOATest");
				cxfEndpoint.setDataFormat(DataFormat.PAYLOAD);
				cxfEndpoint.setCamelContext(this.getContext());


				from("timer://foo?fixedRate=true&period=600000").
						transform(simple("<jav:inputSOATest xmlns:jav=\"http://javainuse.com\">\n" +
								"         </jav:inputSOATest>"))
						.to(cxfEndpoint)
						.removeHeaders("*")
						.description("Start case from the B2B flow")
						.bean(TransformerBean.class,"transformSOAPResponse")
						.log("${body}").to(startCase);

		}
		};


	}


}
