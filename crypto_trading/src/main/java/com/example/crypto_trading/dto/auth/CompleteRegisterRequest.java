package com.example.crypto_trading.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompleteRegisterRequest {

    @NotBlank(message = "Password không được để trống")
    @Size(min = 8, message = "Password phải có ít nhất 8 ký tự")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*\\d).+$",
        message = "Password phải có ít nhất 1 chữ hoa và 1 số"
    )
    private String password;

    @NotBlank(message = "Confirm password không được để trống")
    private String confirmPassword;
}
