package com.multiservice.auth.service;

import com.multiservice.auth.model.User;
import com.multiservice.auth.repository.UserRepository;
import com.multiservice.common.dto.AuthRequest;
import com.multiservice.common.dto.AuthResponse;
import com.multiservice.common.dto.RegisterRequest;
import com.multiservice.common.dto.ValidationResponse;
import com.multiservice.common.exception.CustomException;
import com.multiservice.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException("Email already exists", HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new CustomException("Username already exists", HttpStatus.BAD_REQUEST);
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles("USER") // Default role for new users
                .build();

        userRepository.save(user);

        List<String> roles = parseRoles(user.getRoles());
        String token = jwtUtil.generateToken(user.getEmail(), roles);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .email(user.getEmail())
                .message("User registered successfully")
                .build();
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException("Invalid email or password", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        List<String> roles = parseRoles(user.getRoles());
        String token = jwtUtil.generateToken(user.getEmail(), roles);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .email(user.getEmail())
                .message("Login successful")
                .build();
    }

    public ValidationResponse validateToken(String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            String email = jwtUtil.extractEmail(token);
            boolean isValid = jwtUtil.validateToken(token);

            if (isValid && userRepository.existsByEmail(email)) {
                return ValidationResponse.builder()
                        .valid(true)
                        .email(email)
                        .message("Token is valid")
                        .build();
            }

            return ValidationResponse.builder()
                    .valid(false)
                    .message("Invalid token")
                    .build();
        } catch (Exception e) {
            return ValidationResponse.builder()
                    .valid(false)
                    .message("Token validation failed: " + e.getMessage())
                    .build();
        }
    }

    private List<String> parseRoles(String rolesString) {
        if (rolesString == null || rolesString.trim().isEmpty()) {
            return Arrays.asList("USER");
        }
        return Arrays.stream(rolesString.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
