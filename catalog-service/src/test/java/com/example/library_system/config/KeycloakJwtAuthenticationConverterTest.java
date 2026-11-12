package com.example.library_system.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakJwtAuthenticationConverterTest {

    private final KeycloakJwtAuthenticationConverter converter = new KeycloakJwtAuthenticationConverter();

    @Test
    void convert_tokenWithUserAndAdminRoles_returnsPrefixedAuthoritiesAndUsername() {

        Jwt jwt = createJwt(Map.of(
                "preferred_username", "alice",
                "realm_access", Map.of("roles", List.of("USER", "ADMIN"))));

        AbstractAuthenticationToken result = converter.convert(jwt);

        assertThat(result).isInstanceOf(JwtAuthenticationToken.class);
        assertThat(result.isAuthenticated()).isTrue();
        assertThat(result.getName()).isEqualTo("alice");
        assertThat(result.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void convert_tokenWithOnlyUserRole_returnsOnlyRoleUser() {

        Jwt jwt = createJwt(Map.of(
                "preferred_username", "bob",
                "realm_access", Map.of("roles", List.of("USER"))));

        AbstractAuthenticationToken result = converter.convert(jwt);

        assertThat(result.getName()).isEqualTo("bob");
        assertThat(result.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER")
                .doesNotContain("ROLE_ADMIN");
    }


    @Test
    void convert_tokenWithoutRealmAccessClaim_returnsNoAuthorities() {

        Jwt jwt = createJwt(Map.of("preferred_username", "alice"));

        AbstractAuthenticationToken result = converter.convert(jwt);

        assertThat(result.getAuthorities()).isEmpty();
        assertThat(result.getName()).isEqualTo("alice");
    }

    @Test
    void convert_realmAccessWithoutRolesKey_returnsNoAuthorities() {

        Jwt jwt = createJwt(Map.of(
                "preferred_username", "alice",
                "realm_access", Map.of("something_else", "value")));

        AbstractAuthenticationToken result = converter.convert(jwt);

        assertThat(result.getAuthorities()).isEmpty();
    }

    @Test
    void convert_emptyRolesList_returnsNoAuthorities() {

        Jwt jwt = createJwt(Map.of(
                "preferred_username", "alice",
                "realm_access", Map.of("roles", List.of())));

        AbstractAuthenticationToken result = converter.convert(jwt);

        assertThat(result.getAuthorities()).isEmpty();
    }

    private static Jwt createJwt(Map<String, Object> claims) {
        Jwt.Builder builder = Jwt.withTokenValue("test-token")
                .header("kate", "RS256")
                .subject("test-user-id");
        claims.forEach(builder::claim);
        return builder.build();
    }
}