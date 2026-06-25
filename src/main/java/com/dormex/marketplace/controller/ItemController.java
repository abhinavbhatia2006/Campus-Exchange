package com.dormex.marketplace.controller;

import com.dormex.marketplace.model.Item;
import com.dormex.marketplace.service.AuthService;
import com.dormex.marketplace.service.CustomLogger;
import com.dormex.marketplace.service.FileStorageService;
import com.dormex.marketplace.service.ItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** handles HTTP requests coming from frontend/postman in the path "/api/items" */
@RestController
@RequestMapping("/api/items")
public class ItemController {
    private final ItemService itemService;
    private final FileStorageService storage;
    private final AuthService auth;
    private final CustomLogger logger;

    /** constructor*/
    public ItemController(ItemService itemService, FileStorageService storage, AuthService authService, CustomLogger logger){
        this.itemService = itemService;
        this.storage = storage;
        this.auth = authService;
        this.logger = logger;
    }

    /** This endpoint expects multipart form data because image upload is included.*/
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> createItem(
            @RequestHeader("Auth-Token") String token,
            @RequestParam String college,
            @RequestParam int quantity,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String category,
            @RequestParam Double price,
            @RequestPart MultipartFile image
    ){
        try{
            String listerId = auth.verifySession(token);
            String imagePath = storage.store(image);

            Item item = new Item();
            item.setCollege(college);
            item.setItemId(UUID.randomUUID().toString());
            item.setListerId(listerId);
            item.setQuantity(quantity);
            item.setTitle(title);
            item.setDescription(description);
            item.setCategory(category);
            item.setPrice(price);
            item.setImagePath(imagePath);

            Item created = itemService.createItem(item);
            return ResponseEntity.status(201).body(created); //status 201 used to tell that the request was successfull and a new resource has been created
        }catch (IOException e) {
            return ResponseEntity.status(500).body("Error saving item: " + e.getMessage());// status 500 Internal Server Error (a standard code for unexpected server-side problems)
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal error: " + e.getMessage());
        }
    }
    /** gets all the items from items.json*/
    @GetMapping
    public ResponseEntity<?> getItems(@RequestParam(required = false) String category, @RequestParam String college){
        try{
            List<Item> items;
            if(category != null && !category.isBlank()){
                items = itemService.filter(category,college);
            }
            else{
                items = itemService.getAllItems(college);
            }
            return ResponseEntity.ok(items); // shows 200 ok on postman indicating success
        }catch (IOException e) {

            return ResponseEntity.status(500).body("Error reading items: " + e.getMessage());
        }catch(Exception e){
            return ResponseEntity.status(403).body("Error reading items: " + e.getMessage());
        }
    }

    /** finds items by there id*/
    @GetMapping("/{id}")
    public ResponseEntity<?> getItemById(@PathVariable String id){
        try{

            Optional<Item> opt = itemService.findById(id);
            if (opt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(opt.get());
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error reading item: " + e.getMessage());
        }
    }

    /** shows all the items listed by the user with the userid given*/
    @GetMapping("/listed")
    public ResponseEntity<?> getListedItems(@RequestHeader("Auth-Token") String token) {
        try {
            String userId = auth.verifySession(token);
            List<Item> items = itemService.ListedItems(userId);
            return ResponseEntity.ok(items);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error reading items: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal error: " + e.getMessage());
        }

    }
}
