package com.example.demo.service;

import com.example.demo.model.Customer;
import com.example.demo.model.Login;
import com.example.demo.model.Quat;
import com.example.demo.repository.CustomerRepo;
import com.example.demo.repository.LoginRepo;
import com.example.demo.repository.QuatRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class QuatService {

    @Autowired
    private QuatRepo quatRepository;

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private LoginRepo loginRepo;

    public List<Quat> getAllQuats() {
        return quatRepository.findAll();
    }

    public Optional<Quat> getQuatById(Long id) {
        return quatRepository.findById(id);
    }

    public List<Quat> getQuatsByCustomerId(Long customerId) {
        return quatRepository.findByCustomerId(customerId);
    }

    public List<Quat> getQuatsByUserId(Long userId) {
        return quatRepository.findByUserId(userId);
    }

    public Quat createQuat(Quat quat) {
        // Validate and set customer
        if (quat.getCustomer() != null && quat.getCustomer().getId() != null) {
            Optional<Customer> customer = customerRepo.findById(quat.getCustomer().getId());
            if (customer.isPresent()) {
                quat.setCustomer(customer.get());
            } else {
                throw new RuntimeException("Customer not found with id: " + quat.getCustomer().getId());
            }
        }

        // Validate and set user
        if (quat.getUser() != null && quat.getUser().getId() != null) {
            Optional<Login> user = loginRepo.findById(quat.getUser().getId());
            if (user.isPresent()) {
                quat.setUser(user.get());
            } else {
                throw new RuntimeException("User not found with id: " + quat.getUser().getId());
            }
        }

        // Check if quotation number already exists
        if (quatRepository.existsByQuatno(quat.getQuatno())) {
            throw new RuntimeException("Quotation number already exists: " + quat.getQuatno());
        }

        return quatRepository.save(quat);
    }

    public Quat updateQuat(Long id, Quat updateQuat) {
        return quatRepository.findById(id).map(quat -> {
            if (updateQuat.getQuatno() != null) {
                // Check if new quotation number already exists (excluding current record)
                if (!quat.getQuatno().equals(updateQuat.getQuatno()) && 
                    quatRepository.existsByQuatno(updateQuat.getQuatno())) {
                    throw new RuntimeException("Quotation number already exists: " + updateQuat.getQuatno());
                }
                quat.setQuatno(updateQuat.getQuatno());
            }
            if (updateQuat.getQuatDate() != null) {
                quat.setQuatDate(updateQuat.getQuatDate());
            }
            if (updateQuat.getValidity() != null) {
                quat.setValidity(updateQuat.getValidity()); //
            }
            
            // Update customer if provided
            if (updateQuat.getCustomer() != null && updateQuat.getCustomer().getId() != null) {
                Optional<Customer> customer = customerRepo.findById(updateQuat.getCustomer().getId());
                customer.ifPresent(quat::setCustomer);
            }
            
            // Update user if provided
            if (updateQuat.getUser() != null && updateQuat.getUser().getId() != null) {
                Optional<Login> user = loginRepo.findById(updateQuat.getUser().getId());
                user.ifPresent(quat::setUser);
            }
            
            return quatRepository.save(quat);
        }).orElse(null);
    }

    public boolean deleteQuat(Long id) {
        if (quatRepository.existsById(id)) {
            quatRepository.deleteById(id);
            return true;
        }
        return false;
    }
}