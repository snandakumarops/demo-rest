package com.redhat.rest.example.demorest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PropertySource("classpath:categorization.properties")
public class TransformerBean {

    @Autowired
    private Categorization categorization;

    @Autowired
    private CustomerDetails customerDetails;


    private static final String CASE_USER_ASSIGNMENT="\"case-user-assignments\" : { \"admin\" : \"pamAdmin\" }}";

    public String transformOnlineResponse(String reqJson) {
        Map<String,String> categoryMap = calculateCategoryBusinessUnit(reqJson);
        String rawJson = StringUtils.chop(reqJson);
        StringBuilder returnString = new StringBuilder();
        for(String key:categoryMap.keySet()) {
            returnString.append("{\"case-data\" :  " )
                        .append(rawJson)
                        .append(",\"category\":\"")
                        .append(key).append("\",\"businessUnit\":\"")
                        .append(categoryMap.get(key))
                        .append("\"} ,")
                        .append(CASE_USER_ASSIGNMENT);

        }
        return returnString.toString();
    }

    public Map<String,String> calculateCategoryBusinessUnit(String reqJson) {

        String category = "Other";
        String businessUnit = "OtherBU";
        String complaintsDescription = StringUtils.substringBetween(reqJson,"complaintsDescription\":\"","\"}");

        Map<String,String> properties = categorization.getMapProperty();

        for(String key:properties.keySet()) {
            List<String> list = Arrays.asList(properties.get(key).split(","));
            boolean match = list.stream().anyMatch(s -> complaintsDescription.contains(s));

            if(match) {
                category = key;
                businessUnit = key + "BU";
            }
        }

        Map<String,String> categoryBusinessUnit = new HashMap<>();
        categoryBusinessUnit.put(category,businessUnit);

        return categoryBusinessUnit;


    }

    public String transformBranchBanking(String reqJson) {
        String businessUnit = StringUtils.substringBetween(reqJson,"category\":\"","\",")+"BU";
        String rawJson = StringUtils.chop(reqJson);
        rawJson= rawJson + ",\""+lookUpUserDetails(StringUtils.substringBetween(reqJson,"customerAccNo\":\"","\","));
        StringBuilder returnString = new StringBuilder();

        returnString.append("{\"case-data\" :  ")
                    .append(rawJson)
                    .append(",\"businessUnit\":\"")
                    .append(businessUnit)
                    .append("\"} ,")
                    .append(CASE_USER_ASSIGNMENT);


        return returnString.toString();
    }

    public String lookUpUserDetails(String customerNumber) {
        StringBuilder returnString = new StringBuilder();
        //logic to pull customer details based on customer Number
        if(customerDetails.getCustomerNo().equals(customerNumber)) {
           String[] customerDet= customerDetails.getCustDetails().split(",");
           returnString.append("customerName\":\"")
                       .append(customerDet[0])
                       .append("\",\"customerPhone\":\""+customerDet[1])
                       .append("\",\"customerAddress\":\""+customerDet[2]+"\"");
        }

        return returnString.toString();

    }

    public String transformExcelResponse(String reqJson) {

        String rawJson = StringUtils.chop(reqJson);
        StringBuilder returnString = new StringBuilder();
        returnString.append("{\"case-data\" :  {")
                    .append("\"customerName\":\"" + StringUtils.substringBetween(rawJson, "customerName='", "'"))
                    .append("\",\"customerPhone\":\"" + StringUtils.substringBetween(rawJson, "customerPhone='", "'"))
                    .append("\",\"customerAddress\":\"" + StringUtils.substringBetween(rawJson, "customerAddress='", "'"))
                    .append("\",\"category\":\"" + StringUtils.substringBetween(rawJson, "category='", "'"))
                    .append("\",\"businessUnit\":\"" + StringUtils.substringBetween(rawJson, "businessUnit='", "'") + "\"" + " },")
                    .append(CASE_USER_ASSIGNMENT);


        return returnString.toString();

    }








}