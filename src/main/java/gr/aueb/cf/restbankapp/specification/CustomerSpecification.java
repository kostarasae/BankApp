package gr.aueb.cf.restbankapp.specification;

import gr.aueb.cf.restbankapp.core.filters.CustomerFilters;
import gr.aueb.cf.restbankapp.model.Customer;
import org.springframework.data.jpa.domain.Specification;

public class CustomerSpecification {

    public static Specification<Customer> build(CustomerFilters filters) {
        return Specification.allOf(
                hasLastname(filters.getLastname()),
                hasRegion(filters.getRegion()),
                isDeleted(filters.isDeleted())
        );
    }

    private static Specification<Customer> hasLastname(String lastname) {
        return ((root, query, cb)
                -> lastname == null ? cb.conjunction() :
                cb.like(cb.lower(root.get("lastname")), lastname.toLowerCase() + "%"));
    }

    private static Specification<Customer> hasRegion(String region) {
        return ((root, query, cb)
                -> region == null ? cb.conjunction() :
                cb.like(cb.lower(root.get("region").get("name")), region.toLowerCase()));
    }

    private static Specification<Customer> isDeleted(boolean deleted) {
        return ((root, query, cb)
                -> cb.equal(root.get("deleted"), deleted));
    }
}
