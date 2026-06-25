package com.dormex.marketplace.service;

import com.dormex.marketplace.model.BlockEntry;
import com.dormex.marketplace.model.Claim;
import com.dormex.marketplace.model.Item;
import com.dormex.marketplace.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ClaimService {
    //define your Json files
    private final ClaimRepositoryJson claimRepositoryJson;
    private final ItemRepositoryJson itemRepositoryJson;
    private final BlockRepositoryJson blockRepositoryJson;
    private final ItemService itemService;
    private final CustomLogger logger;
    private final NotificationService notificationService;
    //define your constructors
    public ClaimService(ClaimRepositoryJson claimRepositoryJson, ItemRepositoryJson itemRepositoryJson, BlockRepositoryJson blockRepositoryJson, ItemService itemService, CustomLogger logger, NotificationService notificationService) {
        this.claimRepositoryJson = claimRepositoryJson;
        this.itemRepositoryJson = itemRepositoryJson;
        this.blockRepositoryJson = blockRepositoryJson;
        this.itemService = itemService;
        this.logger = logger;
        this.notificationService = notificationService;
    }
    public Claim createClaim(String itemID,String userID) throws Exception {
        logger.log("ClaimService", "Claim initiated for itemID " + itemID + " by userID " + userID);
        blockRepositoryJson.removeExpiredUsers();
         //now we will check if the user is claiming the item listed by him or not
        Optional<Item> itemOptional = itemRepositoryJson.findById(itemID);
        if(itemOptional.isEmpty()){
            throw new NoSuchElementException("Item not found!");
        }
        Item item = itemOptional.get();
        //now we will check if the user is blacklisted from buying that item or not
        Optional<BlockEntry> blockEntryOptional = blockRepositoryJson.findByItemAndUser(itemID,userID);

        if(blockEntryOptional.isPresent()){
            throw new Exception("User " + userID + " is blocked for purchasing " + itemID);
        }

        //now we have to check if the person who claimed it is the owner or not
        if(item.getListerId().equals(userID)){
            throw new Exception("User cannot claim the item listed by themselves.");
        }

        //now we have verified all the conditions and are ready to request to claim that particular item

        Claim claim  = new Claim(UUID.randomUUID().toString(),itemID,userID,item.getListerId(),"WAITING",System.currentTimeMillis());
        itemService.updateStatus("PENDING",itemID);
        claimRepositoryJson.save(claim);
        /*
        * here we will place the code for notification
        * */

        notificationService.NotifyClaimCreated(itemID,claim.getListerId(),claim.getClaimerId(),item.getTitle());
        logger.log("ClaimService", "Claim created for itemID " + itemID);
        return claim;
    }

    public Claim acceptClaim(Claim claim) throws Exception{
        //claim is accepted and all other claims are rejected
        String itemID = claim.getItemId();
        Optional<Claim>claimOptional = claimRepositoryJson.findByItemId(itemID);
        if(claimOptional.isEmpty()){
            throw new Exception("Claim not found.");
        }
        Claim claim1 = claimOptional.get();
        Optional<Item> optionalItem = itemRepositoryJson.findById(itemID);
        if(optionalItem.isEmpty()){
            throw new Exception("Item is not listed");
        }
        itemService.updateStatus("PENDING",itemID);
        List<Claim> claimList = claimRepositoryJson.findAll();
        for(Claim claim2:claimList) {
            if(Objects.equals(claim2.getClaimId(), claim1.getClaimId())) {
                //make it accepted only for that particular claim accepted by the user
                claim1.setStatus("ACCEPTED");
                claim2.setStatus("ACCEPTED");
                claimRepositoryJson.updateClaimList(claim2);
            } else if(claim2.getItemId().equals(itemID)) {
                    //make rest of the claims rejected by default
                rejectClaim(claim2);
            }
        }
        Optional<Item> itemOptional = itemRepositoryJson.findById(itemID);
        if(itemOptional.isEmpty()){
            throw new NoSuchElementException("Item not found!");
        }
        Item item = itemOptional.get();
        //send notification to  both the user and receiver about the status of their requests
        notificationService.notifyClaimAccepted(itemID,claim.getListerId(),claim.getClaimerId(),item.getTitle());
        logger.log("ClaimService", "Claim accepted for itemID " + itemID);
        return claim1;
    }
    public Claim rejectClaim(Claim claim) throws Exception{
        //lister has the option to reject a particular claim
        String itemID = claim.getItemId();
        Optional<Claim>claimOptional = claimRepositoryJson.findByItemId(itemID);
        if(claimOptional.isEmpty()){
            throw new Exception("Claim not found.");
        }
        Claim claim1 = claimOptional.get();
        claim1.setStatus("REJECTED");
        claimRepositoryJson.updateClaimList(claim1);
        claimRepositoryJson.deleteByID(itemID);
        Optional<Item> itemOptional = itemRepositoryJson.findById(itemID);
        if(itemOptional.isEmpty()){
            throw new NoSuchElementException("Item not found!");
        }
        Item item = itemOptional.get();
        //send the notifications to both the claimer and lister about the status of their requests
        notificationService.notifyClaimRejected(itemID,claim.getListerId(),claim.getClaimerId(),item.getTitle());
        logger.log("ClaimService", "Claim rejected for itemID " + itemID);
        return claim1;
    }

    public void relistItem(Claim claim) throws Exception{
        //item will be relisted if the trade cannot happen under some circumstances
        String itemID = claim.getItemId();
        Optional<Item> OptionalItem = itemRepositoryJson.findById(itemID);
        if(OptionalItem.isEmpty()){
            throw new Exception("Item not found.");
        }
        itemService.updateStatus("LISTED",itemID);
        /*
        * block the user due to which lister had to relist the item
        * */
        long SEVEN_DAYS_TIME = 7L * 24 * 60 * 60 * 1000;
        long timeBlockedUntil = SEVEN_DAYS_TIME + System.currentTimeMillis();
        BlockEntry userBlocked = new BlockEntry(itemID,claim.getClaimerId(),timeBlockedUntil);
        blockRepositoryJson.addBlockedUser(userBlocked);
        logger.log("ClaimService", "Item relisted for itemID " + itemID);
    }
    public void completeDeal(String itemID) throws Exception{
        //deal is completed only after the item is physically exchanged by the both the parties
        Optional<Item> optionalItem = itemRepositoryJson.findById(itemID);
        if(optionalItem.isEmpty()){
            throw new Exception("Item not found");
        }
        itemService.updateStatus("CLAIMED",itemID);
        logger.log("ClaimService", "Claim completed for itemID " + itemID);
    }
    public List<Claim> getAllClaims(String listerID){
        //get all the claims for a particular lister
        List<Claim> claimList = claimRepositoryJson.findAll();
        List<Claim> claimList1 = new ArrayList<>();
        for(Claim claim:claimList){
            if(claim.getListerId().equals(listerID)){
                claimList1.add(claim);
            }
        }
        return claimList1;
    }
}
