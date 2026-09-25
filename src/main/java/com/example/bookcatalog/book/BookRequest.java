package com.example.bookcatalog.book;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record BookRequest(
        @NotBlank String title,
        @NotBlank String author,
        @Pattern(regexp = "^(97[89])?\\d{9}[\\dX]$", message = "must be a valid ISBN-10 or ISBN-13 (digits only)")
        String isbn,
        @Min(1450) @Max(2100) int publishedYear) {
}
