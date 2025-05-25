package com.synchrony.cloudinary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synchrony.cloudinary.dto.LoginRequest;
import com.synchrony.cloudinary.entity.User;
import com.synchrony.cloudinary.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Mock
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void register_whenValidUser_shouldReturnCreated() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("test@example.com");
        user.setName("Test User");

        when(userService.findByUsername("testuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        ResponseEntity<User> response = userController.register(user);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("testuser", response.getBody().getUsername());
        verify(userService).registerUser(any(User.class));
    }

    @Test
    void register_whenUsernameOrPasswordMissing_shouldReturnBadRequest() {
        User user = new User();
        user.setUsername(null);
        user.setPassword(null);

        ResponseEntity<User> response = userController.register(user);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void register_whenUsernameExists_shouldReturnConflict() {
        User user = new User();
        user.setUsername("existinguser");
        user.setPassword("password");
        user.setEmail("email@example.com");
        user.setName("Existing User");

        when(userService.findByUsername("existinguser")).thenReturn(Optional.of(new User()));

        ResponseEntity<User> response = userController.register(user);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void register_whenNameOrEmailMissing_shouldReturnBadRequest() {
        User user = new User();
        user.setUsername("newuser");
        user.setPassword("password");
        user.setEmail(null); // missing email
        user.setName(null);  // missing name

        when(userService.findByUsername("newuser")).thenReturn(Optional.empty());

        ResponseEntity<User> response = userController.register(user);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getUser_whenUserIsValid_shouldReturnUser() {
        String username = "testuser";
        Principal principal = () -> "testuser";
        User user = new User();
        user.setUsername(username);
        user.setEmail("test@example.com");

        when(userService.findByUsername(username)).thenReturn(Optional.of(user));

        ResponseEntity<User> response = userController.getUser(username, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(username, response.getBody().getUsername());
    }

    @Test
    void getUser_whenPrincipalIsNull_shouldReturnUnauthorized() {
        ResponseEntity<User> response = userController.getUser("testuser", null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getUser_whenUsernameIsEmpty_shouldReturnBadRequest() {
        Principal principal = () -> "testuser";

        ResponseEntity<User> response = userController.getUser("", principal);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getUser_whenUsernameDoesNotMatchPrincipal_shouldReturnForbidden() {
        Principal principal = () -> "anotheruser";

        ResponseEntity<User> response = userController.getUser("testuser", principal);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getUser_whenUserNotFound_shouldReturnNotFound() {
        String username = "missinguser";
        Principal principal = () -> "missinguser";

        when(userService.findByUsername(username)).thenReturn(Optional.empty());

        ResponseEntity<User> response = userController.getUser(username, principal);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void login_shouldReturnBadRequest_whenUsernameIsEmpty() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("");
        loginRequest.setPassword("password");

        ResponseEntity<?> response = userController.login(loginRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Username is empty", response.getBody());
    }

    @Test
    void login_shouldReturnBadRequest_whenPasswordIsEmpty() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("");

        ResponseEntity<?> response = userController.login(loginRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Password is empty", response.getBody());
    }

    @Test
    void login_shouldReturnNotFound_whenUserDoesNotExist() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        when(userService.findByUsername("testuser")).thenReturn(Optional.empty());

        ResponseEntity<?> response = userController.login(loginRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody());
    }

    @Test
    void login_shouldReturnUnauthorized_whenPasswordIsInvalid() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpass");

        User user = new User();
        user.setUsername("testuser");
        user.setPassword("hashedPassword");

        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpass", "hashedPassword")).thenReturn(false);

        ResponseEntity<?> response = userController.login(loginRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid password", response.getBody());
    }

    @Test
    void login_shouldReturnOk_whenCredentialsAreValid() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("correctpass");

        User user = new User();
        user.setUsername("testuser");
        user.setPassword("hashedPassword");

        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctpass", "hashedPassword")).thenReturn(true);

        ResponseEntity<?> response = userController.login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user, response.getBody());
    }
}