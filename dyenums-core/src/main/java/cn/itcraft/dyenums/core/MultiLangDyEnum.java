package cn.itcraft.dyenums.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Abstract base class for multi-language dynamic enums.
 * Stores messages in a Map keyed by locale code (zh, en, pt, ru, etc.)
 * Supports unlimited language extensions.
 *
 * @author Helly
 * @since 1.0.0
 */
public abstract class MultiLangDyEnum extends BaseDyEnum {

    private static final long serialVersionUID = 1L;

    protected final Map<String, String> messages;

    /**
     * Constructs a new MultiLangDyEnum instance.
     *
     * @param code     the unique code for this enum value
     * @param name     the display name for this enum value
     * @param order    the order/sort index for this enum value
     * @param messages map of locale code to message {@code (e.g., "zh" -> "系统错误")}
     */
    protected MultiLangDyEnum(String code, String name, int order, Map<String, String> messages) {
        super(code, name, null, order);
        this.messages = messages != null ? new HashMap<>(messages) : new HashMap<>();
    }

    /**
     * Gets the message for a specific locale.
     *
     * @param locale the locale code (e.g., "zh", "en", "pt", "ru")
     * @return the message for the locale, or code if not found
     */
    public String getMessage(String locale) {
        if (locale == null || locale.isEmpty()) {
            return getCode();
        }
        return messages.getOrDefault(locale.toLowerCase(), getCode());
    }

    /**
     * Gets the message for a specific Locale.
     *
     * @param locale the Locale object
     * @return the message for the locale, or code if not found
     */
    public String getMessage(Locale locale) {
        if (locale == null) {
            return getCode();
        }
        return getMessage(locale.getLanguage());
    }

    /**
     * Gets all supported locales for this enum.
     *
     * @return unmodifiable set of locale codes
     */
    public Set<String> getSupportedLocales() {
        return Collections.unmodifiableSet(messages.keySet());
    }

    /**
     * Checks if a specific locale is supported.
     *
     * @param locale the locale code to check
     * @return true if the locale is supported
     */
    public boolean supportsLocale(String locale) {
        return locale != null && messages.containsKey(locale.toLowerCase());
    }

    /**
     * Gets all messages as an unmodifiable map.
     *
     * @return unmodifiable map of locale to message
     */
    public Map<String, String> getAllMessages() {
        return Collections.unmodifiableMap(messages);
    }

    /**
     * Gets the Chinese message.
     *
     * @return Chinese message
     */
    public String getMessageZh() {
        return getMessage("zh");
    }

    /**
     * Gets the English message.
     *
     * @return English message
     */
    public String getMessageEn() {
        return getMessage("en");
    }

    /**
     * Gets the Portuguese message.
     *
     * @return Portuguese message
     */
    public String getMessagePt() {
        return getMessage("pt");
    }

    /**
     * Gets the Russian message.
     *
     * @return Russian message
     */
    public String getMessageRu() {
        return getMessage("ru");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "code='" + getCode() + '\'' +
                ", name='" + getName() + '\'' +
                ", locales=" + messages.keySet() +
                '}';
    }
}
