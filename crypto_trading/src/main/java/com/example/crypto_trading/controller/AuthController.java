package com.example.crypto_trading.controller;

import com.example.crypto_trading.dto.auth.AuthResponse;
import com.example.crypto_trading.dto.auth.CompleteRegisterRequest;
import com.example.crypto_trading.dto.auth.LoginRequest;
import com.example.crypto_trading.dto.auth.SendOtpRequest;
import com.example.crypto_trading.dto.auth.VerifyOtpRequest;
import com.example.crypto_trading.service.AuthService;
import com.example.crypto_trading.util.HttpUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Bước 1: Gửi OTP đến email.
     * POST /api/auth/register/send-otp
     * Body: { "email": "user@example.com" }
     */
    /**
     * Đăng nhập.
     * POST /api/auth/login
     * Body: { "email": "...", "password": "..." }
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register/send-otp")
    public ResponseEntity<Void> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        authService.sendOtp(request);
        return ResponseEntity.ok().build();
    }

    /**
     * Bước 2: Xác thực OTP, nhận token tạm.
     * POST /api/auth/register/verify-otp
     * Body: { "email": "...", "otp": "123456" }
     */
    @PostMapping("/register/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }

    /**
     * Bước 3: Hoàn tất đăng ký với password.
     * POST /api/auth/register/complete
     * Header: Authorization: Bearer <temp-token>
     * Body: { "password": "...", "confirmPassword": "..." }
     */
    @PostMapping("/register/complete")
    public ResponseEntity<AuthResponse> completeRegister(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CompleteRegisterRequest request) {

        String tempToken = HttpUtil.extractBearerToken(authHeader);
        return ResponseEntity.ok(authService.completeRegister(tempToken, request));
    }


}
