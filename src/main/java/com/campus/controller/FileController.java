package com.campus.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.campus.dto.ResultDTO;
import com.campus.exception.BusinessException;
import com.campus.service.storage.FileStorageService;

@RestController
@RequestMapping("/api/file")
public class FileController {

    private final FileStorageService fileStorageService;

    @Value("${campus.media.max-size:10485760}")
    private long maxFileSize;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResultDTO<String> upload(@RequestParam("file") MultipartFile file) {
        validateFile(file);
        try {
            String url = fileStorageService.store(file);
            return ResultDTO.success("上传成功", url);
        } catch (Exception e) {
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择文件");
        }
        if (file.getSize() > maxFileSize) {
            throw new BusinessException("文件大小不能超过" + (maxFileSize / 1024 / 1024) + "MB");
        }
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) ||
                !(contentType.startsWith("image/") || contentType.startsWith("video/"))) {
            throw new BusinessException("仅支持上传图片或视频文件");
        }
    }
}
