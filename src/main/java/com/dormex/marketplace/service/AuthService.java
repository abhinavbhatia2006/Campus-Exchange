package com.dormex.marketplace.service;

import com.dormex.marketplace.dto.SignupRequest;
import com.dormex.marketplace.dto.VerifyOtpRequest;
import com.dormex.marketplace.model.PendingSignup;
import com.dormex.marketplace.model.Sessions;
import com.dormex.marketplace.model.User;
import com.dormex.marketplace.repository.PendingSignupRepositoryJson;
import com.dormex.marketplace.repository.SessionRepositoryJson;
import com.dormex.marketplace.repository.UserRepositoryJson;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.Optional;
import java.util.UUID;

//@Service annotation is similar to the @Bean annotation, lets spring know to detect it and create 1 instance of it during application startup
//this instance is injected as and where it is needed using dependency injection
@Service
public class AuthService
{
    //java mail sender used for sending OTP via mail
    private final JavaMailSender javaMailSender;

    //json based repositories to store data
    private final PendingSignupRepositoryJson pendingRepo;
    private final UserRepositoryJson userRepo;
    private final SessionRepositoryJson sessionRepo;

    //used to hash and compare passwords
    private final BCryptPasswordEncoder passwordEncoder;

    //logging service used to add logs to the my_dev_logs.txt file
    private final CustomLogger logger;

    //college service used to validate the college names
    private final CollegeService collegeService;

    /**Constructor
     * Used Constructor-based dependency injection
     * Spring automatically provides the objects for all dependencies*/
    public AuthService(JavaMailSender javaMailSender, PendingSignupRepositoryJson pendingRepo, UserRepositoryJson userRepo, BCryptPasswordEncoder passwordEncoder, SessionRepositoryJson sessionRepo, CustomLogger logger, CollegeService collegeService)
    {
        this.javaMailSender = javaMailSender;
        this.pendingRepo = pendingRepo;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.sessionRepo = sessionRepo;
        this.logger = logger;
        this.collegeService = collegeService;
    }

    //generates random 6 digit OTP that does not start with 0
    private String generateOtp()
    {
        return String.format("%06d", (int)(Math.random() * 900000) + 100000);
    }

    /**
     * handles the signup request
     * takes SignupRequest DTO as input
     * validates college
     * ensures users does not already exist
     * generates otp
     * saves the new unverified user to pending_signup Repo and sends OTP via email
     * */
    public String signupRequest(SignupRequest signupRequest) throws Exception
    {
        logger.log("AuthService", "New Signup Request by name: " + signupRequest.getName() + ", email: " + signupRequest.getEmail());

        //validates the college according to the colleges.json
        if(!collegeService.checkListedCollege(signupRequest.getCollege()))
        {
            List<String> allowed = collegeService.getAll();
            throw new Exception("College not supported. Allowed colleges: " + allowed);
        }

        Optional<User> user = userRepo.findByEmail(signupRequest.getEmail());
        if(user.isPresent())
        {
            throw new Exception("User already exists");
        }

        //prevents multiple OTP requests
        if(pendingRepo.findByEmail(signupRequest.getEmail()).isPresent())
        {
            throw new Exception("OTP already sent. Please verify your email");
        }

        //password is hashed before being stored
        String hashedPassword = passwordEncoder.encode(signupRequest.getPassword());

        //OTP generated with 2 minutes validity
        String otp = generateOtp();
        long expiresAt = System.currentTimeMillis() + (2*60*1000);

        //unverified user is saved to the pending_signup.json
        PendingSignup pendingSignup = new PendingSignup(signupRequest.getEmail(), signupRequest.getName(), hashedPassword, signupRequest.getHostelNumber(), signupRequest.getGender(), signupRequest.getCollege(), otp, expiresAt);
        pendingRepo.save(pendingSignup);

        //for testing purposes, uncomment below line to get the otp on the console too.
        //System.out.println("OTP for " + signupRequest.getEmail() + ": " + otp);
        SimpleMailMessage message = getSimpleMailMessage(signupRequest, otp);
        javaMailSender.send(message);
        logger.log("AuthService", "OTP sent to: " + signupRequest.getEmail());
        return "OTP sent successfully, please verify";
    }

