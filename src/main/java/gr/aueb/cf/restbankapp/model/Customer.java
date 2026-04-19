package gr.aueb.cf.restbankapp.model;

import gr.aueb.cf.restbankapp.model.static_data.Region;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "customers")
public class Customer extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true, nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID uuid;

    @Column(unique = true)
    private String vat;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    private String firstname;
    private String lastname;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "personal_info_id")
    private PersonalInfo personalInfo;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "customers_accounts",
            joinColumns = @JoinColumn(name = "customer_id"),
            inverseJoinColumns = @JoinColumn(name = "account_id"))
    private Set<Account> accounts = new HashSet<>();

    @PrePersist
    public void initializeUUID() {
        this.uuid = UUID.randomUUID();
    }

    public void addUser(User user) {
        this.user = user;
        user.setCustomer(this);
    }

    public void addAccount(Account account) {
        accounts.add(account);
        account.getCustomers().add(this);
    }

    public void removeAccount(Account account) {
        accounts.remove(account);
        account.getCustomers().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Customer customer)) return false;
        return Objects.equals(getUuid(), customer.getUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getUuid());
    }
}
