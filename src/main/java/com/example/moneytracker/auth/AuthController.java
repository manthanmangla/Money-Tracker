package com.example.moneytracker.auth;

import com.example.moneytracker.auth.dto.AuthResponse;
import com.example.moneytracker.auth.dto.LoginRequest;
import com.example.moneytracker.auth.dto.RegisterRequest;
import com.example.moneytracker.security.CurrentUser;
import com.example.moneytracker.security.JwtService;
import com.example.moneytracker.security.UserPrincipal;
import com.example.moneytracker.user.User;
import com.example.moneytracker.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CurrentUser currentUser;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          AuthenticationManager authenticationManager,
                          CurrentUser currentUser) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.currentUser = currentUser;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        user = userRepository.save(user);

        String token = jwtService.generateToken(user.getId(), user.getEmail());

        return ResponseEntity.ok(new AuthResponse(user.getId(), user.getEmail(), token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtService.generateToken(principal.getId(), principal.getEmail());

        return ResponseEntity.ok(new AuthResponse(principal.getId(), principal.getEmail(), token));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Long userId = currentUser.getUserId();
        String email = currentUser.getEmail();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(new AuthResponse(userId, email, null));
    }
}

