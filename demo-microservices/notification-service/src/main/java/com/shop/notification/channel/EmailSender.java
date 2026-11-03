package com.shop.notification.channel;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.UUID;

// =============================================================================
// [UML: Deployment Diagram] External System = SMTP / Email provider.
// [UML: Sequence Diagram] msg 19 "Send confirmation" leaves the system boundary.
//
// [ARCH] Recipient address is a demo constant here. A real system resolves it
// from User Service or from a local read model -- the UML diagrams do not model
// contact data, so this is kept out of the event contracts.
// =============================================================================
@Component
public class EmailSender {

    private final JavaMailSender mailSender;
    private final String from;
    private final String demoRecipient;

    public EmailSender(JavaMailSender mailSender,
                       @Value("${notification.from}") String from,
                       @Value("${notification.demo-recipient}") String demoRecipient) {
        this.mailSender = mailSender;
        this.from = from;
        this.demoRecipient = demoRecipient;
    }

    public void sendOrderConfirmation(UUID orderId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(demoRecipient);
        message.setSubject("Order " + orderId + " confirmed");
        message.setText("Your order " + orderId + " has been confirmed.");
        mailSender.send(message); // MailHog in docker-compose, real SMTP in production
    }
}