    //creates the actual mail message that is sent
    private static SimpleMailMessage getSimpleMailMessage(SignupRequest signupRequest, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("dormex.marketplace@gmail.com");
        message.setTo(signupRequest.getEmail());
        message.setSubject("One Time Password (OTP) for your account on Dormex");
        message.setText("Hi, " + signupRequest.getName() + "\nUse " + otp + " as One Time Password (OTP) to verify your account with Dormex. This OTP is valid for 2 minutes.\nPlease do not share your OTP with anyone.\n\nRegards,\nDormex Team");
        return message;
    }

    /**
     * verifies the OTP
     * first checks if pending signup exists
     * validates the OTP
     * creates a permanent user entry to the users.json
     * deletes from pending_signup.json*/
    public User verifyOtp(VerifyOtpRequest verifyOtpDto) throws Exception
    {
        Optional<PendingSignup> pendingSignup = pendingRepo.findByEmail(verifyOtpDto.getEmail());

        if(pendingSignup.isEmpty())
        {
            throw new Exception("No pending signup found!");
        }

        PendingSignup pending = pendingSignup.get();

        if(!pending.getOtp().equals(verifyOtpDto.getOtp()))
        {
            throw new Exception("Invalid OTP");
        }

        User user = new User(UUID.randomUUID().toString(), pending.getName(), pending.getEmail(), pending.getHashPassword(), pending.getHostelNumber(), pending.getGender(), pending.getCollege(), true);
        userRepo.save(user);
        pendingRepo.deleteByEmail(verifyOtpDto.getEmail());
        logger.log("AuthService", "OTP verified for " + user.getUserId());

        return user;
    }

    /**
     * used to log in a user
     * validates the email and password
     * generates a session token which is stored in session.json on successful validation
     * return the userId and token*/
    public Map<String, String> login(String email, String password) throws Exception {

        Optional<User> userOpt = userRepo.findByEmail(email);
        if (userOpt.isEmpty())
            throw new Exception("User does not exist");

        User user = userOpt.get();

        //hashed password is checked
        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new Exception("Invalid password");

        if (!user.isEnabled())
            throw new Exception("User not verified");

        // Create session token
        String token = UUID.randomUUID().toString();
        long expiresAt = System.currentTimeMillis() + 24 * 60 * 60 * 1000; // 24 hours

        Sessions session = new Sessions(token, user.getUserId(), expiresAt);
        sessionRepo.save(session);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("userId", user.getUserId());
        logger.log("AuthService", "Login successful for " + user.getUserId());

        return response;
    }

    /**
     * public function used by all protected api endpoints, the ones that require userId
     * checks if token exists and is valid, not expired
     * if valid, return the userId of the logged in user*/
    public String verifySession(String token) throws Exception
    {

        Optional<Sessions> sessionOptional = sessionRepo.findByToken(token);

        if (sessionOptional.isEmpty())
            throw new Exception("Invalid or missing token");

        Sessions session = sessionOptional.get();

        //deletes the session if expired
        if (session.getExpiresAt() < System.currentTimeMillis()) {
            sessionRepo.deleteToken(token);
            throw new Exception("Session expired. Please login again.");
        }

        return session.getUserId();  //return logged-in user
    }

    //logout function, deletes the session entry from the sessions.json
    public String logout(String token) throws Exception {
        Optional<Sessions> sessionOpt = sessionRepo.findByToken(token);

        if (sessionOpt.isEmpty()) {
            throw new Exception("Invalid token or already logged out.");
        }

        sessionRepo.deleteToken(token);
        return "Logged out successfully.";
    }
}
