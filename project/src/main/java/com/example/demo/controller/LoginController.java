package com.example.demo.controller;

import com.example.demo.model.Login;
import com.example.demo.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logins")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @GetMapping
    public List<Login> getAllLogins() {
        return loginService.getAllLogins();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Login> getLoginById(@PathVariable Long id) {
        return loginService.getloginById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createLogin(@RequestBody Login login) {
        String validationMessage = loginService.validateLogin(login);
        if (!"OK".equals(validationMessage)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(validationMessage);
        }
        return ResponseEntity.ok(loginService.createLogin(login));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Login> updateLogin(@PathVariable Long id, @RequestBody Login login) {
        Login updated = loginService.updateLogin(id, login);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteLogin(@PathVariable Long id) {
        boolean deleted = loginService.deleteLogin(id);
        if (deleted) {
            return ResponseEntity.ok("Login with ID " + id + " was deleted successfully.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}