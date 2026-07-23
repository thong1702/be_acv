package com.example.acv.service;

import com.example.acv.entity.ContactRequest;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.mail.target-email:Acvvaluation@gmail.com}")
    private String targetEmail;

    @Async
    public void sendContactNotificationEmail(ContactRequest request) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Website ACV Valuation");
            helper.setTo(targetEmail);
            helper.setSubject("🔔 [THÔNG BÁO] Yêu cầu tư vấn mới từ: " + request.getFullName());

            String formattedDate = request.getCreatedAt() != null ?
                    request.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy")) : "";

            String htmlContent = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e2e8f0; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.05);\">" +
                    "<div style=\"background: #2b5f97; padding: 24px; text-align: center; color: #ffffff;\">" +
                    "<h2 style=\"margin: 0; font-size: 20px; text-transform: uppercase;\">YÊU CẦU TƯ VẤN MỚI</h2>" +
                    "<p style=\"margin: 6px 0 0 0; font-size: 14px; opacity: 0.9;\">Công ty TNHH Tư vấn và Định giá ACV</p>" +
                    "</div>" +
                    "<div style=\"padding: 24px; background: #ffffff; color: #334155; line-height: 1.6;\">" +
                    "<p>Xin chào Ban Quản trị ACV,</p>" +
                    "<p>Hệ thống vừa nhận được một đăng ký tư vấn dịch vụ mới từ khách hàng qua Website với chi tiết như sau:</p>" +
                    "<table style=\"width: 100%; border-collapse: collapse; margin-top: 16px;\">" +
                    "<tr style=\"background: #f8fafc;\"><td style=\"padding: 10px 14px; font-weight: bold; width: 140px; border-bottom: 1px solid #e2e8f0;\">Họ và tên:</td><td style=\"padding: 10px 14px; border-bottom: 1px solid #e2e8f0; color: #0f3f75; font-weight: bold;\">" + request.getFullName() + "</td></tr>" +
                    "<tr><td style=\"padding: 10px 14px; font-weight: bold; border-bottom: 1px solid #e2e8f0;\">Số điện thoại:</td><td style=\"padding: 10px 14px; border-bottom: 1px solid #e2e8f0; color: #ef4444; font-weight: bold;\">" + request.getPhone() + "</td></tr>" +
                    "<tr style=\"background: #f8fafc;\"><td style=\"padding: 10px 14px; font-weight: bold; border-bottom: 1px solid #e2e8f0;\">Email:</td><td style=\"padding: 10px 14px; border-bottom: 1px solid #e2e8f0;\">" + (request.getEmail() != null && !request.getEmail().isBlank() ? request.getEmail() : "Không cung cấp") + "</td></tr>" +
                    "<tr><td style=\"padding: 10px 14px; font-weight: bold; border-bottom: 1px solid #e2e8f0;\">Dịch vụ quan tâm:</td><td style=\"padding: 10px 14px; border-bottom: 1px solid #e2e8f0; color: #2b5f97; font-weight: bold;\">" + (request.getServiceType() != null ? request.getServiceType() : "Tư vấn tổng quát") + "</td></tr>" +
                    "<tr style=\"background: #f8fafc;\"><td style=\"padding: 10px 14px; font-weight: bold; border-bottom: 1px solid #e2e8f0;\">Thời gian gửi:</td><td style=\"padding: 10px 14px; border-bottom: 1px solid #e2e8f0;\">" + formattedDate + "</td></tr>" +
                    "</table>" +
                    "<div style=\"margin-top: 20px; padding: 16px; background: #eff6ff; border-left: 4px solid #3b82f6; border-radius: 4px;\">" +
                    "<strong style=\"display: block; margin-bottom: 6px; color: #1e40af;\">Nội dung / Lời nhắn từ khách hàng:</strong>" +
                    "<p style=\"margin: 0; font-style: italic; color: #1e3a8a;\">\"" + (request.getContent() != null && !request.getContent().isBlank() ? request.getContent() : "Không có lời nhắn bổ sung") + "\"</p>" +
                    "</div>" +
                    "<div style=\"margin-top: 24px; text-align: center;\">" +
                    "<a href=\"https://acv-company.com/admin/contacts\" style=\"display: inline-block; background: #2b5f97; color: #ffffff; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; font-size: 14px;\">XEM TRONG TRANG QUẢN TRỊ ADMIN</a>" +
                    "</div>" +
                    "</div>" +
                    "<div style=\"background: #f1f5f9; padding: 16px; text-align: center; color: #64748b; font-size: 12px;\">" +
                    "Email này được gửi tự động từ hệ thống Website Công ty TNHH Tư vấn và Định giá ACV." +
                    "</div>" +
                    "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Đã gửi email thông báo liên hệ thành công tới: {}", targetEmail);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email thông báo liên hệ: {}", e.getMessage(), e);
        }
    }
}
