package com.example.library_system.web;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CurrentUserArgumentResolverTest {

    private final CurrentUserArgumentResolver resolver = new CurrentUserArgumentResolver();

    @Test
    void supportsParameter_annotatedAuthenticatedUserParameter_returnsTrue() throws NoSuchMethodException {

        MethodParameter parameter = parameterOf("annotatedUser", AuthenticatedUser.class);

        boolean supported = resolver.supportsParameter(parameter);

        assertThat(supported).isTrue();
    }

    @Test
    void supportsParameter_parameterWithoutAnnotation_returnsFalse() throws NoSuchMethodException {

        MethodParameter parameter = parameterOf("notAnnotatedUser", AuthenticatedUser.class);

        boolean supported = resolver.supportsParameter(parameter);

        assertThat(supported).isFalse();
    }

    @Test
    void supportsParameter_annotatedParameterOfWrongType_returnsFalse() throws NoSuchMethodException {

        MethodParameter parameter = parameterOf("annotatedString", String.class);

        boolean supported = resolver.supportsParameter(parameter);

        assertThat(supported).isFalse();
    }


    @Test
    void resolveArgument_authenticatedUserWithRoles_returnsUsernameAndRolesWithoutPrefix() throws Exception {

        Authentication alice = UsernamePasswordAuthenticationToken.authenticated("alice", null, List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("SCOPE_profile")));
        NativeWebRequest request = requestWithPrincipal(alice);

        Object result = resolver.resolveArgument(
                parameterOf("annotatedUser", AuthenticatedUser.class), null, request, null);

        assertThat(result).isInstanceOf(AuthenticatedUser.class);
        AuthenticatedUser user = (AuthenticatedUser) result;
        assertThat(user.username()).isEqualTo("alice");
        assertThat(user.roles()).containsExactly("ADMIN", "USER");
    }

    @Test
    void resolveArgument_authenticatedUserWithoutRoles_returnsEmptyRoles() throws Exception {

        Authentication guest = UsernamePasswordAuthenticationToken.authenticated("guest", null, List.of(
                new SimpleGrantedAuthority("SCOPE_profile")));
        NativeWebRequest request = requestWithPrincipal(guest);

        AuthenticatedUser user = resolver.resolveArgument(
                parameterOf("annotatedUser", AuthenticatedUser.class), null, request, null);

        assertThat(user.username()).isEqualTo("guest");
        assertThat(user.roles()).isEmpty();
    }

    @Test
    void resolveArgument_requestWithoutPrincipal_throwsUnauthorized() throws Exception {

        NativeWebRequest request = new ServletWebRequest(new MockHttpServletRequest());
        MethodParameter parameter = parameterOf("annotatedUser", AuthenticatedUser.class);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> resolver.resolveArgument(parameter, null, request, null));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    private static MethodParameter parameterOf(String methodName, Class<?> parameterType) throws NoSuchMethodException {
        return new MethodParameter(SampleHandler.class.getDeclaredMethod(methodName, parameterType), 0);
    }

    private static NativeWebRequest requestWithPrincipal(Authentication authentication) {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setUserPrincipal(authentication);
        return new ServletWebRequest(servletRequest);
    }

    @SuppressWarnings("unused")
    static class SampleHandler {

        void annotatedUser(@CurrentUser AuthenticatedUser user) {
        }

        void notAnnotatedUser(AuthenticatedUser user) {
        }

        void annotatedString(@CurrentUser String username) {
        }
    }
}