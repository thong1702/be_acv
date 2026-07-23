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

    /**
     * Tải file từ Cloudinary về dạng byte[].
     * Dùng Signed CDN URL (res.cloudinary.com/s--SIGNATURE--/) để bypass 401.
     * Cloudinary /raw/download Admin API endpoint không tồn tại nên không dùng privateDownload.
     */
    public byte[] downloadFileBytes(String fileUrl) {
        if (fileUrl == null) return null;
        try {
            // Xác định resource_type
            String resourceType = "raw";
            if (fileUrl.contains("/image/upload/")) resourceType = "image";
            else if (fileUrl.contains("/video/upload/")) resourceType = "video";

            int uploadIdx = fileUrl.indexOf("/upload/");
            if (uploadIdx == -1) return downloadFromUrl(fileUrl);

            // afterUploadRaw bao gồm version: "v1784780569/acv/documents/TB_1626.pdf"
            String afterUploadRaw = fileUrl.substring(uploadIdx + 8);

            // Trích xuất version nếu có
            String version = null;
            String afterUpload = afterUploadRaw;
            if (afterUploadRaw.matches("v\\d+/.*")) {
                int slashIdx = afterUploadRaw.indexOf('/');
                version = afterUploadRaw.substring(1, slashIdx); // "1784780569"
                afterUpload = afterUploadRaw.substring(slashIdx + 1); // "acv/documents/TB_1626.pdf"
            }

            // Tạo Signed CDN URL có version → res.cloudinary.com/cloud/raw/upload/s--SIG--/vXXX/publicId
            com.cloudinary.Url urlBuilder = cloudinary.url()
                    .resourceType(resourceType)
                    .type("upload")
                    .secure(true)
                    .signed(true);
            if (version != null) urlBuilder = urlBuilder.version(version);
            String signedUrl = urlBuilder.generate(afterUpload);

            byte[] result = downloadFromUrl(signedUrl);
            if (result != null) return result;

            // Fallback: thử lại với image/download (chỉ cho image/video không phải raw)
            if (!resourceType.equals("raw")) {
                @SuppressWarnings("unchecked")
                String privateUrl = cloudinary.privateDownload(afterUpload, null,
                        ObjectUtils.asMap("resource_type", resourceType));
                return downloadFromUrl(privateUrl);
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] downloadFromUrl(String url) {
        try {
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            int code = conn.getResponseCode();
            if (code >= 200 && code < 300) {
                try (java.io.InputStream is = conn.getInputStream()) {
                    return is.readAllBytes();
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}

