package dev.demo.accounts.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AccountServiceTest {

    private final AccountRepository repository = mock(AccountRepository.class);
    private final AccountService service = new AccountService(repository);

    @Test
    void createTrimsOwnerNameAndPersists() {
        when(repository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account created = service.create(new CreateAccountRequest("  Ada Lovelace ", "EUR", new BigDecimal("10.50")));

        assertThat(created.getId()).isNotNull();
        assertThat(created.getOwnerName()).isEqualTo("Ada Lovelace");
        assertThat(created.getCurrency()).isEqualTo("EUR");
        assertThat(created.getBalance()).isEqualByComparingTo("10.50");
        assertThat(created.getCreatedAt()).isNotNull();
    }

    @Test
    void getThrowsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(id))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining(id.toString());
    }
}
