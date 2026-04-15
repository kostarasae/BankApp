package gr.aueb.cf.restbankapp.dto;

public record CustomerReadOnlyDTO(String uuid, String firstname, String lastname,
                                 String vat, String email, String region, PersonalInfoReadOnlyDTO personalInfo) {
}