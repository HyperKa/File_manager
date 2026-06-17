package org.example.file_manager_cloudflare.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "file_records")
@Getter
@Setter
@NoArgsConstructor // Конструктор без параметров для JPA
@AllArgsConstructor // Конструктор со всеми параметрами
@Builder // Удобный паттерн для создания объектов
public class FileRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    private String fileType;

    private String filePath;

    private LocalDateTime uploadDate;

    private double fileSize;

    @PrePersist
    protected void onCreate() {
        this.uploadDate = LocalDateTime.now();
    }
}