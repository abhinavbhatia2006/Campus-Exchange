package com.dormex.marketplace.controller;

import com.dormex.marketplace.dto.ClaimRequest;
import com.dormex.marketplace.model.Claim;
import com.dormex.marketplace.service.AuthService;
import com.dormex.marketplace.service.ClaimService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    private final ClaimService claimService;
    private final AuthService authService;
    public ClaimController(ClaimService claimService, AuthService authService) {
        this.claimService = claimService;
        this.authService = authService;
    }

    /**
     * Create a new claim
     * Body: ClaimRequest { itemId, claimerId, listerId (optional) }
     */
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createClaim(@RequestBody ClaimRequest request, @RequestHeader ("Auth-Token") String token) {
        try {
            String ClaimerId = authService.verifySession(token);
            if (request.getItemId() == null || request.getItemId().isBlank())
                return ResponseEntity.badRequest().body("itemId is required");
            Claim created = claimService.createClaim(request.getItemId(), ClaimerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + ex.getMessage());
        }
    }

    /**
     * Accept a claim.
     * Body: Claim (the service accepts a Claim object)
     */
    @PutMapping(path = "/accept", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> acceptClaim(@RequestBody Claim claim, @RequestHeader ("Auth-Token") String token) {
        try {
            String UserId = authService.verifySession(token);
            if(!UserId.equals(claim.getListerId())){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only Lister can accept the claim.");
            }
            Claim accepted = claimService.acceptClaim(claim);
            return ResponseEntity.ok(accepted);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + ex.getMessage());
        }
    }

    /**
     * Reject a claim.
     * Body: Claim
     */
    @PutMapping(path = "/reject", consumes = "application/json")
    public ResponseEntity<?> rejectClaim(@RequestBody Claim claim, @RequestHeader ("Auth-Token") String token) {
        try {
            String UserId = authService.verifySession(token);
            if(!UserId.equals(claim.getListerId())){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only Lister can accept the claim.");
            }
            Claim rejected = claimService.rejectClaim(claim);
            return ResponseEntity.ok(rejected);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + ex.getMessage());
        }
    }

    /**
     * Relist a claim / item after failed offline deal (blocks the claimer for 7 days).
     * Body: Claim
     */
    @PutMapping(path = "/relist", consumes = "application/json")
    public ResponseEntity<?> relistItem(@RequestBody Claim claim) {
        try {
            claimService.relistItem(claim);
            return ResponseEntity.ok("Item relisted and claimer blocked.");
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + ex.getMessage());
        }
    }

    /**
     * Owner marks a waiting item as completed.
     * Path: /api/claims/item/{itemId}/complete
     */
    @PutMapping(path = "/item/{itemId}/complete", produces = "application/json")
    public ResponseEntity<?> completeDeal(@PathVariable("itemId") String itemId) {
        try {
            claimService.completeDeal(itemId);
            return ResponseEntity.ok("Item marked as completed.");
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (IllegalStateException | IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + ex.getMessage());
        }
    }
}
