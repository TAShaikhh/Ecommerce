package com.forwardauction.iam.dto;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @NotBlank String username,
        @NotBlank String currentPassword,
        @NotBlank String newPassword
) {}
