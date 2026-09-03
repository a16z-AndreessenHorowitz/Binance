package com.example.crypto_trading.util;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory OTP store với TTL 10 phút.
 * Thread-safe dùng ConcurrentHashMap.
 */
@Component
public class OtpStore {

    private static final long OTP_TTL_MS = 10 * 60 * 1000L; // 10 phút

    private final Map<String, OtpEntry> store = new ConcurrentHashMap<>();

    public void save(String email, String otp) {
        store.put(email.toLowerCase(), new OtpEntry(otp, Instant.now().plusMillis(OTP_TTL_MS)));
    }

    /**
     * Kiểm tra OTP có đúng và chưa hết hạn không.
     *
     * @return true nếu hợp lệ và xóa entry luôn
     */
    public boolean verify(String email, String otp) {
        OtpEntry entry = store.get(email.toLowerCase());
        if (entry == null) return false;
        if (Instant.now().isAfter(entry.expiresAt())) {
            store.remove(email.toLowerCase());
            return false;
        }
        boolean match = entry.otp().equals(otp);
        if (match) {
            store.remove(email.toLowerCase());
        }
        return match;
    }

    public void invalidate(String email) {
        store.remove(email.toLowerCase());
    }

    private record OtpEntry(String otp, Instant expiresAt) {}
}
