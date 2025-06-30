package com.example.demo.service;

import com.example.demo.dto.UserDto;
import com.example.demo.model.Login;
import com.example.demo.repository.LoginRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    @Autowired
    private LoginRepo loginRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Login registerUser(UserDto userDto) {
        // Check if username already exists
        if (loginRepo.existsByUsername(userDto.getUsername())) {
            throw new RuntimeException("Username already exists!");
        }

        // Check if email already exists
        if (loginRepo.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }

        // Create new user
        Login user = new Login();
        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setEmail(userDto.getEmail());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setUserType(userDto.getUserType() != null ? userDto.getUserType() : "USER");
        user.setIsActive(userDto.getIsActive() != null ? userDto.getIsActive() : true);
        user.setCreatedOn(LocalDateTime.now());

        return loginRepo.save(user);
    }

    public UserDto getUserByUsername(String username) {
        Login user = loginRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userType(user.getUserType())
                .isActive(user.getIsActive())
                .build();
    }
}