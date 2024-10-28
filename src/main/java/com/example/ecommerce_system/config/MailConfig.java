package com.example.ecommerce_system.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.example.com"); // Change to your SMTP host
        mailSender.setPort(587); // Change to your SMTP port

        mailSender.setUsername("u.anonymous533333@gmail.com.com"); // Your email
        mailSender.setPassword("Passwordyangu@1"); // Your email password

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }
}
