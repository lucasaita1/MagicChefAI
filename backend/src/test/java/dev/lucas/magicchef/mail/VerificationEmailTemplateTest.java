package dev.lucas.magicchef.mail;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VerificationEmailTemplateTest {

    @Test
    void escapesUserDataInHtml() {
        EmailMessage message = new VerificationEmailTemplate()
                .build("a@mail.com", "<script>x</script>", "https://app.test/verificar-email?token=abc&x=1");

        assertThat(message.html())
                .doesNotContain("<script>x</script>")
                .contains("&lt;script&gt;")
                .contains("token=abc&amp;x=1");
        assertThat(message.text()).contains("https://app.test/verificar-email?token=abc&x=1");
    }
}
