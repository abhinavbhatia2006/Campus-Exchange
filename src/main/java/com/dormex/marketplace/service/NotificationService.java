package com.dormex.marketplace.service;

import com.dormex.marketplace.model.Notification;
import com.dormex.marketplace.model.User;
import com.dormex.marketplace.repository.NotificationRepositoryJson;
import com.dormex.marketplace.repository.UserRepositoryJson;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * This service handles all in-app notifications and alerts.
 *  Functions performed:
 *  It generates notification messages for various events (Claim requests, Acceptances, Rejections).
 *  It fetches User details (Name, Hostel, Email) to construct the messages to be sent.
 *  It saves new notifications to the JSON file notifications.json.
 *  It searches for notifications for a particular user and marks them as 'read'.
 *  Dependencies:
 *  It uses UserRepositoryJson to get user contact info.
 *  It uses NotificationRepositoryJson to store the actual notifications.
 *  It uses CustomLogger to log the functions called by controller.
 */

@Service
public class NotificationService {
    NotificationRepositoryJson notificationJson;
    private CustomLogger logger;
    UserRepositoryJson userJson;

    public NotificationService(NotificationRepositoryJson notificationJson, UserRepositoryJson userJson, CustomLogger logger) {
        this.notificationJson = notificationJson;
        this.userJson = userJson;
        this.logger = logger;
    }

    public void NotifyClaimCreated(String itemId,String listerId,String claimerId,String itemTitle) throws IOException {
        logger.log("NotificationService", "NotifyClaimCreated for itemID " + itemId);
        Optional<User> lister = userJson.findById(listerId);
        Optional<User> claimer = userJson.findById(claimerId);
        if(lister.isEmpty() || claimer.isEmpty()){
            return;
        }
        String messageToLister = claimer.get().getName() + " has requested to claim your item" + itemTitle + ".";
        Notification listerNotification = new Notification(UUID.randomUUID().toString(),listerId,"CLAIM_CREATED",
                                                            "NEW CLAIM REQUEST",messageToLister,
                                                            System.currentTimeMillis(),false);
        String messageToClaimer = "Your claim request for "+ itemTitle + " has been submitted.";
        Notification claimerNotification = new Notification(UUID.randomUUID().toString(),claimerId,"CLAIM_CREATED",
                                            "CLAIM SUBMITTED",messageToClaimer,
                                                System.currentTimeMillis(), false);
        notificationJson.addNotification(listerNotification);
        notificationJson.addNotification(claimerNotification);
    }

    public void notifyClaimAccepted(String itemId,String listerId,String claimerId,String itemTitle) throws IOException{
        logger.log("NotificationService", "notifyClaimAccepted for claimerID " + claimerId);
        Optional<User> lister = userJson.findById(listerId);
        Optional<User> claimer = userJson.findById(claimerId);
        if(lister.isEmpty() || claimer.isEmpty()){
            return;
        }
        String messageToLister = "You accepted " + claimer.get().getName() +  " claim on " +itemTitle + " .\n" +
                "They can now contact you at " + lister.get().getEmail()+ " .\n" +
                "Their email: "+ claimer.get().getEmail()+ " .\n" +
                "Their hostel number: " + claimer.get().getHostelNumber() + " .";
        Notification listerNotification = new Notification(UUID.randomUUID().toString(),listerId,"CLAIM_ACCEPTED",
                "Claim Accepted Successfully",messageToLister,
                System.currentTimeMillis(),false);
        String messageToClaimer = "Good news! Your claim on " + itemTitle + " has been accepted.\n" +
                "You may now contact the owner at: " + lister.get().getEmail() + " to coordinate pickup.\n" +
                "Their hostel number: " + lister.get().getHostelNumber() + " .\n";
        Notification claimerNotification = new Notification(UUID.randomUUID().toString(),claimerId,"CLAIM_ACCEPTED",
                "Claim Accepted!",messageToClaimer,
                System.currentTimeMillis(), false);
        notificationJson.addNotification(listerNotification);
        notificationJson.addNotification(claimerNotification);
    }

    public void notifyClaimRejected(String itemId,String listerId,String claimerId,String itemTitle) throws IOException{
        logger.log("NotificationService", "notifyClaimRejected for claimerID "+ claimerId );
        Optional<User> claimer = userJson.findById(claimerId);
        if(claimer.isEmpty()){
            return;
        }
        String messageToClaimer = "Your claim for " + itemTitle + " was not accepted.\n" +
                "You may still explore other available listings.";
        Notification claimerNotification = new Notification(UUID.randomUUID().toString(),claimerId,"CLAIM_REJECTED",
                "Claim Rejected",messageToClaimer,
                System.currentTimeMillis(), false);
        notificationJson.addNotification(claimerNotification);
    }

    public void notifyItemExpired(String itemId,String listerId, String itemTitle) throws IOException{
        logger.log("NotificationService", "notifyItemExpired for itemID " + itemId);
        Optional<User> lister = userJson.findById(listerId);
        if(lister.isEmpty()){
            return;
        }
        String messageToLister = "Your item "+ itemTitle +" has expired and is no longer visible on the platform.\n" +
                "If it's still available, you may relist it anytime.";
        Notification listerNotification = new Notification(UUID.randomUUID().toString(),listerId,"ITEM_EXPIRED",
                "Listing Expired",messageToLister,
                System.currentTimeMillis(),false);
        notificationJson.addNotification(listerNotification);
    }

    public List<Notification> showAllNotification(String userId) throws IOException{
        logger.log("NotificationService", "showAllNotification called for userID " + userId);
        List<Notification> list = notificationJson.findAll();
        List<Notification> result = new ArrayList<>();

        for(Notification notification : list){
            if(notification.getUserId().equals(userId)){
                result.add(notification);
                notificationJson.updateNotification(notification.getId());
            }
        }
        return result;
    }
}
