package com.example.crypto_trading.service;

import com.example.crypto_trading.dto.auth.AuthResponse;
import com.example.crypto_trading.dto.auth.CompleteRegisterRequest;
import com.example.crypto_trading.dto.auth.LoginRequest;
import com.example.crypto_trading.dto.auth.SendOtpRequest;
import com.example.crypto_trading.dto.auth.VerifyOtpRequest;
import com.example.crypto_trading.entity.User;
import com.example.crypto_trading.exception.AppException;
import com.example.crypto_trading.repository.UserRepository;
import com.example.crypto_trading.util.HttpUtil;
import com.example.crypto_trading.util.JwtUtil;
import com.example.crypto_trading.util.OtpStore;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OtpStore otpStore;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    // ─── Bước 1: Gửi OTP ────────────────────────────────────────────────────────

    public void sendOtp(SendOtpRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(email)) {
            throw new AppException(HttpStatus.CONFLICT,
                    "Email đã được đăng ký, vui lòng đăng nhập");
        }

        String otp = HttpUtil.generateOtp();
        otpStore.save(email, otp);

        emailService.sendOtpEmail(email, otp);
        log.info("OTP sent to email: {}", email);
    }

    // ─── Bước 2: Verify OTP → trả token tạm ────────────────────────────────────

    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        boolean valid = otpStore.verify(email, request.getOtp());
        if (!valid) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "OTP không đúng hoặc đã hết hạn");
        }

        String tempToken = jwtUtil.generateTempToken(email);
        log.info("OTP verified for email: {}", email);

        return AuthResponse.builder()
                .token(tempToken)
                .email(email)
                .message("Xác thực OTP thành công, vui lòng hoàn tất đăng ký")
                .build();
    }

    // ─── Bước 3: Hoàn tất đăng ký → trả access token ───────────────────────────

    @Transactional
    public AuthResponse completeRegister(String tempToken, CompleteRegisterRequest request) {
        // Validate temp token
        String email;
        try {
            email = jwtUtil.validateTempToken(tempToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw new AppException(HttpStatus.UNAUTHORIZED,
                    "Token xác thực không hợp lệ hoặc đã hết hạn, vui lòng gửi lại OTP");
        }

        // Kiểm tra password khớp confirm
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Password và confirm password không khớp");
        }

        // Kiểm tra email chưa bị đăng ký trùng (race condition guard)
        if (userRepository.existsByEmail(email)) {
            throw new AppException(HttpStatus.CONFLICT,
                    "Email đã được đăng ký");
        }

        // Tạo user thật
        User user = User.builder()
                .email(email)
                .username(email.split("@")[0])
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .build();

        userRepository.save(user);
        log.info("New user registered: {}", email);

        // Sinh access token chính thức
        String accessToken = jwtUtil.generateAccessToken(email);

        return AuthResponse.builder()
                .token(accessToken)
                .email(email)
                .message("Đăng ký thành công")
                .build();
    }

    // ─── Login ──────────────────────────────────────────────────────────────────

    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED,
                        "Email hoặc mật khẩu không đúng"));

        if (!user.isEnabled()) {
            throw new AppException(HttpStatus.FORBIDDEN,
                    "Tài khoản chưa được kích hoạt");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(HttpStatus.UNAUTHORIZED,
                    "Email hoặc mật khẩu không đúng");
        }

        String accessToken = jwtUtil.generateAccessToken(email);
        log.info("User logged in: {}", email);

        return AuthResponse.builder()
                .token(accessToken)
                .email(email)
                .message("Đăng nhập thành công")
                .build();
    }

}
