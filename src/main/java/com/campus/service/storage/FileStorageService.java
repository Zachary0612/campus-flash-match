package com.campus.service.storage;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    /**
     * 存储文件并返回可访问的URL
     */
    String store(MultipartFile file) throws IOException;
}
