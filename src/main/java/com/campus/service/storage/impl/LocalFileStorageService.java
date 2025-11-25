package com.campus.service.storage.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import com.campus.exception.BusinessException;
import com.campus.service.storage.FileStorageService;

@Service
public class LocalFileStorageService implements FileStorageService {

    private final Path storageDirectory;
    private final String urlPrefix;

    public LocalFileStorageService(
            @Value("${campus.media.storage-path:./uploads/}") String storagePath,
            @Value("${campus.media.url-prefix:/media/}") String urlPrefix) throws IOException {
        this.storageDirectory = Paths.get(storagePath).toAbsolutePath().normalize();
        this.urlPrefix = urlPrefix;
        Files.createDirectories(this.storageDirectory);
    }

    @Override
    public String store(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件为空");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename);
        String generatedName = UUID.randomUUID().toString().replace("-", "");
        String fileName = extension != null ? generatedName + "." + extension : generatedName;

        Path targetLocation = this.storageDirectory.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return urlPrefix + fileName;
    }
}
