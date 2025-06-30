package com.example.demo;

import com.example.demo.model.Login;
import com.example.demo.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {



    @Autowired
    Login login;

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Boolean>> handleBadCredentialsException() {
        Map<String, Boolean> response = new HashMap<>();
        response.put("authentication", false);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Boolean>> handleBadCredentialsException2() {
        Map<String, Boolean> response = new HashMap<>();
        response.put(login.getUsername(), false);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
}
