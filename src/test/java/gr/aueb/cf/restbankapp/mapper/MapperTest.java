package gr.aueb.cf.restbankapp.mapper;

import gr.aueb.cf.restbankapp.dto.CustomerReadOnlyDTO;
import gr.aueb.cf.restbankapp.model.Customer;
import gr.aueb.cf.restbankapp.model.PersonalInfo;
import gr.aueb.cf.restbankapp.model.User;
import gr.aueb.cf.restbankapp.model.static_data.Region;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The mapper decides what leaves the service layer. A field silently dropped here
 * shows up as an empty box in the interface with nothing else going wrong — which
 * is exactly how the home address and the region id went missing until 12/08.
 */
class MapperTest {

    private Mapper mapper;
    private Customer customer;

    private static final UUID UUID_MARIA = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @BeforeEach
    void setup() {
        // The mapper has no collaborators, so it can simply be constructed
        mapper = new Mapper();

        Region region = new Region();
        region.setId(2L);
        region.setName("ΑΤΤΙΚΗΣ");

        User user = new User();
        user.setUsername("maria");

        PersonalInfo info = new PersonalInfo();
        info.setIdNumber("ΑΒ123456");
        info.setPlaceOfBirth("Αθήνα");
        info.setMunicipalityOfRegistration("Αθήνα");
        info.setDateOfBirth("1988-03-15");
        info.setHomeAddress("Πατησίων 1");
        info.setGender("FEMALE");

        customer = new Customer();
        customer.setUuid(UUID_MARIA);
        customer.setFirstname("Μαρία");
        customer.setLastname("Παπαδοπούλου");
        customer.setVat("123456789");
        customer.setEmail("maria@test.gr");
        customer.setPhone("6900000001");
        customer.setRegion(region);
        customer.setUser(user);
        customer.setPersonalInfo(info);
    }

    // G.14 — every field the interface relies on has to survive the mapping
    @Test
    void mapToCustomerReadOnlyDTO_shouldCarryEveryField() {
        CustomerReadOnlyDTO dto = mapper.mapToCustomerReadOnlyDTO(customer);

        assertEquals(UUID_MARIA.toString(), dto.uuid());
        assertEquals("Μαρία", dto.firstname());
        assertEquals("Παπαδοπούλου", dto.lastname());
        assertEquals("123456789", dto.vat());
        assertEquals("maria@test.gr", dto.email());
        assertEquals("6900000001", dto.phone());
        assertEquals("maria", dto.username());
    }

    // The region travels as both a name to display and an id the edit form preselects
    @Test
    void mapToCustomerReadOnlyDTO_shouldCarryBothHalvesOfTheRegion() {
        CustomerReadOnlyDTO dto = mapper.mapToCustomerReadOnlyDTO(customer);

        assertEquals("ΑΤΤΙΚΗΣ", dto.region());
        assertEquals(2L, dto.regionId());
    }

    // The home address was absent from this DTO until 12/08, which made the edit
    // form unable to show what it was about to overwrite.
    @Test
    void mapToCustomerReadOnlyDTO_shouldIncludeThePersonalInfo() {
        CustomerReadOnlyDTO dto = mapper.mapToCustomerReadOnlyDTO(customer);

        assertNotNull(dto.personalInfo());
        assertEquals("ΑΒ123456", dto.personalInfo().idNumber());
        assertEquals("Αθήνα", dto.personalInfo().placeOfBirth());
        assertEquals("Αθήνα", dto.personalInfo().municipalityOfRegistration());
        assertEquals("1988-03-15", dto.personalInfo().dateOfBirth());
        assertEquals("Πατησίων 1", dto.personalInfo().homeAddress());
        assertEquals("FEMALE", dto.personalInfo().gender());
    }
}
