package dev.lucas.magicchef.mail;

/**
 * Port to the e-mail provider. Production uses Resend; tests replace it with a mock.
 */
public interface EmailSender {

    /**
     * @throws EmailDeliveryException when the provider rejects or cannot be reached
     */
    void send(EmailMessage message);
}
