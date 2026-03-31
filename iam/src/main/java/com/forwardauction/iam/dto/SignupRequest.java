package com.forwardauction.iam.dto;

import jakarta.validation.constraints.NotBlank;

public record SignupRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String address
) {}