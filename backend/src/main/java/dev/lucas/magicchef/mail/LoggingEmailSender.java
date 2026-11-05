package dev.lucas.magicchef.mail;

import lombok.extern.slf4j.Slf4j;

/**
 * Local development fallback used when RESEND_API_KEY is empty: prints the e-mail to the log.
 */
@Slf4j
public class LoggingEmailSender implements EmailSender {

    @Override
    public void send(EmailMessage message) {
        log.warn("RESEND_API_KEY not set. E-mail to {} not sent:\n{}", message.to(), message.text());
    }
}
