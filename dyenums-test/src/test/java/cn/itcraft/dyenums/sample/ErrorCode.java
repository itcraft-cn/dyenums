package cn.itcraft.dyenums.sample;

import cn.itcraft.dyenums.annotation.EnumDefinition;
import cn.itcraft.dyenums.core.EnumRegistry;
import cn.itcraft.dyenums.core.MultiLangDyEnum;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Multi-language error code enum.
 * Supports Chinese, English, Portuguese, Russian and any other language.
 * <p>
 * Predefined error codes: err.00001 ~ err.00010
 * <p>
 * New error codes can be added dynamically via configuration.
 *
 * @author Helly
 * @since 1.0.0
 */
@EnumDefinition(
        category = "error",
        dynamic = true,
        configSource = "file",
        description = "Multi-language error code enum"
)
public class ErrorCode extends MultiLangDyEnum {

    public static final ErrorCode ERR_00001 = new ErrorCode(
            "err_00001", "系统错误", 1,
            createMessages("系统内部错误", "System error", "Erro do sistema", "Системная ошибка")
    );
    public static final ErrorCode ERR_00002 = new ErrorCode(
            "err_00002", "参数错误", 2,
            createMessages("参数验证失败", "Invalid parameter", "Parâmetro inválido", "Неверный параметр")
    );
    public static final ErrorCode ERR_00003 = new ErrorCode(
            "err_00003", "权限错误", 3,
            createMessages("无访问权限", "Access denied", "Acesso negado", "Доступ запрещен")
    );
    public static final ErrorCode ERR_00004 = new ErrorCode(
            "err_00004", "认证错误", 4,
            createMessages("认证失败", "Authentication failed", "Falha na autenticação", "Ошибка аутентификации")
    );
    public static final ErrorCode ERR_00005 = new ErrorCode(
            "err_00005", "数据错误", 5,
            createMessages("数据不存在", "Data not found", "Dados não encontrados", "Данные не найдены")
    );
    public static final ErrorCode ERR_00006 = new ErrorCode(
            "err_00006", "网络错误", 6,
            createMessages("网络连接失败", "Network error", "Erro de rede", "Ошибка сети")
    );
    public static final ErrorCode ERR_00007 = new ErrorCode(
            "err_00007", "超时错误", 7,
            createMessages("请求超时", "Request timeout", "Tempo limite esgotado", "Тайм-аут запроса")
    );
    public static final ErrorCode ERR_00008 = new ErrorCode(
            "err_00008", "限流错误", 8,
            createMessages("请求过于频繁", "Rate limit exceeded", "Limite de taxa excedido", "Превышен лимит запросов")
    );
    public static final ErrorCode ERR_00009 = new ErrorCode(
            "err_00009", "服务错误", 9,
            createMessages("服务不可用", "Service unavailable", "Serviço indisponível", "Сервис недоступен")
    );
    public static final ErrorCode ERR_00010 = new ErrorCode(
            "err_00010", "未知错误", 10,
            createMessages("未知错误", "Unknown error", "Erro desconhecido", "Неизвестная ошибка")
    );
    private static final long serialVersionUID = 173298272982320928L;

    private ErrorCode(String code, String name, int order, Map<String, String> messages) {
        super(code, name, order, messages);
    }

    /**
     * Creates a message map from zh, en, pt, ru messages.
     *
     * @param zh Chinese message
     * @param en English message
     * @param pt Portuguese message
     * @param ru Russian message
     * @return message map
     */
    private static Map<String, String> createMessages(String zh, String en, String pt, String ru) {
        Map<String, String> messages = new HashMap<>();
        messages.put("zh", zh);
        messages.put("en", en);
        messages.put("pt", pt);
        messages.put("ru", ru);
        return messages;
    }

    /**
     * Factory method for creating ErrorCode instances from configuration.
     * Format: name|zh|en|pt|ru|order
     *
     * @param code        the error code
     * @param valueString value in format: name|zh|en|pt|ru|order
     * @return new ErrorCode instance
     */
    public static ErrorCode fromValueString(String code, String valueString) {
        Objects.requireNonNull(code, "Code cannot be null");
        Objects.requireNonNull(valueString, "Value string cannot be null");

        String[] parts = valueString.split("\\|", -1);
        if (parts.length < 6) {
            throw new IllegalArgumentException(
                    "Invalid value format. Expected: name|zh|en|pt|ru|order, got: " + valueString);
        }

        String name = parts[0].trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }

        int order;
        try {
            order = Integer.parseInt(parts[5].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid order value: " + parts[5], e);
        }

        Map<String, String> messages = new HashMap<>();
        messages.put("zh", parts[1].trim());
        messages.put("en", parts[2].trim());
        messages.put("pt", parts[3].trim());
        messages.put("ru", parts[4].trim());

        return new ErrorCode(code.trim(), name, order, messages);
    }

    /**
     * Factory method for creating ErrorCode with custom messages map.
     *
     * @param code     the error code
     * @param name     the error name
     * @param order    the order
     * @param messages the messages map
     * @return new ErrorCode instance
     */
    public static ErrorCode create(String code, String name, int order, Map<String, String> messages) {
        return new ErrorCode(code, name, order, messages);
    }

    /**
     * Dynamically adds a new error code.
     *
     * @param code     the error code
     * @param name     the error name
     * @param order    the order
     * @param messages the messages map
     * @return the created ErrorCode instance
     */
    public static ErrorCode addErrorCode(String code, String name, int order, Map<String, String> messages) {
        return EnumRegistry.addEnum(ErrorCode.class, code, name, null, order);
    }
}
