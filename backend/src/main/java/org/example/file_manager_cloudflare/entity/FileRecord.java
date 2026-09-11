package org.example.file_manager_cloudflare.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "file_records")
public class FileRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String fileType;
    private String s3Key;
    private LocalDateTime uploadDate;
    private double fileSize;

    public FileRecord() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getS3Key() { return s3Key; }
    public void setS3Key(String s3Key) { this.s3Key = s3Key; }
    public String getFileType() {return fileType;}
    public void setFileType(String fileType) {this.fileType = fileType;}
    public void setFileName(String fileName) {this.fileName = fileName;}
    public double getFileSize() {return fileSize;}
    public void setFileSize(double fileSize) {this.fileSize = fileSize;}
    public String getFileName() {return fileName;}

    public FileRecord(String fileName, String fileType, String s3Key, double fileSize) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.s3Key = s3Key;
        this.fileSize = fileSize;
    }

    @PrePersist
    protected void onCreate() {
        this.uploadDate = LocalDateTime.now();
    }
}