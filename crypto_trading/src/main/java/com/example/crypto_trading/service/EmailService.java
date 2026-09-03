package com.example.crypto_trading.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Mã xác thực đăng ký CryptoTrading");
            helper.setText(buildOtpEmailBody(otp), true);

            mailSender.send(message);
            log.info("OTP email sent to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send OTP email to: {}", toEmail, e);
            throw new RuntimeException("Không thể gửi email OTP, vui lòng thử lại");
        }
    }

    private String buildOtpEmailBody(String otp) {
        return """
                <div style="font-family: Arial, sans-serif; max-width: 480px; margin: 0 auto; padding: 24px;
                            border: 1px solid #e0e0e0; border-radius: 8px;">
                    <h2 style="color: #1a1a2e; margin-bottom: 8px;">CryptoTrading</h2>
                    <p style="color: #555;">Mã xác thực đăng ký của bạn là:</p>
                    <div style="font-size: 36px; font-weight: bold; letter-spacing: 8px;
                                color: #f0b90b; text-align: center; padding: 16px 0;">
                        %s
                    </div>
                    <p style="color: #888; font-size: 13px;">Mã có hiệu lực trong <b>10 phút</b>.
                       Không chia sẻ mã này với bất kỳ ai.</p>
                </div>
                """.formatted(otp);
    }
}
