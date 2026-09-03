package com.example.crypto_trading.util;

import com.example.crypto_trading.exception.AppException;
import org.springframework.http.HttpStatus;

import java.security.SecureRandom;

public class HttpUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private HttpUtil() {}

    /**
     * Trích xuất Bearer token từ Authorization header.
     *
     * @param authHeader giá trị của header Authorization
     * @return token (phần sau "Bearer ")
     * @throws AppException nếu header null hoặc không đúng định dạng
     */
    public static String extractBearerToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AppException(
                    HttpStatus.UNAUTHORIZED,
                    "Thiếu hoặc sai định dạng Authorization header (Bearer <token>)");
        }
        return authHeader.substring(7);
    }

    /**
     * Sinh mã OTP gồm 6 chữ số ngẫu nhiên (100000–999999).
     */
    public static String generateOtp() {
        int code = RANDOM.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
