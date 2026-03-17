package gr.aueb.cf.restbankapp.dto;

import java.util.Map;

public record ValidationErrorResponseDTO(String code, String message, Map<String, String> errors) {
}
