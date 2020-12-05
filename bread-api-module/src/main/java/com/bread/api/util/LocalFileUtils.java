package com.bread.api.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static com.sun.javafx.PlatformUtil.isWindows;
import static java.lang.String.format;

@Slf4j
@Profile("default")
@Component
public class LocalFileUtils extends FileUtils {

    private final String path;

    public LocalFileUtils(@Value("${file.key.path}") String path) {
        if (isWindows()) {
            path = "c:" + path;
        }
        this.path = path;
    }

    @PostConstruct
    public void postConstruct() {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        } else if (file.exists() && !file.isDirectory()) {
            file.delete();
            file.mkdirs();
        }
    }

    @Override
    public String upload(MultipartFile multipartFile, String dirPath) {
        String key = path + dirPath + "/" + generateRandomFileName(multipartFile);
        try {
            multipartFile.transferTo(new File(key));
        } catch (IOException e) {
            throw new IllegalArgumentException(format("file(%s) upload failed", key));
        }
        log.debug("file({}) upload success", key);
        return key;
    }

    @Override
    public void delete(String key) {
        File file = new File(key);
        if (!file.exists()) {
            throw new IllegalArgumentException(format("file(%s) is not exists", key));
        }
        file.delete();
        log.debug("file({}) delete success", key);
    }

    @Override
    public Optional<File> convert(MultipartFile multipartFile) {
        return Optional.empty();
    }

}
