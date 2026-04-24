package com.renteasebd.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdatePreferredLanguageRequest(
    @NotBlank @Pattern(regexp = "^(en|bn)$") String language
) {
}
