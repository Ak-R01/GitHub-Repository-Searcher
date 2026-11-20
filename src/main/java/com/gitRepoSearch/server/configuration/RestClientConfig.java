package com.gitRepoSearch.server.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${github.url}")
    private String url;

    @Bean
    public RestClient externalRestClient(){
        return RestClient.builder().baseUrl(this.url).build();
    }
}
