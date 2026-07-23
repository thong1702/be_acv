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

    /**
     * Chuyển tiếng Việt có dấu sang không dấu
     */
    private String removeVietnameseDiacritics(String input) {
        if (input == null) return "";
        String normalized = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                         .replace("đ", "d").replace("Đ", "D");
    }

    public Map upload(MultipartFile file, String folder) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String publicId = null;
        boolean isRaw = false;

        if (originalFilename != null) {
            int lastDot = originalFilename.lastIndexOf('.');
            String nameWithoutExt = lastDot > 0 ? originalFilename.substring(0, lastDot) : originalFilename;
            String extension = lastDot > 0 ? originalFilename.substring(lastDot).toLowerCase() : "";
            // Bỏ dấu tiếng Việt rồi thay ký tự đặc biệt bằng _
            String cleanName = removeVietnameseDiacritics(nameWithoutExt).replaceAll("[^a-zA-Z0-9_\\-.]", "_");

            // Raw files (.pdf, .docx, .xlsx, .doc) require extension in public_id and resource_type = raw
            isRaw = extension.equals(".pdf") || (!extension.equals(".png") && !extension.equals(".jpg") && !extension.equals(".jpeg"));
            if (isRaw) {
                publicId = cleanName + extension;
            } else {
                publicId = cleanName;
            }
        }

        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("resource_type", isRaw ? "raw" : "auto");
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

    public String getSignedUrl(String fileUrl) {
        if (fileUrl == null || !fileUrl.contains("cloudinary.com")) {
            return fileUrl;
        }
        try {
            int uploadIndex = fileUrl.indexOf("/upload/");
            if (uploadIndex == -1) return fileUrl;

            String resourceType = "raw";
            if (fileUrl.contains("/image/upload/")) {
                resourceType = "image";
            } else if (fileUrl.contains("/video/upload/")) {
                resourceType = "video";
            }

            String afterUpload = fileUrl.substring(uploadIndex + 8);
            if (afterUpload.startsWith("v")) {
                int firstSlash = afterUpload.indexOf('/');
                if (firstSlash != -1) {
                    afterUpload = afterUpload.substring(firstSlash + 1);
                }
            }

            return cloudinary.url()
                    .resourceType(resourceType)
                    .type("upload")
                    .signed(true)
                    .generate(afterUpload);
        } catch (Exception e) {
            System.err.println("Error generating Cloudinary signed URL: " + e.getMessage());
            return fileUrl;
        }
    }

    public byte[] downloadFileBytes(String fileUrl) {
        if (fileUrl == null) return null;
        try {
            String apiKey = cloudinary.config.apiKey;
            String apiSecret = cloudinary.config.apiSecret;
            String targetUrl = getSignedUrl(fileUrl);

            java.net.URL url = new java.net.URL(targetUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(20000);

            if (apiKey != null && apiSecret != null) {
                String auth = apiKey + ":" + apiSecret;
                String encodedAuth = java.util.Base64.getEncoder().encodeToString(auth.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == java.net.HttpURLConnection.HTTP_MOVED_TEMP || responseCode == java.net.HttpURLConnection.HTTP_MOVED_PERM) {
                String newUrl = conn.getHeaderField("Location");
                if (newUrl != null) {
                    conn = (java.net.HttpURLConnection) new java.net.URL(newUrl).openConnection();
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                    if (apiKey != null && apiSecret != null) {
                        String auth = apiKey + ":" + apiSecret;
                        String encodedAuth = java.util.Base64.getEncoder().encodeToString(auth.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                        conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
                    }
                }
            }

            try (java.io.InputStream is = conn.getInputStream()) {
                return is.readAllBytes();
            }
        } catch (Exception e) {
            System.err.println("Error downloading file bytes from Cloudinary: " + e.getMessage());
            return null;
        }
    }
}
