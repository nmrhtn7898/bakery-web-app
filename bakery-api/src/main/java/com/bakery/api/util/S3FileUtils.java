package com.bakery.api.util;

import com.amazonaws.services.s3.AmazonS3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;

@Slf4j
@Component
public class S3FileUtils {

    private final AmazonS3 amazonS3;

    private final String bucket;

    private final String path;

    public S3FileUtils(AmazonS3 amazonS3, @Value("${aws.s3.bucket}") String bucket,
                       @Value("${aws.s3.path}") String path) {
        this.amazonS3 = amazonS3;
        this.bucket = bucket;
        this.path = path;
    }

    public String upload(MultipartFile multipartFile, String dirPath) {
        String key = path + dirPath + "/" + generateRandomFileName(multipartFile);
        File file = convert(multipartFile).orElseThrow(() -> new IllegalArgumentException(format("file(%s) upload failed", key)));
        amazonS3.putObject(bucket, key, file);
        String url = amazonS3.getUrl(bucket, key).toString();
        log.debug("file({}) upload success", key);
        return url;
    }

    public void delete(String key) {
        amazonS3.deleteObject(bucket, key);
        log.debug("file({}) delete success", key);
    }

    public Optional<File> convert(MultipartFile multipartFile) {
        String filename = requireNonNull(multipartFile.getOriginalFilename());
        File file = new File(filename);
        try {
            if (file.createNewFile()) {
                try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                    fileOutputStream.write(multipartFile.getBytes());
                }
            }
        } catch (IOException e) {
            log.debug("multipartFile({}) upload convert fail", filename);
            return empty();
        }
        log.debug("multipartFile({}) upload convert success", filename);
        return of(file);
    }

    public String generateRandomFileName(MultipartFile multipartFile) {
        String originalFilename = requireNonNull(multipartFile.getOriginalFilename());
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        return currentTimeMillis() + "." + extension;
    }

}
