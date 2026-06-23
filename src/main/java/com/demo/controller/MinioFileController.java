package com.demo.controller;

import com.demo.dto.BaseResponse;
import com.demo.dto.RequestUploadFileBean;
import com.demo.service.MinioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/file")
public class MinioFileController {

    private final MinioService minioService;

    @PostMapping("/upload")
    public ResponseEntity<BaseResponse<String>> uploadFile(
            @Valid @ModelAttribute RequestUploadFileBean request) {
        String message = minioService.uploadFile(request);
        return ResponseEntity.ok(BaseResponse.ok(message));
    }

    @GetMapping("/files/{fileName}")
    public ResponseEntity<InputStreamResource> download(
            @PathVariable String fileName) throws Exception {

        InputStream inputStream = minioService.download(fileName);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\""
                )
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(inputStream));
    }
}