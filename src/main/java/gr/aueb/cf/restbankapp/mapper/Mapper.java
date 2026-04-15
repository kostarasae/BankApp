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
        customer.setEmail(dto.email());

        UserInsertDTO userDTO = dto.userInsertDTO();
        User user = new User();
        user.setUsername(userDTO.username());
        user.setPassword(userDTO.password());

        customer.addUser(user);

        PersonalInfoInsertDTO personalInfoDTO = dto.personalInfoInsertDTO();
        PersonalInfo personalInfo = new PersonalInfo();
        personalInfo.setIdNumber(personalInfoDTO.idNumber());
        personalInfo.setPlaceOfBirth(personalInfoDTO.placeOfBirth());
        personalInfo.setMunicipalityOfRegistration(personalInfoDTO
                .municipalityOfRegistration());
        customer.setPersonalInfo(personalInfo);  // Set PersonalInfo entity to Customer

        return customer;
    }

    public CustomerReadOnlyDTO mapToCustomerReadOnlyDTO(Customer customer) {
        PersonalInfoReadOnlyDTO personalInfoDTO = new PersonalInfoReadOnlyDTO(
                customer.getPersonalInfo().getIdNumber(),
                customer.getPersonalInfo().getPlaceOfBirth(),
                customer.getPersonalInfo().getMunicipalityOfRegistration()
        );
        return new CustomerReadOnlyDTO(customer.getUuid().toString(),
                customer.getFirstname(), customer.getLastname(), customer.getVat(), customer.getEmail(), 
                customer.getRegion().getName(), personalInfoDTO);
    }

    public Account mapToAccountModelEntity(AccountInsertDTO dto) {
        return AccountFactory.create(dto.type(), dto.balance(), dto.customerUuid());
    }

    public AccountReadOnlyDTO mapToAccountReadOnlyDTO(Account account) {
        return new AccountReadOnlyDTO(account.getIban(), account.getBalance());
    }
}
