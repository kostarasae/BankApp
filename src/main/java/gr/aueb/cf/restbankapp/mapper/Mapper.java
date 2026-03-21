package gr.aueb.cf.restbankapp.mapper;

import gr.aueb.cf.restbankapp.dto.*;
import gr.aueb.cf.restbankapp.model.PersonalInfo;
import gr.aueb.cf.restbankapp.model.Role;
import gr.aueb.cf.restbankapp.model.Customer;
import gr.aueb.cf.restbankapp.model.User;
import gr.aueb.cf.restbankapp.model.static_data.Region;
import org.springframework.stereotype.Component;

@Component
public class Mapper {

    public User mapToUserEntity(UserInsertDTO userInsertDTO) {
        return new User(userInsertDTO.username(), userInsertDTO.password());
    }

    public UserReadOnlyDTO mapToUserReadOnlyDTO(User user) {
        return new UserReadOnlyDTO(user.getUuid().toString(), user.getUsername(), user.getRole().getName());
    }

    public Customer mapToCustomerEntity(CustomerInsertDTO dto) {
        Customer customer = new Customer();
        customer.setFirstname(dto.firstname());
        customer.setLastname(dto.lastname());
        customer.setVat(dto.vat());

        UserInsertDTO userDTO = dto.userInsertDTO();
        User user = new User();
        user.setUsername(userDTO.username());
        user.setPassword(userDTO.password());

        customer.addUser(user);

        PersonalInfoInsertDTO personalInfoDTO = dto.personalInfoInsertDTO();
        PersonalInfo personalInfo = new PersonalInfo();
        personalInfo.setAfm(personalInfoDTO.afm());
        personalInfo.setIdentityNumber(personalInfoDTO.identityNumber());
        personalInfo.setPlaceOfBirth(personalInfoDTO.placeOfBirth());
        personalInfo.setMunicipalityOfRegistration(personalInfoDTO
                .municipalityOfRegistration());
        customer.setPersonalInfo(personalInfo);  // Set PersonalInfo entity to Customer

        return customer;
    }

//    public CustomerEditDTO mapToCustomerEditDTO(Customer customer) {
//        return new CustomerEditDTO(
//                customer.getUuid(),
//                customer.getFirstname(),
//                customer.getLastname(),
//                customer.getVat(),
//                customer.getRegion().getId());
//    }

    public CustomerReadOnlyDTO mapToCustomerReadOnlyDTO(Customer customer) {
        return new CustomerReadOnlyDTO(customer.getUuid().toString(),
                customer.getFirstname(), customer.getLastname(), customer.getVat(), customer.getRegion().getName());
    }

//    public RegionReadOnlyDTO mapToRegionReadOnlyDTO(Region region) {
//        return new RegionReadOnlyDTO(region.getId(), region.getName());
//    }
//
//    public RoleReadOnlyDTO mapToRoleReadOnlyDTO(Role role) {
//        return new RoleReadOnlyDTO(role.getId(), role.getName());
//    }
//
//    public static Account mapToModelEntity(AccountInsertDTO dto) {
//        return AccountFactory.create(dto.type(), dto.balance());
//    }
//
//    public static AccountReadOnlyDTO mapToReadOnlyDTO(Account account) {
//        return new AccountReadOnlyDTO(account.getIban(), account.getBalance());
//    }
}
