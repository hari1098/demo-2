package com.example.demo.service;

import com.example.demo.model.Item;
import com.example.demo.model.Qitem;
import com.example.demo.model.Quat;
import com.example.demo.repository.ItemRepos;
import com.example.demo.repository.QitemRepo;
import com.example.demo.repository.QuatRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class QitemService {

    @Autowired
    private QitemRepo qitemRepo;

    @Autowired
    private QuatRepo quatRepo;

    @Autowired
    private ItemRepos itemRepo;

    public List<Qitem> getQitems() {
        return qitemRepo.findAll();
    }

    public Optional<Qitem> getQitemById(Long id) {
        return qitemRepo.findById(id);
    }

    public List<Qitem> getQitemsByQuotationId(Long quatId) {
        return qitemRepo.findByQuotationId(quatId);
    }

    public Qitem createQitem(Qitem qitem) {
        // Ensure the quotation and item exist
        if (qitem.getQuotation() != null && qitem.getQuotation().getId() != null) {
            Optional<Quat> quat = quatRepo.findById(qitem.getQuotation().getId());
            if (quat.isPresent()) {
                qitem.setQuotation(quat.get());
            } else {
                throw new RuntimeException("Quotation not found with id: " + qitem.getQuotation().getId());
            }
        }

        if (qitem.getItem() != null && qitem.getItem().getId() != null) {
            Optional<Item> item = itemRepo.findById(qitem.getItem().getId());
            if (item.isPresent()) {
                qitem.setItem(item.get());
                // Set unit price from item if not provided
                if (qitem.getUnitPrice() == null) {
                    qitem.setUnitPrice(item.get().getPrice());
                }
            } else {
                throw new RuntimeException("Item not found with id: " + qitem.getItem().getId());
            }
        }

        return qitemRepo.save(qitem);
    }

    public Qitem updateQitem(Long id, Qitem updatedQitem) {
        return qitemRepo.findById(id).map(qitem -> {
            if (updatedQitem.getQuantity() != null) {
                qitem.setQuantity(updatedQitem.getQuantity());
            }
            if (updatedQitem.getUnitPrice() != null) {
                qitem.setUnitPrice(updatedQitem.getUnitPrice());
            }
            if (updatedQitem.getLicenseType() != null) {
                qitem.setLicenseType(updatedQitem.getLicenseType());
            }
            
            // Update quotation if provided
            if (updatedQitem.getQuotation() != null && updatedQitem.getQuotation().getId() != null) {
                Optional<Quat> quat = quatRepo.findById(updatedQitem.getQuotation().getId());
                quat.ifPresent(qitem::setQuotation);
            }
            
            // Update item if provided
            if (updatedQitem.getItem() != null && updatedQitem.getItem().getId() != null) {
                Optional<Item> item = itemRepo.findById(updatedQitem.getItem().getId());
                item.ifPresent(qitem::setItem);
            }
            
            return qitemRepo.save(qitem);
        }).orElse(null);
    }

    public boolean deleteQitem(Long id) {
        if (qitemRepo.existsById(id)) {
            qitemRepo.deleteById(id);
            return true;
        }
        return false;
    }
}