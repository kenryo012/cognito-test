package com.example.cognitotest.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClientBuilder;

@Configuration
public class CognitoClientConfig {

    private final String region;
    private final String endpointOverride;

    public CognitoClientConfig(@Value("${app.aws.region}") String region,
                               @Value("${app.aws.cognito-endpoint:}") String endpointOverride) {
        this.region = region;
        this.endpointOverride = endpointOverride;
    }

    @Bean
    public CognitoIdentityProviderClient cognitoIdentityProviderClient() {
        CognitoIdentityProviderClientBuilder builder = CognitoIdentityProviderClient.builder()
                .region(Region.of(region));

        if (StringUtils.hasText(endpointOverride)) {
            builder = builder.endpointOverride(URI.create(endpointOverride));
        }

        return builder.build();
    }
}
