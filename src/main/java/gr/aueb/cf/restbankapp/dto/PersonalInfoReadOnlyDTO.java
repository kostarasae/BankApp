package gr.aueb.cf.restbankapp.dto;

public record PersonalInfoReadOnlyDTO(String idNumber, String placeOfBirth, 
                                        String municipalityOfRegistration,
                                        String dateOfBirth, String gender) {}