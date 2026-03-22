package gr.aueb.cf.restbankapp.core.filters;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class CustomerFilters {
    private UUID uuid;
    private String vat;
    private String afm;
    private String lastname;
    private boolean deleted;
    private String region;
}
