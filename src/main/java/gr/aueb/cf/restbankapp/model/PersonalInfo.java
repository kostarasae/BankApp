package gr.aueb.cf.restbankapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "personal_information")
public class PersonalInfo extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_number", unique = true, nullable = false)
    private String idNumber;

    @Column(name = "place_of_birth", nullable = false)
    private String placeOfBirth;

    @Column(name = "date_of_birth", nullable = false)
    private String dateOfBirth;

    @Column(name = "home_address", nullable = false)
    private String homeAddress;

    @Column(name = "gender")
    private String gender;

    @Column(name = "municipality_of_registration", nullable = false)
    private String municipalityOfRegistration;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "id_file_id", unique = true)
    private Attachment idFile;

    public void addIdFile(Attachment attachment) {
        this.idFile = attachment;
    }

    public void removeIdFile() {
        this.idFile = null;
    }
}
