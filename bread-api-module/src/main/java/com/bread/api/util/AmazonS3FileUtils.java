package com.bread.api.util;

import com.amazonaws.services.s3.AmazonS3Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;

@Slf4j
@Profile(value = {"dev", "prod"})
@Component
public class AmazonS3FileUtils extends FileUtils {

    private final AmazonS3Client amazonS3Client;

    private final String bucket;

    private final String path;

    public AmazonS3FileUtils(AmazonS3Client amazonS3Client, @Value("${cloud.aws.s3.bucket}") String bucket,
                             @Value("${file.key.path}") String path) {
        this.amazonS3Client = amazonS3Client;
        this.bucket = bucket;
        this.path = path;
    }

    @Override
    public String upload(MultipartFile multipartFile, String dirPath) {
        String key = path + dirPath + "/" + generateRandomFileName(multipartFile);
        File file = convert(multipartFile).orElseThrow(() -> new IllegalArgumentException(format("file(%s) upload failed", key)));
        amazonS3Client.putObject(bucket, key, file);
        String url = amazonS3Client.getUrl(bucket, key).toString();
        log.debug("file({}) upload success", key);
        return url;
    }

    @Override
    public void delete(String key) {
        amazonS3Client.deleteObject(bucket, key);
        log.debug("file({}) delete success", key);
    }

    @Override
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

}
