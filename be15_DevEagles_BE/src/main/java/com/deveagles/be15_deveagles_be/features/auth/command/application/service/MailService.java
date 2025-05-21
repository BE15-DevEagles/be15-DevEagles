package com.deveagles.be15_deveagles_be.features.auth.command.application.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

  private final JavaMailSender mailSender;

  @Value("${spring.mail.auth-url}")
  private String authUrl;

  @Value("${spring.mail.pwd-url}")
  private String pwdUrl;

  public void sendAuthMail(String email, String authCode) throws MessagingException {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

    String subject = "Goody 회원가입 이메일 인증 요청";
    String verifyLink = authUrl + "?email=" + email + "&code=" + authCode;

    String htmlContent =
        "<h3>Goody 회원가입 인증 안내입니다. </h3>"
            + "<p>아래 버튼을 클릭하여 이메일 인증을 완료해주세요 : </p>"
            + "<a href='"
            + verifyLink
            + "' style =style='padding:10px 20px; background-color:#4CAF50; color:white; text-decoration:none;'"
            + ">이메일 인증하기</a>";

    helper.setTo(email);
    helper.setSubject(subject);
    helper.setText(htmlContent, true);

    mailSender.send(message);
  }

  public void sendFindPwdEmail(String email, String authCode) throws MessagingException {

    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

    String subject = "Goody 비밀번호 변경 이메일 인증 요청";
    String verifyLink = pwdUrl + "?email=" + email + "&code=" + authCode;

    String htmlContent =
        "<h3>Goody 비밀번호 변경 인증 안내입니다. </h3>"
            + "<p>아래 버튼을 클릭하여 이메일 인증을 완료해주세요 : </p>"
            + "<a href='"
            + verifyLink
            + "' style =style='padding:10px 20px; background-color:#4CAF50; color:white; text-decoration:none;'"
            + ">이메일 인증하기</a>";

    helper.setTo(email);
    helper.setSubject(subject);
    helper.setText(htmlContent, true);

    mailSender.send(message);
  }
}
