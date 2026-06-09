package com.demo.service;

import com.demo.dto.RequestUploadFileBean;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@RequiredArgsConstructor
@Service
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

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
}

