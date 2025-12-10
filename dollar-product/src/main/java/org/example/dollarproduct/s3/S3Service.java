
package org.example.dollarproduct.s3;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3Service {

    @Autowired(required = false)
    private S3Client s3;

    @Value("${storage.mode:local}")
    private String storageMode;

    @Value("${storage.local.base-path:uploads}")
    private String localBasePath;

    public void putObject(String bucketName, String key, MultipartFile file) throws IOException {
        if ("s3".equalsIgnoreCase(storageMode) && s3 != null) {
            String contentType = file.getContentType();
            if (contentType == null || contentType.isBlank()) {
                contentType = "application/octet-stream";
            }
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .contentType(contentType)
                .key(key)
                .acl(ObjectCannedACL.PUBLIC_READ)
                .build();
            RequestBody requestBody = RequestBody
                .fromInputStream(file.getInputStream(), file.getSize());
            s3.putObject(objectRequest, requestBody);
            return;
        }
        // local mode: save to filesystem under localBasePath/key
        Path base = Paths.get(localBasePath).toAbsolutePath();
        Path target = base.resolve(key);
        Files.createDirectories(target.getParent());
        Files.copy(file.getInputStream(), target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }
}
