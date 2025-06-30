package com.example.demo.service;

import com.example.demo.model.Login;
import com.example.demo.repository.LoginRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LoginService {

    @Autowired
    private LoginRepo loginRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Login> getAllLogins() {
        return loginRepo.findAll();
    }

    public Optional<Login> getloginById(Long id) {
        return loginRepo.findById(id);
    }

    public String validateLogin(Login login) {
        if (loginRepo.existsByEmail(login.getEmail())) {
            return "Email already exists";
        }
        if (loginRepo.existsByUsername(login.getUsername())) {
            return "Username already exists";
        }
        return "OK";
    }

    public Login createLogin(Login login) {
        // Encode password before saving
        login.setPassword(passwordEncoder.encode(login.getPassword()));
        login.setCreatedOn(LocalDateTime.now());
        if (login.getIsActive() == null) {
            login.setIsActive(true);
        }
        return loginRepo.save(login);
    }

    public Login updateLogin(Long id, Login updatedLogin) {
        return loginRepo.findById(id).map(login -> {
            login.setUsername(updatedLogin.getUsername());
            // Only encode password if it's being changed
            if (updatedLogin.getPassword() != null && !updatedLogin.getPassword().isEmpty()) {
                login.setPassword(passwordEncoder.encode(updatedLogin.getPassword()));
            }
            login.setEmail(updatedLogin.getEmail());
            login.setFirstName(updatedLogin.getFirstName());
            login.setLastName(updatedLogin.getLastName());
            login.setUserType(updatedLogin.getUserType());
            login.setUpdatedBy(updatedLogin.getUpdatedBy());
            login.setUpdatedOn(LocalDateTime.now());
            if (updatedLogin.getIsActive() != null) {
                login.setIsActive(updatedLogin.getIsActive());
            }
            return loginRepo.save(login);
        }).orElse(null);
    }

    public boolean deleteLogin(Long id) {
        Optional<Login> login = loginRepo.findById(id);
        if (login.isPresent()) {
            loginRepo.deleteById(id);
            return true;
        }
        return false;
    }
}