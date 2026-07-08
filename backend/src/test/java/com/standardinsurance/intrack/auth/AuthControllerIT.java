package com.standardinsurance.intrack.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.standardinsurance.intrack.auth.dto.LoginRequestDto;
import com.standardinsurance.intrack.auth.dto.RegisterRequestDto;
import com.standardinsurance.intrack.support.AbstractIntegrationTest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * End-to-end auth flow against a real Postgres: register issues a session cookie, and that cookie
 * authenticates a call to /me. Also checks the login failure path.
 */
@AutoConfigureMockMvc
@Transactional
class AuthControllerIT extends AbstractIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void registerThenAccessMeWithSessionCookie() throws Exception {
        var register = new RegisterRequestDto("Ada", "ada@intrack.local", "password1");

        MockHttpServletResponse response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ada@intrack.local"))
                .andExpect(jsonPath("$.role").value("DEVELOPER"))
                .andReturn().getResponse();

        Cookie accessCookie = accessCookie(response);
        assertThat(accessCookie).isNotNull();

        mockMvc.perform(get("/api/v1/auth/me").cookie(accessCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ada@intrack.local"));
    }

    @Test
    void meRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithWrongPasswordIsUnauthorized() throws Exception {
        var register = new RegisterRequestDto("Bo", "bo@intrack.local", "password1");
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk());

        var badLogin = new LoginRequestDto("bo@intrack.local", "wrong-password");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("BAD_CREDENTIALS"));
    }

    /** Builds a jakarta Cookie from the Set-Cookie header (robust across mock response versions). */
    private static Cookie accessCookie(MockHttpServletResponse response) {
        for (String header : response.getHeaders("Set-Cookie")) {
            if (header.startsWith(JwtCookieNames.ACCESS + "=")) {
                String value = header.substring((JwtCookieNames.ACCESS + "=").length(),
                        header.indexOf(';'));
                return new Cookie(JwtCookieNames.ACCESS, value);
            }
        }
        return null;
    }

    private static final class JwtCookieNames {
        static final String ACCESS = "access_token";
    }
}
