package com.example.azuredevopsdashboard.controller;

import com.example.azuredevopsdashboard.exception.UserAlreadyExistsException;
import com.example.azuredevopsdashboard.payload.JwtAuthenticationResponse;
import com.example.azuredevopsdashboard.payload.LoginRequest;
import com.example.azuredevopsdashboard.payload.SignUpRequest;
import com.example.azuredevopsdashboard.security.JwtTokenProvider;
import com.example.azuredevopsdashboard.user.User;
import com.example.azuredevopsdashboard.user.UserService; // Using UserService for user creation
// import com.example.azuredevopsdashboard.user.UserRepository; // Keep if direct access is needed elsewhere
// import org.springframework.security.crypto.password.PasswordEncoder; // No longer needed here if UserService handles encoding

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid; // For validating request bodies
import java.net.URI;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    // private final UserRepository userRepository; // Replaced by userService for signup
    // private final PasswordEncoder passwordEncoder; // Replaced by userService for signup

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider,
                          UserService userService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        try {
            User result = userService.createUser(signUpRequest);

            URI location = ServletUriComponentsBuilder
                    .fromCurrentContextPath().path("/api/users/{username}") // Assuming an endpoint to get user by username
                    .buildAndExpand(result.getUsername()).toUri();

            return ResponseEntity.created(location).body("{\"message\": \"User registered successfully!\"}");
        } catch (UserAlreadyExistsException ex) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST) // Or HttpStatus.CONFLICT
                    .body(String.format("{\"error\": \"%s\"}", ex.getMessage()));
        }
    }
}
