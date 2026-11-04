package dev.lucas.magicchef.ai;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * First defense layer against prompt injection. Runs before any token is sent to the AI:
 * <ol>
 *     <li>{@link #sanitize} normalizes Unicode (folds look-alike characters), strips invisible
 *     control characters and markup symbols that could fake prompt delimiters;</li>
 *     <li>{@link #inspect} rejects text matching known jailbreak / instruction-override phrases
 *     in Portuguese and English.</li>
 * </ol>
 * The system prompt, the output schema and the canary check in {@link OpenAiRecipeClient} are
 * the next layers, so this list does not need to be exhaustive.
 */
@Component
public class PromptGuard {

    private static final Pattern INVISIBLE = Pattern.compile("[\\p{Cc}\\p{Cf}&&[^\\n]]");
    private static final Pattern MARKUP = Pattern.compile("[<>{}\\[\\]`#|\\\\$^~*_=]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");

    private static final List<Pattern> INJECTION_PATTERNS = List.of(
            // "ignore as instruções anteriores", "ignore all previous rules"
            "ignor\\w*\\s+(\\w+\\s+){0,4}(instruc|instruct|regra|rule|comando|prompt|orientac|diretriz)",
            "(esquec|desconsider|disregard|forget|override|sobrescrev|bypass|burl)\\w*\\s+(\\w+\\s+){0,4}"
                    + "(instruc|instruct|regra|rule|prompt|anterior|previous|acima|above|tudo|everything|restric)",
            "(system|sistema)\\s*(prompt|message|mensagem)",
            "prompt\\s+(do|de|inicial|original)\\s*(sistema)?",
            "(revel|mostr|exib|imprim|repit|escrev|cont[ae]|print|reveal|show|repeat|display|tell)\\w*\\s+(\\w+\\s+){0,4}"
                    + "(prompt|instruc|instruct|regras|rules|configurac|codigo interno|secret|segredo|chave|api key)",
            "(you are|you're)\\s+(now|no longer)|voce\\s+(agora\\s+e|nao\\s+e\\s+mais)|a partir de agora,?\\s+voce"
                    + "|from now on,?\\s+you",
            "\\b(act|aja|atue|comporte-se|behave|responda)\\s+(as|como)\\s+(um|uma|an?|o|a)?\\s*"
                    + "(\\w+\\s+)?(ia|ai|assistente|assistant|hacker|programador|developer|sistema|admin|dan|gpt|chatgpt)",
            "\\b(finja|pretend|roleplay|role-play|simule\\s+ser|imagine\\s+que\\s+voce|faca\\s+de\\s+conta)\\b",
            "jailbreak|\\bdan\\b|developer\\s+mode|modo\\s+(desenvolvedor|dev|deus|irrestrito|admin)"
                    + "|sem\\s+restric|no\\s+restrictions|unfiltered|sem\\s+filtro",
            "\\b(role|papel)\\s*:\\s*(system|assistant|sistema)|\\b(system|assistant|sistema|assistente)\\s*:",
            "\\b(novas?|new)\\s+(instruc|instruct|regras?|rules?|tarefas?|tasks?|ordens?)",
            "base64|rot13|\\beval\\s*\\(|<\\s*script|javascript:|\\bsudo\\b"
    ).stream().map(Pattern::compile).toList();

    public String sanitize(String raw) {
        if (raw == null) {
            return "";
        }
        String text = Normalizer.normalize(raw, Normalizer.Form.NFKC);
        text = INVISIBLE.matcher(text).replaceAll(" ");
        text = MARKUP.matcher(text).replaceAll(" ");
        return WHITESPACE.matcher(text).replaceAll(" ").trim();
    }

    /**
     * @throws PromptInjectionException when the text looks like an attempt to hijack the model
     */
    public void inspect(String text) {
        if (isSuspicious(text)) {
            throw new PromptInjectionException();
        }
    }

    public boolean isSuspicious(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String folded = fold(text);
        return INJECTION_PATTERNS.stream().anyMatch(pattern -> pattern.matcher(folded).find());
    }

    private static String fold(String text) {
        String withoutAccents = DIACRITICS.matcher(Normalizer.normalize(text, Normalizer.Form.NFD)).replaceAll("");
        return WHITESPACE.matcher(withoutAccents.toLowerCase(Locale.ROOT)).replaceAll(" ");
    }
}
