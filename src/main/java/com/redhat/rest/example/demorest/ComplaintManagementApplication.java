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

	@Value("${fileLocation}")
	private String fileLocation;

	@Value("${soapEndPoint}")
	private String soapEndPoint;

	@Value("${soapWsdlURL}")
	private String soapWsdlURL;

	@Value("${soapOperation}")
	private String soapOperation;


	public static final String BODY = "${body}";


	@Bean
	public RouteBuilder routeBuilder() {
		return new RouteBuilder() {


			private String host = "host="+businessCentralUserName+":"+businessCentralPassword+"@"+businessCentralUrl;


			@Override
			public void configure() throws Exception {

			String SOAPCallPayload = "<jav:inputSOATest xmlns:jav=\"http://javainuse.com\">\n" +
					"         </jav:inputSOATest>";

			String startCase = "rest:post:/kie-server/services/rest/server/containers/ComplaintsManagementSystem_1.0.0/cases/" +
						"ComplaintsManagementWorkflow/instances?" +
						host +
						"&produces=application/json";


				restConfiguration()
						.component("servlet")
						.bindingMode(RestBindingMode.auto)
						.producerComponent("http4").host("localhost:8090");




				//start case from the online banking website
				rest("/complaints/online")
						.post()
						.type(CaseData.class).enableCORS(true)
						.route().id("Complaints Management - Online Banking")
						.removeHeaders("*") // strip all headers (for this example) so that the received message HTTP headers do not confuse the REST producer when POSTing
						.bean(TransformerBean.class,"transformOnlineResponse")
						.id("Enrich/Add Categorization")
						.to(startCase).id("Start Case Management Case:Online")
						.endRest();

				//start case from the branch banking website
				rest("/complaints/branch")
						.post()
						.type(CaseData.class).enableCORS(true)
						.route()
						.id("Complaints Management - Branch Banking")
						.removeHeaders("*") // strip all headers (for this example) so that the received message HTTP headers do not confuse the REST producer when POSTing
						.bean(TransformerBean.class,"transformBranchBanking")
						.id("Enrich/Add Customer Information")
						.to(startCase).id("Start Case Management Case:Branch")
						.endRest();


				//Batch Processing Mode - start cases from excel (scenario for By Phone and By POST)
				from("file:"+fileLocation)
						.routeId("Complaints Management - File Polling/Batch File Upload Mode")
						.bean(ExcelConverterBean.class,"process")
						.id("Parse Excel Data")
						.split(bodyAs(String.class)
						.tokenize("CaseData"))
						.id("Split each case detail")
						.choice()
						.id("For every record from the excel bean")
						.when(simple("${property.CamelSplitIndex} > 0"))
						.id("case count > 0")
						.bean(TransformerBean.class,"transformExcelResponse")
						.id("Enrich Data from case Creation: Batch Mode")
						.to(startCase)
						.id("Start Case Management Case:File Mode")
						.otherwise()
						.id("End of File")
						.end();



				//SOAP Scenario
				CxfEndpoint cxfEndpoint = new CxfEndpoint();
				cxfEndpoint.setAddress(soapEndPoint);
				cxfEndpoint.setWsdlURL(soapWsdlURL);
				cxfEndpoint.setDefaultOperationName(soapOperation);
				cxfEndpoint.setDataFormat(DataFormat.PAYLOAD);
				cxfEndpoint.setCamelContext(this.getContext());


				from("timer://regulatoryChannelTimer?fixedRate=true&period=600000")
						.routeId("Complaints Management - Regulatory Channel")
						.transform(simple(SOAPCallPayload))
						.id("Enrich Data for SOAP Call")
						.to(cxfEndpoint).id("Call SOAP Channel")
						.removeHeaders("*")
						.bean(TransformerBean.class,"transformSOAPResponse")
						.id("Enrich Data from case Creation")
						.to(startCase)
						.id("Start Case Management Case:SOAP Channel");

		}
		};


	}


}
