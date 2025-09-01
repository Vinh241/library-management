package com.library.service;

import com.library.dto.request.LoginRequest;
import com.library.dto.response.LoginResponse;
import com.library.dto.request.RegisterRequest;
import com.library.entity.Account;
import com.library.repository.AccountRepository;
import com.library.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    public LoginResponse login(LoginRequest request) {
        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // Get user details
        Account account = accountRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate JWT token
        String token = jwtUtil.generateToken(account);

        // Create response
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUsername(account.getUsername());
        response.setFullName(account.getFullName());
        response.setRole(account.getRole().name());
        response.setLibraryId(account.getLibraryId());
        response.setProvinceId(account.getProvinceId());

        return response;
    }

    public void register(RegisterRequest request) {
        // Check if username exists
        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email exists
        if (request.getEmail() != null && accountRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create new account
        Account account = new Account();
        account.setUsername(request.getUsername());
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        account.setFullName(request.getFullName());
        account.setEmail(request.getEmail());
        account.setPhone(request.getPhone());
        account.setAddress(request.getAddress());
        account.setRole(Account.Role.valueOf(request.getRole() != null ? request.getRole() : "READER"));
        account.setLibraryId(request.getLibraryId());
        account.setProvinceId(request.getProvinceId());
        account.setCommuneId(request.getCommuneId());

        accountRepository.save(account);
    }
}