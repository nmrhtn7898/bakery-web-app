package com.bread.api.controller;

import com.bread.api.util.FileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final FileUtils fileUtils;

    @GetMapping("/api/v1/test")
    public ResponseEntity test(OAuth2Authentication auth2Authentication) {
        return ResponseEntity.ok(auth2Authentication);
    }

    @PostMapping("/api/v1/files")
    public ResponseEntity fileUploadTest(List<MultipartFile> files) {
        String dir = "";
        files.forEach(file -> {
            fileUtils.upload(file, dir);
            // TODO DB 파일 정보 저장
        });
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/v1/files/{id}")
    public ResponseEntity fileDeleteTest(@RequestBody String filename) {
        // TODO DB 파일 정보 조회
        fileUtils.delete(filename);
        return ResponseEntity.noContent().build();
    }

}
