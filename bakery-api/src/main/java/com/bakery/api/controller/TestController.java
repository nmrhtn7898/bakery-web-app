package com.bakery.api.controller;

import com.bakery.api.util.S3FileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final S3FileUtils s3FileUtils;

    @GetMapping("/api/v1/test")
    public ResponseEntity test(OAuth2Authentication auth2Authentication) {
        return ResponseEntity.ok(auth2Authentication);
    }

    @PostMapping("/api/v1/files")
    public ResponseEntity fileUploadTest(List<MultipartFile> files) {
        String dir = "";
        files.forEach(file -> {
            String key = s3FileUtils.upload(file, dir);
            // TODO DB 파일 정보 저장
        });
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/v1/files/{id}")
    public ResponseEntity fileDeleteTest(@RequestBody String filename) {
        // TODO DB 파일 정보 조회
        s3FileUtils.delete(filename);
        return ResponseEntity.noContent().build();
    }

}
