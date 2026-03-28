package gr.aueb.cf.restbankapp.mapper;

import gr.aueb.cf.restbankapp.core.factory.AccountFactory;
import gr.aueb.cf.restbankapp.dto.*;
import gr.aueb.cf.restbankapp.model.*;
import gr.aueb.cf.restbankapp.model.static_data.Region;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

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

    public CustomerReadOnlyDTO mapToCustomerReadOnlyDTO(Customer customer) {
        return new CustomerReadOnlyDTO(customer.getUuid().toString(),
                customer.getFirstname(), customer.getLastname(), customer.getVat(), customer.getRegion().getName());
    }

    public Account mapToAccountModelEntity(AccountInsertDTO dto) {
        return AccountFactory.create(dto.type(), dto.balance());
    }

    public AccountReadOnlyDTO mapToAccountReadOnlyDTO(Account account) {
        return new AccountReadOnlyDTO(account.getIban(), account.getBalance());
    }
}
