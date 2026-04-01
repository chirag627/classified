package com.classified.app.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@oxxlo.in}")
    private String fromEmail;

    @Override
    public void sendEmailVerification(String toEmail, String name, String verificationLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            String sender = (fromEmail != null && !fromEmail.isBlank()) ? fromEmail : "noreply@oxxlo.in";
            helper.setFrom(sender);
            helper.setTo(toEmail);
            helper.setSubject("Verify your Oxxlo email address");

            String html = """
                    <div style="font-family:Arial,sans-serif;max-width:520px;margin:0 auto;padding:24px;background:#f7f8fa;">
                        <div style="background:white;border-radius:10px;border:1px solid #e0e0e0;padding:32px;">
                            <div style="text-align:center;margin-bottom:28px;">
                                <span style="background:#002f34;color:#ffce32;font-weight:900;font-size:1.4rem;padding:6px 16px;border-radius:6px;letter-spacing:-1px;">Oxxlo</span>
                            </div>
                            <h2 style="font-size:20px;color:#002f34;margin-bottom:8px;">Hi %s,</h2>
                            <p style="color:#555;line-height:1.6;">
                                Thanks for joining Oxxlo! Please verify your email address by clicking the button below.
                                This link will expire in <strong>24 hours</strong>.
                            </p>
                            <div style="text-align:center;margin:32px 0;">
                                <a href="%s"
                                   style="background:#002f34;color:#ffce32;padding:14px 32px;text-decoration:none;border-radius:6px;font-weight:700;font-size:15px;display:inline-block;">
                                    Verify Email Address
                                </a>
                            </div>
                            <p style="color:#999;font-size:12px;margin-top:24px;">
                                If you didn't create an Oxxlo account, you can safely ignore this email.
                            </p>
                        </div>
                    </div>
                    """.formatted(name, verificationLink);

            helper.setText(html, true);
            mailSender.send(message);
            log.info("Verification email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send verification email");
        }
    }
}
