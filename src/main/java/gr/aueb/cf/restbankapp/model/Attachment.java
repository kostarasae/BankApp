package gr.aueb.cf.restbankapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "attachments")
public class Attachment extends AbstractEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String filename;

    @Column(name = "saved_name",  unique = true, nullable = false)
    private String savedName;

    @Column(name = "file_path", nullable = false, length = 1024)
    private String filePath;

    @Column(name = "content_type")
    private String contentType;

    @Column(length = 50)
    private String extension;
}