package xyz.alexcrea.cuanvil.lang;

import org.bukkit.configuration.ConfigurationSection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.alexcrea.cuanvil.tests.SharedCustomAnvilTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Checks the integrity between the registered {@link Message} keys
 * (MsgCommand, MsgUI, MsgError, MsgWarning) and the default language resource (lang/en.yml).
 *
 * <p>The plugin is initialised once by {@link SharedCustomAnvilTest#setUp()}, so the
 * default language is loaded and available for the whole test class.
 */
public class DefaultLanguageIntegrityTest extends SharedCustomAnvilTest {

    private static final Message[] MESSAGES = collectMessages();

    private static Message[] collectMessages() {
        // just load them
        @SuppressWarnings("unused")
        Object[] providers = {
                MsgCommand.INSTANCE,
                MsgUI.INSTANCE,
                MsgError.INSTANCE,
                MsgWarning.INSTANCE
        };

        return Message.Companion.getValues().toArray(new Message[0]);
    }

    public static Message[] provideRegisteredKeys() {
        return MESSAGES;
    }

    @Test
    public void messagesAreRegistered() {
        Assertions.assertTrue(MESSAGES.length > 0, "No message was registered, provider objects failed to initialize");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideRegisteredKeys")
    public void registeredKeyExistsInDefaultLanguage(Message message) {
        Assertions.assertTrue(Lang.INSTANCE.has(message.getKey()),
                "Registered message key is missing from the default language file (lang/en.yml)"
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideRegisteredKeys")
    public void registeredKeyPlaceholdersMatchParams(Message message) {
        String[] params = message.getParams();
        Set<String> textParams = extractTextParams(message);

        List<String> issues = new ArrayList<>();

        // Every %placeholder found in the text must map to a registered param
        Set<String> usedParams = new LinkedHashSet<>();
        for(String textParam : textParams) {
            boolean found = false;
            for(String param : params) {
                if(param == null) continue;
                if(textParam.startsWith(param)) {
                    found = true;
                    usedParams.add(param);
                    break;
                }
            }
            if(!found) issues.add("Placeholder %" + textParam + " does not match any registered param");
        }

        // Every registered param must be used in the text
        for(String param : params) {
            if(param == null) continue;
            boolean found = false;
            for(String used : usedParams) {
                if(used.startsWith(param)) {
                    found = true;
                    break;
                }
            }
            if(!found) issues.add("Registered param %" + param + " is never used in the text");
        }

        Assertions.assertTrue(issues.isEmpty(),
                "Placeholder/param mismatch:\n  " + String.join("\n  ", issues));
    }

    @Test
    public void defaultLanguageKeysAreAllRegistered() {
        var registered = Arrays.stream(MESSAGES).map(Message::getKey).collect(Collectors.toSet());

        List<String> unregistered = new ArrayList<>();
        for(String key : Lang.INSTANCE.getKeys()) {
            if(!registered.contains(key)) unregistered.add(key);
        }

        Assertions.assertTrue(unregistered.isEmpty(),
                "Key(s) in the default language file (lang/en.yml) are not registered in any Message provider:\n  "
                        + String.join("\n  ", unregistered));
    }

    @Test
    public void noDuplicateRegisteredKeys() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for(Message message : MESSAGES) {
            counts.merge(message.getKey(), 1, Integer::sum);
        }

        List<String> duplicates = new ArrayList<>();
        for(Map.Entry<String, Integer> entry : counts.entrySet()) {
            if(entry.getValue() > 1) duplicates.add(entry.getKey() + " (registered " + entry.getValue() + " times)");
        }

        Assertions.assertTrue(duplicates.isEmpty(),
                "Duplicate registered message key(s):\n  " + String.join("\n  ", duplicates));
    }

    private static List<String> getTexts(String key) {
        ConfigurationSection section = Lang.INSTANCE.getSection(key);
        if(section == null) return List.of(Lang.INSTANCE.getTranslated(key));

        List<String> texts = new ArrayList<>();
        section.getValues(false).forEach((entryKey, value) -> texts.add(String.valueOf(value)));
        return texts;
    }

    private static Set<String> extractTextParams(Message message) {
        List<String> texts = getTexts(message.getKey());

        Set<String> textParams = new LinkedHashSet<>();
        for(String text : texts) {
            int index = 0;
            while(true) {
                index = text.indexOf('%', index);
                if(index > 0 && text.charAt(index - 1) == '\\') { // escaped \%, skip
                    index++;
                    continue;
                }
                if(index++ < 0) break;

                int end = text.indexOf(' ', index);
                if(end == -1) end = text.length();
                textParams.add(text.substring(index, end));
            }
        }
        return textParams;
    }

}
