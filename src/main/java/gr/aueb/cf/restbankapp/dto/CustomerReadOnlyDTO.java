package gr.aueb.cf.restbankapp.dto;

public record CustomerReadOnlyDTO(String uuid, String firstname, String lastname,
                                 String vat, String email, String phone, String region,
                                 String username, PersonalInfoReadOnlyDTO personalInfo) {
}