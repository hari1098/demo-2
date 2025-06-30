package com.example.demo.service;

import com.example.demo.model.Item;
import com.example.demo.repository.ItemRepos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime; // Added import
import java.util.List;
import java.util.Optional;

@Service
public class ItemService {

    @Autowired
    private ItemRepos itemRepo;

    public List<Item> getAllItem() {
        return itemRepo.findAll();
    }

    public Optional<Item> getItemById(Long id) { // Changed int to Long
        return itemRepo.findById(id);
    }

    public Item createItem(Item item) {
        // Set creation timestamp if not already set by the client
        if (item.getCreatedon() == null) {
            item.setCreatedon(LocalDateTime.now());
        }
        // Set active status if not already set
        if (item.getIsactive() == null) {
            item.setIsactive(true); // Default to true for new items
        }
        return itemRepo.save(item);
    }

    public boolean deleteItem(Long id) { // Changed int to Long
        Optional<Item> item = itemRepo.findById(id);
        if (item.isPresent()) {
            itemRepo.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    public Item updateItem(Long id, Item updatedItem) { // Changed int to Long
        return itemRepo.findById(id).map(existingItem -> {
            // Update fields from updatedItem to existingItem
            // Ensure you are not trying to update the ID.
            if (updatedItem.getIdname() != null) {
                existingItem.setIdname(updatedItem.getIdname());
            }
            if (updatedItem.getLicensetype() != null) {
                existingItem.setLicensetype(updatedItem.getLicensetype());
            }
            if (updatedItem.getPrice() != null) {
                existingItem.setPrice(updatedItem.getPrice());
            }
            // You might want to update createdby/createdon only if explicitly provided,
            // or keep them as original for auditing. Typically, these are not updated.
            // For now, let's assume they *can* be updated if passed.
            if (updatedItem.getCreatedby() != null) {
                existingItem.setCreatedby(updatedItem.getCreatedby());
            }
            // createdon usually shouldn't be updated, but if business logic allows:
            // if (updatedItem.getCreatedon() != null) {
            //     existingItem.setCreatedon(updatedItem.getCreatedon());
            // }

            if (updatedItem.getItemname() != null) {
                existingItem.setItemname(updatedItem.getItemname());
            }
            if (updatedItem.getUpdatedby() != null) {
                existingItem.setUpdatedby(updatedItem.getUpdatedby());
            }
            // Always update updatedon when an item is modified
            existingItem.setUpdatedon(LocalDateTime.now()); // Set to current time of update

            if (updatedItem.getIsactive() != null) {
                existingItem.setIsactive(updatedItem.getIsactive());
            }
            return itemRepo.save(existingItem);
        }).orElse(null); // Return null if item not found
    }
}