package com.redhat.rest.example.demorest;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
@PropertySource("classpath:categorization.properties")
@ConfigurationProperties(prefix = "categories")
@Component
public class Categorization {


    private Map<String, String> mapProperty;


    public Map<String, String> getMapProperty() {
        return mapProperty;
    }
    public void setMapProperty(Map<String, String> mapProperty) {
        this.mapProperty = mapProperty;
    }

}