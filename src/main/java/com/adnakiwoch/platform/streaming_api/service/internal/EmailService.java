package com.adnakiwoch.platform.streaming_api.service.internal;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
  private final JavaMailSender javaMailSender;

  EmailService(JavaMailSender javaMailSender) {
    this.javaMailSender = javaMailSender;
  }

  @Async
  public void sendEmailToUser(String toEmail, String fromEmail, String subject, String message) {
    SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
    simpleMailMessage.setTo(toEmail);
    simpleMailMessage.setFrom(fromEmail);
    simpleMailMessage.setSubject(subject);
    simpleMailMessage.setText(message);
    javaMailSender.send(simpleMailMessage);
  }
}
