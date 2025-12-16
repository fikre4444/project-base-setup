package com.sample.sampleservice.shared.fileservice.impl;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.sample.sampleservice.shared.error.domain.ErrorKey;
// import com.sample.sampleservice.feature.document.bo.DocumentBo;
// import com.sample.sampleservice.feature.document.exception.DocumentErrorKey;
import com.sample.sampleservice.shared.error.domain.GeneratorException;
import com.sample.sampleservice.shared.fileservice.FileService;
import com.sample.sampleservice.shared.fileservice.data.FileSizeResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    @Autowired
    private AmazonS3 amazonS3;

    @Value("${minio.bucket-name}")
    private String bucketName;

    private static final long DEFAULT_LIMIT_BYTES = 20L * 1024L * 1024L; // 20 MB

    private PutObjectResult uploadFile(String keyName, MultipartFile file) throws IOException {
        File fileObj = convertMultiPartFileToFile(file);
        PutObjectResult result = amazonS3.putObject(bucketName, keyName, fileObj);
        fileObj.delete();
        return result;
    }

    private PutObjectResult uploadFile(String name, String keyName, MultipartFile file) throws IOException {
        File fileObj = convertMultiPartFileToFile(file);
        PutObjectResult result = amazonS3.putObject(String.format("%s/%s", bucketName, name), keyName, fileObj);
        fileObj.delete();
        return result;
    }

    private File convertMultiPartFileToFile(MultipartFile file) throws IOException {
        File convertedFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convertedFile);
        fos.write(file.getBytes());
        fos.close();
        return convertedFile;
    }

    public String getFileUrl(String keyName) {
        return amazonS3.getUrl(bucketName, keyName).toString();
    }

    public String getFileUrl(String directoryName, String keyName) {
        return amazonS3.getUrl(String.format("%s/%s", bucketName, directoryName), keyName).toString();
    }

    // @Override
    // public Optional<DocumentBo> uploadDocument(MultipartFile multipartFile) {
    //     String originalFileName = multipartFile.getOriginalFilename();

    //     String extension = getFileExtension(originalFileName).orElse("bin"); //.bin if non found
    //     String uniqueKey = UUID.randomUUID().toString() + "." + extension;
    //     log.debug("Uploading file '{}' with unique key '{}'", originalFileName, uniqueKey);

    //     String fileUrl = "";
    //     try {
    //         uploadFile(uniqueKey, multipartFile);
    //         fileUrl = getFileUrl(uniqueKey);
    //     } catch (Exception e) {
    //         log.error("uploading document failed {}", e.getMessage(), e);
    //     }
    //     return Optional.of(DocumentBo.builder().url(fileUrl).documentType(multipartFile.getContentType()).fileName(originalFileName).build());
    // }

    @Override
    public Optional<String> createBucket(String name) {
        // Create an empty content for the folder
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(0);

        // Create an empty input stream
        ByteArrayInputStream emptyContent = new ByteArrayInputStream(new byte[0]);

        // Ensure the folderPath ends with a slash
        String folderPathWithSlash = name.endsWith("/") ? name : name + "/";

        // Put the empty object to create the folder
        try{
            amazonS3.putObject(bucketName, folderPathWithSlash, emptyContent, metadata);
        }catch(Exception e){
            log.error("creating the folder failed", e.getMessage(), e);
        }

        return Optional.of(name);
    }

    @Override
    public Optional<String> uploadBucket(String directoryName, MultipartFile multipartFile) {

        String originalFileName = multipartFile.getOriginalFilename();
        
        String extension = getFileExtension(originalFileName).orElse("bin");
        String uniqueKey = UUID.randomUUID().toString() + "." + extension;
        String fileUrl = "";
        log.debug("Uploading file '{}' to directory '{}' with unique key '{}'", originalFileName, directoryName, uniqueKey);
        try {
            uploadFile(directoryName, uniqueKey, multipartFile);
            fileUrl = getFileUrl(directoryName, uniqueKey);
        } catch (Exception e) {
            log.error("uploading document failed {}", e.getMessage(), e);
        }
        return Optional.of(fileUrl);

    }

    
    @Override
    public Optional<String> generatePresignedUrl(String key) {
        try {
            Date expiration = new Date();
            long expTimeMillis = expiration.getTime();
            expTimeMillis += 1000 * 60 * 15; // Convert minutes to milliseconds (curerntly 15 mintues)
            expiration.setTime(expTimeMillis);

            GeneratePresignedUrlRequest generatePresignedUrlRequest =
                    new GeneratePresignedUrlRequest(bucketName, key)
                            .withMethod(HttpMethod.GET)
                            .withExpiration(expiration);

            URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
            return Optional.of(url.toString());
        } catch (Exception e) {
            log.error("Failed to generate pre-signed URL for key [{}]: {}", key, e.getMessage(), e);
            return Optional.empty();
        }
    }

    // @Override
    // public Optional<DocumentBo> uploadForEntity(MultipartFile multipartFile, String entityType, String entityId) {
    //     if (multipartFile == null || multipartFile.isEmpty()) {
    //         log.warn("Attempted to upload an empty file for entity {}/{}", entityType, entityId);
    //         return Optional.empty();
    //     }

    //     String originalFileName = multipartFile.getOriginalFilename();
    //     String extension = getFileExtension(originalFileName).orElse("bin");
    //     String uniqueFileName = UUID.randomUUID().toString() + "." + extension;

    //     String fileKey = String.format("%s/%s/%s", entityType, entityId, uniqueFileName);

    //     log.debug("Uploading file '{}' with key '{}' to bucket '{}'", originalFileName, fileKey, bucketName);
    //     String fileUrl = "";
    //     try {
    //         ObjectMetadata metadata = new ObjectMetadata();
    //         metadata.setContentType(multipartFile.getContentType());
    //         metadata.setContentLength(multipartFile.getSize());
    //         amazonS3.putObject(bucketName, fileKey, multipartFile.getInputStream(), metadata);
            
    //         fileUrl = amazonS3.getUrl(bucketName, fileKey).toString();
    //     } catch (Exception e) {
    //         log.error("Failed to upload document '{}' for key '{}': {}", originalFileName, fileKey, e.getMessage(), e);
    //     }
    //     return Optional.of(DocumentBo.builder().url(fileUrl).documentType(multipartFile.getContentType()).fileName(originalFileName).build());
    // }

    @Override
    public boolean isFileSizeExceeded(MultipartFile file, String limit) {
        long limitBytes = parseSizeToBytes(limit);
        return file.getSize() > limitBytes;
    }

    @Override
    public void checkSizeExceeded(MultipartFile file, String limit, String type) {

        FileSizeResult result = parseSize(limit);

        if (file.getSize() > result.getBytes()) {
            String message;

            if (result.isUsedDefault()) {
                message = type + " document file size exceeded the default limit of 6MB";
            } else {
                message = type + " document file size exceeded the limit of " + limit;
            }

            throw GeneratorException
                    .badRequest(new ErrorKey() {
                        @Override
                        public String get() {
                            return "failed.to.upload";
                        }
                    })
                    .message(message)
                    .build();
        }
    }

    private long parseSizeToBytes(String size) {
        if (size == null || size.isBlank()) {
            return DEFAULT_LIMIT_BYTES;
        }

        try {
            size = size.trim().toUpperCase();

            long multiplier;

            if (size.endsWith("KB")) {
                multiplier = 1024L;
                size = size.replace("KB", "");
            } else if (size.endsWith("MB")) {
                multiplier = 1024L * 1024L;
                size = size.replace("MB", "");
            } else if (size.endsWith("GB")) {
                multiplier = 1024L * 1024L * 1024L;
                size = size.replace("GB", "");
            } else if (size.endsWith("B")) {
                multiplier = 1L;
                size = size.replace("B", "");
            } else {
                // Unknown unit → default to 20MB
                return DEFAULT_LIMIT_BYTES;
            }

            long value = Long.parseLong(size.trim());
            return value * multiplier;

        } catch (Exception ex) {
            // Any parse error → default to 20MB
            return DEFAULT_LIMIT_BYTES;
        }
    }

    private FileSizeResult parseSize(String size) {
        if (size == null || size.isBlank()) {
            return new FileSizeResult(DEFAULT_LIMIT_BYTES, true);
        }

        try {
            size = size.trim().toUpperCase();

            long multiplier;

            if (size.endsWith("KB")) {
                multiplier = 1024L;
                size = size.replace("KB", "");
            } else if (size.endsWith("MB")) {
                multiplier = 1024L * 1024L;
                size = size.replace("MB", "");
            } else if (size.endsWith("GB")) {
                multiplier = 1024L * 1024L * 1024L;
                size = size.replace("GB", "");
            } else if (size.endsWith("B")) {
                multiplier = 1L;
                size = size.replace("B", "");
            } else {
                return new FileSizeResult(DEFAULT_LIMIT_BYTES, true);
            }

            long value = Long.parseLong(size.trim());
            return new FileSizeResult(value * multiplier, false);

        } catch (Exception ex) {
            return new FileSizeResult(DEFAULT_LIMIT_BYTES, true);
        }
    }

    private Optional<String> getFileExtension(String filename) {
        if (filename == null) {
            return Optional.empty();
        }
        int lastIndexOfDot = filename.lastIndexOf(".");
        if (lastIndexOfDot == -1 || lastIndexOfDot == 0) {
            return Optional.empty(); // No extension or it's a hidden file like .bashrc
        }
        return Optional.of(filename.substring(lastIndexOfDot + 1));
    }
}
