package com.rbkmoney.orgmanager.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class KeycloakOpenIdStub {

    private final String keycloakRealm;
    private final String issuer;
    private final String openidConfig;
    private final JwtTokenBuilder jwtTokenBuilder;

    public KeycloakOpenIdStub(String keycloakAuthServerUrl, String keycloakRealm, JwtTokenBuilder jwtTokenBuilder) {
        this.keycloakRealm = keycloakRealm;
        this.jwtTokenBuilder = jwtTokenBuilder;
        this.issuer = keycloakAuthServerUrl + "/realms/" + keycloakRealm;
        this.openidConfig = "{\n" +
                "  \"issuer\": \"" + issuer + "\",\n" +
                "  \"authorization_endpoint\": \"" + issuer + "/protocol/openid-connect/auth\",\n" +
                "  \"token_endpoint\": \"" + issuer + "/protocol/openid-connect/token\",\n" +
                "  \"token_introspection_endpoint\": \"" + issuer + "/protocol/openid-connect/token/introspect\",\n" +
                "  \"userinfo_endpoint\": \"" + issuer + "/protocol/openid-connect/userinfo\",\n" +
                "  \"end_session_endpoint\": \"" + issuer + "/protocol/openid-connect/logout\",\n" +
                "  \"jwks_uri\": \"" + issuer + "/protocol/openid-connect/certs\",\n" +
                "  \"check_session_iframe\": \"" + issuer + "/protocol/openid-connect/login-status-iframe.html\",\n" +
                "  \"registration_endpoint\": \"" + issuer + "/clients-registrations/openid-connect\",\n" +
                "  \"introspection_endpoint\": \"" + issuer + "/protocol/openid-connect/token/introspect\"\n" +
                "}";
    }

    public void givenStub() {
        stubFor(get(urlEqualTo(String.format("/auth/realms/%s/.well-known/openid-configuration", keycloakRealm)))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(openidConfig)
                )
        );
    }

    public String generateJwt(String... roles) {
        return jwtTokenBuilder.generateJwtWithRoles(issuer, roles);
    }

    public String generateJwt(long iat, long exp, String... roles) {
        return jwtTokenBuilder.generateJwtWithRoles(iat, exp, issuer, roles);
    }

    public String getUserId() {
        return jwtTokenBuilder.getUserId();
    }
}
