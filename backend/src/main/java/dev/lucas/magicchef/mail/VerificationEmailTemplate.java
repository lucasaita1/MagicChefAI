package dev.lucas.magicchef.mail;

import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

/**
 * Builds the "confirm your e-mail" message in the app's visual identity.
 */
@Component
public class VerificationEmailTemplate {

    public EmailMessage build(String to, String name, String link) {
        String safeName = HtmlUtils.htmlEscape(name);
        String safeLink = HtmlUtils.htmlEscape(link);
        String html = """
                <!doctype html>
                <html lang="pt-BR">
                <body style="margin:0;padding:0;background:#0c0a1d;font-family:Arial,Helvetica,sans-serif;">
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:#0c0a1d;padding:40px 16px;">
                    <tr><td align="center">
                      <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="max-width:520px;background:#161230;border:1px solid #2a2452;border-radius:20px;overflow:hidden;">
                        <tr><td style="height:4px;background:linear-gradient(90deg,#ffb347,#ff5e7e);"></td></tr>
                        <tr><td style="padding:36px 32px 4px;text-align:center;font-size:22px;font-weight:bold;letter-spacing:-0.5px;color:#f4f2ff;">Magic<span style="color:#ff8a6b;">Chef</span>AI</td></tr>
                        <tr><td style="padding:0 32px;text-align:center;">
                          <h1 style="margin:12px 0 8px;color:#f4f2ff;font-size:24px;">Confirme seu e-mail, %s!</h1>
                          <p style="margin:0 0 28px;color:#b9b4d6;font-size:15px;line-height:1.6;">
                            O Chef Magic está aquecendo as panelas. Confirme seu e-mail para liberar as receitas geradas por IA.
                          </p>
                          <a href="%s" style="display:inline-block;padding:14px 28px;border-radius:12px;background:#ff7a6b;background-image:linear-gradient(135deg,#ffb347,#ff5e7e);color:#1a0f1f;font-weight:bold;font-size:15px;text-decoration:none;">
                            Confirmar meu e-mail
                          </a>
                          <p style="margin:28px 0 0;color:#8a84ad;font-size:12.5px;line-height:1.6;">
                            O link expira em 24 horas e só pode ser usado uma vez.<br>Se você não criou uma conta no MagicChefAI, ignore este e-mail.
                          </p>
                        </td></tr>
                        <tr><td style="padding:28px 32px 32px;text-align:center;color:#5d5785;font-size:12px;">MagicChefAI · Seu chef particular movido a IA</td></tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(safeName, safeLink);
        String text = """
                Olá, %s!

                Confirme seu e-mail para liberar as receitas do Chef Magic:
                %s

                O link expira em 24 horas. Se você não criou uma conta no MagicChefAI, ignore este e-mail.
                """.formatted(name, link);
        return new EmailMessage(to, "Confirme seu e-mail no MagicChefAI", html, text);
    }
}
