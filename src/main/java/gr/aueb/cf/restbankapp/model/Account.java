package gr.aueb.cf.restbankapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Abstract Class / Skeletal Implementation / Builder Design Pattern
 */
@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "accounts")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "account_type")
public abstract class Account extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String accountNumber;

    @Column(unique = true)
    private String iban;

    private String currency;
    private BigDecimal balance;

    @Transient
    protected FeeStrategy feeStrategy;

    @Getter
    @Setter(AccessLevel.NONE)
    @ManyToMany(mappedBy = "accounts", fetch = FetchType.LAZY)
    private Set<Customer> customers = new HashSet<>();

    protected Account(Builder<?> builder) {
        this.accountNumber = builder.accountNumber;
        this.iban = builder.iban;
        this.balance = builder.balance;
        this.feeStrategy = builder.feeStrategy;
    }

    public abstract static class Builder<T extends Builder<T>> {
        private final String accountNumber;
        private final String iban;
        private final BigDecimal balance;
        private final String customerUuid;
        protected FeeStrategy feeStrategy;

        public Builder(String accountNumber, String iban, BigDecimal balance, String customerUuid) {

            if (accountNumber == null || accountNumber.isBlank())
                throw new IllegalArgumentException("Account number required");

            if (iban == null || iban.isBlank())
                throw new IllegalArgumentException("IBAN required");

            if (balance == null)
                throw new IllegalArgumentException("Balance required");

            if (customerUuid == null || customerUuid.isBlank())
                throw new IllegalArgumentException("Customer UUID required");

            this.accountNumber = accountNumber;
            this.iban = iban;
            this.balance = balance;
            this.customerUuid = customerUuid;
        }

        public T feeStrategy(FeeStrategy feeStrategy) {
            this.feeStrategy = feeStrategy;
            return self();
        }

        protected abstract T self();

        public abstract Account build();
    }

    public void addCustomer(Customer customer) {
        customers.add(customer);
        customer.getAccounts().add(this);
    }

    public void removeCustomer(Customer customer) {
        customers.remove(customer);
        customer.getAccounts().remove(this);
    }

    // Template Method / Hook
    public abstract boolean violatesRules(BigDecimal balance);

    // concrete methods (full implementation, opposite of abstract)
    // override concrete methods
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Account account)) return false;
        return Objects.equals(this.iban, account.iban);
    }

    @Override
    public int hashCode() {
        return this.iban.hashCode();
    }

    @Override
    public String toString() {
        String feeStrategyInfo = "none";
        if (feeStrategy != null) {
            String value = feeStrategy.value();
            feeStrategyInfo = (value == null || value.isBlank())
                    ? feeStrategy.getClass().getSimpleName()
                    : value;
        }

        return "Account number " + this.accountNumber + " with iban " + this.iban + " has balance of " + this.balance
                + " in currency of " + this.currency + " and fee strategy " + feeStrategyInfo + " created at " + this.getCreatedAt();
    }
}