package com.campus.service.storage;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    /**
     * 存储文件并返回可访问的URL
     */
    String store(MultipartFile file) throws IOException;
    
    /**
     * 上传文件到指定目录
     * @param file 文件
     * @param directory 目录名（如avatar、event等）
     * @return 文件访问URL
     */
    default String uploadFile(MultipartFile file, String directory) {
        try {
            return store(file);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }
}
