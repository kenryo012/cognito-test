package com.example.cognitotest.service;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserResponse;

@Service
public class CognitoUserService {

    private final CognitoIdentityProviderClient cognitoIdentityProviderClient;
    private final OAuth2AuthorizedClientService authorizedClientService;

    public CognitoUserService(CognitoIdentityProviderClient cognitoIdentityProviderClient,
                              OAuth2AuthorizedClientService authorizedClientService) {
        this.cognitoIdentityProviderClient = cognitoIdentityProviderClient;
        this.authorizedClientService = authorizedClientService;
    }

    public Optional<Map<String, String>> getUserAttributes(Authentication authentication) {
        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            return Optional.empty();
        }

        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                oauthToken.getAuthorizedClientRegistrationId(),
                oauthToken.getName()
        );

        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            return Optional.empty();
        }

        GetUserResponse response = cognitoIdentityProviderClient.getUser(
                GetUserRequest.builder()
                        .accessToken(authorizedClient.getAccessToken().getTokenValue())
                        .build()
        );

        return Optional.of(
                response.userAttributes()
                        .stream()
                        .collect(Collectors.toMap(AttributeType::name, AttributeType::value))
        );
    }
}

