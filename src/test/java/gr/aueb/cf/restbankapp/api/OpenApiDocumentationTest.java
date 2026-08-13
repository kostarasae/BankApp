package gr.aueb.cf.restbankapp.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * The brief requires the REST API to be documented with Swagger, and documentation
 * rots quietly: a new endpoint ships undocumented and nobody notices. This reads the
 * generated OpenAPI document and fails if any operation is missing a summary.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser
    void everyEndpointIsDocumented() throws Exception {
        String body = mockMvc.perform(get("/v3/api-docs"))
                .andReturn().getResponse().getContentAsString();

        JsonNode paths = objectMapper.readTree(body).get("paths");
        List<String> undocumented = new ArrayList<>();
        int total = 0;

        for (var pathEntry : paths.properties()) {
            for (var methodEntry : pathEntry.getValue().properties()) {
                total++;
                JsonNode summary = methodEntry.getValue().get("summary");
                if (summary == null || summary.asText().isBlank()) {
                    undocumented.add(methodEntry.getKey().toUpperCase() + " " + pathEntry.getKey());
                }
            }
        }

        assertTrue(total > 0, "the OpenAPI document listed no operations at all");
        assertTrue(undocumented.isEmpty(),
                "these operations have no summary: " + undocumented);
    }
}
