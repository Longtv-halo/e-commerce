package com.demo.controller;

import com.demo.dto.BaseResponse;
import com.demo.dto.RequestUploadFileBean;
import com.demo.service.MinioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/file")
public class UploadFileController {

    private final MinioService minioService;

    @PostMapping("/upload")
    public ResponseEntity<BaseResponse<String>> uploadFile(
            @Valid @ModelAttribute RequestUploadFileBean request) {
        String message = minioService.uploadFile(request);
        return ResponseEntity.ok(BaseResponse.ok(message));
    }
}

