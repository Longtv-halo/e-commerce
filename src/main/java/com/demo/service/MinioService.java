package com.demo.service;

import com.demo.dto.RequestUploadFileBean;
import com.demo.entity.Documents;
import com.demo.repository.DocumentRepository;
import com.demo.security.SecurityUtils;
import io.minio.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    private final DocumentRepository documentRepository;

    public String uploadFile(MultipartFile file) {
        return uploadFile(RequestUploadFileBean.builder().file(file).build());
    }

    public String uploadFile(RequestUploadFileBean request) {
        MultipartFile file = request.getFile();
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            String objectName = file.getOriginalFilename();
            if (request.getFolder() != null && !request.getFolder().trim().isEmpty()) {
                String folder = request.getFolder().trim();
                if (!folder.endsWith("/")) {
                    folder = folder + "/";
                }
                objectName = folder + objectName;
            }

            Documents document = Documents.builder()
                    .fileName(file.getOriginalFilename())
                    .objectName(objectName)
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .uploaderId(UUID.fromString(SecurityUtils.currentUserId()))
                    .uploaderUsername(SecurityUtils.currentUsername())
                    .uploadedAt(LocalDateTime.now())
                    .build();

            documentRepository.save(document);

            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .stream(inputStream, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );
            }

            return "Upload thành công file: " + objectName;

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi upload file lên MinIO: " + e.getMessage());
        }
    }

    public InputStream download(String objectName) throws Exception {
        UUID userId = UUID.fromString(SecurityUtils.currentUserId());

        Documents file = documentRepository.findByObjectNameAndUploaderId(objectName, userId)
                .orElseThrow(null);

        if (file == null) {
            throw new AccessDeniedException("Access denied");
        }

        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
    }
}