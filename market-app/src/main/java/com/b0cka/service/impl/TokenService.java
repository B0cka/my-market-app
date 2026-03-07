package com.b0cka.service.impl;

import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TokenService {

    private final ReactiveOAuth2AuthorizedClientManager authorizedClientManager;

    public TokenService(ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
        this.authorizedClientManager = authorizedClientManager;
    }

    public Mono<String> getAccessToken() {

        OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest
                .withClientRegistrationId("my-client")
                .principal("my-client")
                .build();

        return authorizedClientManager.authorize(request)
                .map(OAuth2AuthorizedClient::getAccessToken)
                .map(token -> token.getTokenValue());
    }
}