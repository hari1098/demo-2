package com.example.demo.service;

import com.example.demo.model.Customer;
import com.example.demo.repository.CustomerRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepo customerRepo;

    // Get all customers
    public List<Customer> getAllCustomer() {
        return customerRepo.findAll();
    }

    // Get customer by ID
    public Optional<Customer> getCustomerById(Long id) {
        return customerRepo.findById(id);
    }

    // Create customer
    public Customer createCustomer(Customer customer) {
        return customerRepo.save(customer);
    }

    // Delete customer
    public boolean deleteCustomer(Long id) {
        if (customerRepo.existsById(id)) {
            customerRepo.deleteById(id);
            return true;
        }
        return false;
    }

    // Update customer
    public Customer updateCustomer(Long id, Customer customerDetails) {
        Customer existingCustomer = customerRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
        existingCustomer.setCustomername(customerDetails.getCustomername());
        existingCustomer.setEmailid(customerDetails.getEmailid());
        existingCustomer.setMobilenumber(customerDetails.getMobilenumber());
        existingCustomer.setCompanyname(customerDetails.getCompanyname());
        existingCustomer.setAddress(customerDetails.getAddress());
        existingCustomer.setRefferedby(customerDetails.getRefferedby());
        existingCustomer.setUserno(customerDetails.getUserno());

        return customerRepo.save(existingCustomer);
    }
}
