package dev.demo.accounts.account;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotBlank @Size(max = 100) String ownerName,
        @NotNull @Pattern(regexp = "[A-Z]{3}", message = "must be an ISO 4217 code, e.g. EUR") String currency,
        @NotNull @PositiveOrZero @Digits(integer = 17, fraction = 2) BigDecimal initialBalance) {
}
