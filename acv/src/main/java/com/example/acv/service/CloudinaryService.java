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
     * Dùng Cloudinary privateDownload() để tạo URL có chữ ký hợp lệ,
     * sau đó tải về server-side để bypass 401 Unauthorized.
     */
    public byte[] downloadFileBytes(String fileUrl) {
        if (fileUrl == null) return null;
        try {
            // Phân tích resource_type và public_id từ URL
            String resourceType = "raw";
            if (fileUrl.contains("/image/upload/")) resourceType = "image";
            else if (fileUrl.contains("/video/upload/")) resourceType = "video";

            // Trích xuất public_id + extension từ URL
            int uploadIdx = fileUrl.indexOf("/upload/");
            if (uploadIdx == -1) return downloadFromUrl(fileUrl);

            String afterUpload = fileUrl.substring(uploadIdx + 8);
            // Bỏ version (vXXXXXX/)
            if (afterUpload.matches("v\\d+/.*")) {
                afterUpload = afterUpload.substring(afterUpload.indexOf('/') + 1);
            }
            // afterUpload bây giờ là: acv/documents/TB_1626.pdf
            String publicId = afterUpload;
            String format = null;
            if (!resourceType.equals("raw")) {
                int dotIdx = afterUpload.lastIndexOf('.');
                if (dotIdx > 0) {
                    format = afterUpload.substring(dotIdx + 1);
                    publicId = afterUpload.substring(0, dotIdx);
                }
            }

            // Tạo private download URL có chữ ký hợp lệ (sử dụng API Key + API Secret)
            String signedUrl = cloudinary.url()
                    .resourceType(resourceType)
                    .type("upload")
                    .signed(true)
                    .generate(afterUpload);

            byte[] result = downloadFromUrl(signedUrl);
            if (result != null) return result;

            // Thử lại với privateDownload
            @SuppressWarnings("unchecked")
            String privateUrl = cloudinary.privateDownload(publicId, format,
                    ObjectUtils.asMap("resource_type", resourceType));
            return downloadFromUrl(privateUrl);

        } catch (Exception e) {
            System.err.println("downloadFileBytes error: " + e.getMessage());
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
            System.err.println("downloadFromUrl HTTP " + code + " for: " + url);
            return null;
        } catch (Exception e) {
            System.err.println("downloadFromUrl error: " + e.getMessage());
            return null;
        }
    }
}

