package com.dormex.marketplace.controller;

import com.dormex.marketplace.dto.SignupRequest;
import com.dormex.marketplace.dto.VerifyOtpRequest;
import com.dormex.marketplace.model.User;
import com.dormex.marketplace.service.AuthService;
import com.dormex.marketplace.service.CustomLogger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
//base path for all the authentication endpoints
@RequestMapping("/api/auth")
public class AuthController
{
    private final AuthService authService;
    private final CustomLogger logger;

    //constructor injection to provide the necessary beans
    public AuthController(AuthService authService, CustomLogger logger)
    {

        this.authService = authService;
        this.logger = logger;
    }

    /**Endpoint for creating a new signup request
     * Body- SignupRequest DTO is taken as input
     * that request dto is passed to the signupRequest method form AuthService which initiates the signup process*/
    @PostMapping("/signup-request")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request)
    {
        try
        {
            String message = authService.signupRequest(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        }
        catch (Exception e)
        {
            logger.log("AuthService", "Error in sending signup request");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Endpoint for verifying otp
     * Takes VerifyOtpRequest DTO as input and passes it to the verifyOtp method from AuthService*/
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request)
    {
        try
        {
            User user = authService.verifyOtp(request);
            return ResponseEntity.ok(user);
        }
        catch (Exception e)
        {
            logger.log("AuthService", "Error in verifying otp");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * login endpoint
     * if correct password, server returns the session token*/
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password)
    {
        try
        {
            Map<String, String> sessionData = authService.login(email, password);
            return ResponseEntity.ok(sessionData);
        }
        catch (Exception e)
        {
            logger.log("AuthService", "Error in login");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    /**
     * logout endpoint
     * session entry is deleted*/
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Auth-Token") String token) {
        try {
            String message = authService.logout(token);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            logger.log("AuthService", "Error in logout");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
