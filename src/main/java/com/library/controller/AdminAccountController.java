package com.library.controller;

import com.library.dto.request.RegisterRequest;
import com.library.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/accounts")
@CrossOrigin(origins = "*")
public class AdminAccountController {

    @Autowired
    private AuthService authService;

    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody RegisterRequest request) {
        authService.createUserByAdmin(request);
        return ResponseEntity.ok("User created successfully");
    }
}


