package dev.lucas.magicchef.verification;

public class EmailNotVerifiedException extends RuntimeException {

    public EmailNotVerifiedException() {
        super("Confirme seu e-mail para pedir receitas ao Chef Magic. Enviamos um link para a sua caixa de entrada.");
    }
}
