package com.demo.controller;

import com.demo.advice.GlobalExceptionHandler;
import com.demo.dto.AuthResponse;
import com.demo.dto.LoginRequest;
import com.demo.dto.RegisterRequest;
import com.demo.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    @BeforeEach
    void setUp() {
        AuthController controller = new AuthController(authService);
        // enable validation and exception handler
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void register_shouldReturnCreated_andAuthResponse() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setName("John Doe");
        req.setUsername("johndoe");
        req.setPassword("password123");

        AuthResponse resp = AuthResponse.builder()
                .token("fake-token")
                .username("johndoe")
                .role("ROLE_USER")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.results.token").value("fake-token"))
                .andExpect(jsonPath("$.results.username").value("johndoe"));
    }

    @Test
    void login_shouldReturnOk_andAuthResponse() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("johndoe");
        req.setPassword("password123");

        AuthResponse resp = AuthResponse.builder()
                .token("login-token")
                .username("johndoe")
                .role("ROLE_USER")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.results.token").value("login-token"))
                .andExpect(jsonPath("$.results.username").value("johndoe"));
    }

    @Test
    void register_withInvalidPayload_shouldReturnBadRequest_andValidationErrors() throws Exception {
        // missing username and password
        RegisterRequest req = new RegisterRequest();
        req.setName("");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.results.name").exists());
    }
}

