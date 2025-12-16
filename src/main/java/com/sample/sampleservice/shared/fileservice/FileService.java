package com.sample.sampleservice.shared.fileservice;

// import com.sample.sampleservice.feature.document.bo.DocumentBo;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface FileService {

    // Optional<DocumentBo> uploadDocument(MultipartFile file);

    Optional<String> createBucket(String bucketName);

    Optional<String> uploadBucket(String bucketName, MultipartFile multipartFile);

    Optional<String> generatePresignedUrl(String key);

    // Optional<DocumentBo> uploadForEntity(MultipartFile multipartFile, String entityType, String entityId);

    boolean isFileSizeExceeded(MultipartFile file, String limit);

    void checkSizeExceeded(MultipartFile file, String limit, String type);
    
}
