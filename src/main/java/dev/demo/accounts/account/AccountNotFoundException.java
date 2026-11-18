package dev.demo.accounts.account;

import java.util.UUID;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(UUID id) {
        super("Account " + id + " not found");
    }
}
