package com.example.acv.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public Map upload(MultipartFile file, String folder) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String publicId = null;
        if (originalFilename != null) {
            int lastDot = originalFilename.lastIndexOf('.');
            String nameWithoutExt = lastDot > 0 ? originalFilename.substring(0, lastDot) : originalFilename;
            String extension = lastDot > 0 ? originalFilename.substring(lastDot).toLowerCase() : "";
            String cleanName = nameWithoutExt.replaceAll("[^a-zA-Z0-9_-]", "_");
            
            // For raw files (not PDFs or images), Cloudinary requires the extension in public_id
            boolean isRaw = !extension.equals(".pdf") && !extension.equals(".png") && !extension.equals(".jpg") && !extension.equals(".jpeg");
            if (isRaw) {
                publicId = cleanName + "_" + System.currentTimeMillis() + extension;
            } else {
                publicId = cleanName + "_" + System.currentTimeMillis();
            }
        }

        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("resource_type", "auto");
        params.put("folder", folder);
        if (publicId != null) {
            params.put("public_id", publicId);
        }

        return cloudinary.uploader().upload(file.getBytes(), params);
    }

    public String uploadImage(MultipartFile file) throws IOException {
        Map data = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "resource_type", "image",
                "folder", "acv/images"
        ));
        return data.get("secure_url").toString();
    }

    public Map uploadDocument(MultipartFile file) throws IOException {
        return this.upload(file, "acv/documents");
    }

    public void deleteFileByUrl(String url) {
        if (url == null || !url.contains("cloudinary.com")) {
            return;
        }
        try {
            String resourceType = "image";
            if (url.contains("/raw/upload/")) {
                resourceType = "raw";
            } else if (url.contains("/video/upload/")) {
                resourceType = "video";
            }

            int uploadIndex = url.indexOf("/upload/");
            if (uploadIndex == -1) return;

            String afterUpload = url.substring(uploadIndex + 8);
            int firstSlash = afterUpload.indexOf('/');
            if (firstSlash == -1) return;

            String publicIdWithExt = afterUpload.substring(firstSlash + 1);

            int lastDot = publicIdWithExt.lastIndexOf('.');
            String publicId = lastDot > 0 ? publicIdWithExt.substring(0, lastDot) : publicIdWithExt;

            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", resourceType));
        } catch (Exception e) {
            System.err.println("Failed to delete file from Cloudinary: " + e.getMessage());
        }
    }
}
