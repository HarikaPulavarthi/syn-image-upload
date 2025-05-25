package com.synchrony.cloudinary.controller;


import com.synchrony.cloudinary.dto.LoginRequest;
import com.synchrony.cloudinary.entity.User;
import com.synchrony.cloudinary.service.UserService;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@RestController
@Schema(description = "User API")
@RequestMapping("/api/users/v1")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User Registered"),
            @ApiResponse(responseCode = "409", description = "User already exists"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
    })
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        if (user.getUsername() == null || user.getPassword() == null) {
            log.info("Username or password is empty");
            return ResponseEntity.badRequest().body(null);
        }
        if (userService.findByUsername(user.getUsername()).isPresent()) {
            log.info("Username already exists");
            return ResponseEntity.status(409).body(null);
        }
        if (user.getName() == null || user.getEmail() == null) {
            log.info("Name or email is empty");
            return ResponseEntity.badRequest().body(null);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        log.info("Registering user: {}", user.getUsername());
        log.info("Encoded password: {}", user.getPassword());
        userService.registerUser(user);
        return ResponseEntity.status(201).body(user);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found"),
    })
    @GetMapping("/{username}")
    public ResponseEntity<User> getUser(@PathVariable String username, Principal principal) {
        if (principal == null) {
            log.info("User is not authenticated");
            return ResponseEntity.status(401).body(null);
        }
        if (username == null || username.isEmpty()) {
            log.info("Username is empty");
            return ResponseEntity.badRequest().body(null);
        }
        if (!principal.getName().equals(username)) {
            log.info("User not authorized to access this profile");
            return ResponseEntity.status(403).body(null);
        }
        log.info("Fetching user: {}", username);
        User user = userService.findByUsername(username).orElse(null);
        if (user == null) {
            log.info("User not found");
            return ResponseEntity.status(404).body(null);
        }
        return ResponseEntity.ok(user);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated"),
            @ApiResponse(responseCode = "404", description = "User not found"),
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        if (username == null || username.isEmpty()) {
            log.info("Username is empty");
            return ResponseEntity.badRequest().body("Username is empty");
        }
        if (password == null || password.isEmpty()) {
            log.info("Password is empty");
            return ResponseEntity.badRequest().body("Password is empty");
        }
        log.info("Logging in user: {}", username);
        User user = userService.findByUsername(username).orElse(null);
        if (user == null) {
            log.info("User not found");
            return ResponseEntity.status(404).body("User not found");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.info("Invalid password");
            return ResponseEntity.status(401).body("Invalid password");
        }
        log.info("User logged in successfully: {}", username);
        return ResponseEntity.ok(user);
    }



}

