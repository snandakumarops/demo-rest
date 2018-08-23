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





    public String transformOnlineResponse(String reqJson) {
        Map<String,String> categorization = calculateCategoryBusinessUnit(reqJson);
        String rawJson = StringUtils.chop(reqJson);
        String returnString = null;
        for(String key:categorization.keySet()) {
            returnString = "{\"case-data\" :  " + rawJson + ",\"category\":\"" + key + "\",\"businessUnit\":\""+ categorization.get(key)
                    + "\"} ," +
                    "\"case-user-assignments\" : {\n" +
                    "  \t\"admin\" : \"pamAdmin\"\n" +
                    "  } }";
            System.out.println("retString" + returnString);
        }
        return returnString;
    }

    public Map<String,String> calculateCategoryBusinessUnit(String reqJson) {

        String category = "Other";
        String businessUnit = "OtherBU";
        String complaintsDescription = StringUtils.substringBetween(reqJson,"complaintsDescription\":\"","\"}");
        System.out.print("complaintsDescription"+complaintsDescription);

        Map<String,String> properties = categorization.getMapProperty();

        System.out.print(properties.keySet().toString());


        for(String key:properties.keySet()) {
            List<String> list = Arrays.asList(properties.get(key).split(","));
            boolean match = list.stream().anyMatch(s -> complaintsDescription.contains(s));
            System.out.println(match);
            if(match) {
                category = key;
                businessUnit = key + "BU";
            }
        }


        Map<String,String> categoryBusinessUnit = new HashMap<>();
        categoryBusinessUnit.put(category,businessUnit);
        System.out.println(categoryBusinessUnit.toString());
        return categoryBusinessUnit;


    }

    public String transformBranchBanking(String reqJson) {
        String businessUnit = StringUtils.substringBetween(reqJson,"category\":\"","\",")+"BU";
        String rawJson = StringUtils.chop(reqJson);
        rawJson= rawJson + ",\""+lookUpUserDetails(StringUtils.substringBetween(reqJson,"customerAccNo\":\"","\","));
        String returnString = null;

        returnString = "{\"case-data\" :  " + rawJson + ",\"businessUnit\":\""+ businessUnit
                    + "\"} ," +
                    "\"case-user-assignments\" : {\n" +
                    "  \t\"admin\" : \"pamAdmin\"\n" +
                    "  } }";
        System.out.println("retString" + returnString);

        return returnString;
    }

    public String lookUpUserDetails(String customerNumber) {
        String returnString = "";
        //logic to pull customer details based on customer Number
        if(customerDetails.getCustomerNo().equals(customerNumber)) {
           String[] customerDet= customerDetails.getCustDetails().split(",");
           returnString+="customerName\":\""+customerDet[0] +
                   "\",\"customerPhone\":\""+customerDet[1] +
                   "\",\"customerAddress\":\""+customerDet[2]+"\"";
        }
        System.out.println(returnString+"+++");
        return returnString;

    }






}
