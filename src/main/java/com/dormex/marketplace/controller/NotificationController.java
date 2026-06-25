package com.dormex.marketplace.controller;

import com.dormex.marketplace.model.Notification;
import com.dormex.marketplace.service.AuthService;
import com.dormex.marketplace.service.CustomLogger;
import com.dormex.marketplace.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthService authService;
    private final CustomLogger logger;

    // Constructor-based dependency injection including AuthService
    public NotificationController(NotificationService notificationService, AuthService authService, CustomLogger logger) {
        this.notificationService = notificationService;
        this.authService = authService;
        this.logger = logger;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getNotificationsForUser(
            @PathVariable String userId,
            @RequestHeader("Auth-Token") String token) {
        try {
            // This verifies Session via AuthService
            // This will throw an Exception if the token is invalid or expired.
            String loggedInUserId = authService.verifySession(token);

            // Ensure user can only view their own notifications
            if (!loggedInUserId.equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied: You can only view your own notifications.");
            }

            // fetches notifications
            List<Notification> notifications = notificationService.showAllNotification(userId);
            return ResponseEntity.ok(notifications);

        } catch (Exception e) {
            logger.log("AuthService", "Error in showing notifications");
            // Handle Auth errors (thrown by AuthService)
            String msg = e.getMessage();
            if (msg != null && (msg.contains("Session") || msg.contains("token") || msg.contains("Invalid"))) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(msg);
            }

            // Fallback for IO or other unexpected errors
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching notifications: " + e.getMessage());
        }
    }
}
