package dev.demo.accounts.account;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository repository;

    public AccountService(AccountRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Account> findAll() {
        return repository.findAll(Sort.by("createdAt"));
    }

    @Transactional(readOnly = true)
    public Account get(UUID id) {
        return repository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
    }

    @Transactional
    public Account create(CreateAccountRequest request) {
        return repository.save(new Account(request.ownerName().strip(), request.currency(), request.initialBalance()));
    }
}
