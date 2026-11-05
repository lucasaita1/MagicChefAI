package dev.lucas.magicchef.verification;

import dev.lucas.magicchef.common.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

/**
 * Rejects well-known throwaway inbox providers at sign up. Verification by e-mail is the real
 * gate; this only stops the laziest abuse before an e-mail is even sent.
 */
@Component
public class DisposableEmailPolicy {

    private static final Set<String> DISPOSABLE_DOMAINS = Set.of(
            "mailinator.com", "guerrillamail.com", "guerrillamail.net", "sharklasers.com", "10minutemail.com",
            "10minutemail.net", "temp-mail.org", "tempmail.com", "tempmail.net", "tempmailo.com", "yopmail.com",
            "yopmail.net", "trashmail.com", "getnada.com", "nada.email", "dispostable.com", "maildrop.cc",
            "throwawaymail.com", "fakeinbox.com", "mintemail.com", "mohmal.com", "emailondeck.com",
            "mailnesia.com", "tempr.email", "discard.email", "burnermail.io", "moakt.com", "spamgourmet.com");

    public void check(String email) {
        String domain = email.substring(email.lastIndexOf('@') + 1).toLowerCase(Locale.ROOT);
        if (DISPOSABLE_DOMAINS.contains(domain)) {
            throw new BadRequestException("E-mails temporários não são aceitos. Use um e-mail pessoal ou profissional.");
        }
    }
}
