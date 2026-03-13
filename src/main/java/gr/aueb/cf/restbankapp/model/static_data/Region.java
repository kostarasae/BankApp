package gr.aueb.cf.restbankapp.model.static_data;

import gr.aueb.cf.restbankapp.model.Customer;
import jakarta.persistence.*;
import lombok.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "regions")
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PRIVATE)
    @OneToMany(mappedBy = "region", fetch = FetchType.LAZY)
    private Set<Customer> customers = new HashSet<>();

    public Set<Customer> getAllCustomers() {
        return Collections.unmodifiableSet(customers);
    }

    public void addCustomer(Customer customer) {
        if (customers == null) customers = new HashSet<>();
        customers.add(customer);
        customer.setRegion(this);
    }

    public void removeCustomer(Customer customer) {
        if (customers == null) return;
        customers.remove(customer);
        customer.setRegion(null);
    }
}
