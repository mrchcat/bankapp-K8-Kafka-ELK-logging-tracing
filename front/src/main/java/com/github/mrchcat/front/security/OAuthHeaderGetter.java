package com.github.mrchcat.front.security;

import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuthHeaderGetter {
    private final OAuth2AuthorizedClientManager defaultAuthorizedClientManager;

    private final String CLIENT_REGISTRATION_ID = "bank_front";


    public OAuthHeader getOAuthHeader() throws AuthException {
        return getOAuthHeader(defaultAuthorizedClientManager);
    }


    public OAuthHeader getOAuthHeader(OAuth2AuthorizedClientManager specialAuthorizedClientManager) throws AuthException {
        var token = specialAuthorizedClientManager.authorize(OAuth2AuthorizeRequest
                .withClientRegistrationId(CLIENT_REGISTRATION_ID)
                .principal("system")
                .build());
        if (token == null) {
            throw new AuthException("OAuth token is absent");
        }
        return new OAuthHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token.getAccessToken().getTokenValue());
    }
}
