package com.bread.api.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Optional;

import static java.lang.System.currentTimeMillis;
import static java.util.Objects.requireNonNull;

public abstract class FileUtils {

    public abstract String upload(MultipartFile multipartFile, String dirPath);

    public abstract void delete(String key);

    public abstract Optional<File> convert(MultipartFile multipartFile);

    public String generateRandomFileName(MultipartFile multipartFile) {
        String originalFilename = requireNonNull(multipartFile.getOriginalFilename());
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        return currentTimeMillis() + "." + extension;
    }

}
