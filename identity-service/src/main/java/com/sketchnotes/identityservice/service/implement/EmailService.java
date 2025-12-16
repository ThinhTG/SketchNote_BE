package com.sketchnotes.identityservice.service.implement;

import com.sketchnotes.identityservice.dtos.response.EmailDetail;
import com.sketchnotes.identityservice.exception.AppException;
import com.sketchnotes.identityservice.exception.ErrorCode;
import com.sketchnotes.identityservice.service.interfaces.IUserService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final SpringTemplateEngine templateEngine;
    private final JavaMailSender javaMailSender;
    private final IUserService userService;
    @Value("${spring.mail.username}")
    @NonFinal
    private String emailFrom;

    public void sendMailTemplate(EmailDetail emailDetail, Map<String, Object> variables, String template )
    {
        try {
            Context context = new Context();

            context.setVariables(variables);

            String html = templateEngine.process(template, context);

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(emailFrom);
            helper.setTo(emailDetail.getRecipient());
            helper.setSubject(emailDetail.getSubject());
            helper.setText(html, true);

            javaMailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new AppException(ErrorCode.EMAIL_SENDING_FAILED);
        }
    }


}
