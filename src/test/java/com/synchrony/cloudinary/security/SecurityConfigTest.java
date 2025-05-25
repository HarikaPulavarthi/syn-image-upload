package com.synchrony.cloudinary.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private SecurityFilterChain filterChain;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    private static final String REGISTER_ENDPOINT = "/api/users/v1/register";

    @Test
    void passwordEncoderBeanExists() {
        PasswordEncoder encoder = context.getBean(PasswordEncoder.class);
        assertThat(encoder).isNotNull();
        String encoded = encoder.encode("password");
        assertThat(encoder.matches("password", encoded)).isTrue();
    }

    @Test
    void protectedEndpointRequiresAuth() throws Exception {
        mockMvc.perform(get("/upload"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whenAccessPublicEndpoint_thenShouldAllow() throws Exception {
        String json = """
                {
                    "username": "testuser",
                    "password": "testpass",
                    "name": "Test User",
                    "email": "test@example.com"
                }
                """;
        mockMvc.perform(post("/api/users/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    void contextLoads() {
        assertNotNull(filterChain);
        assertNotNull(passwordEncoder);
    }
}