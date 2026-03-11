package cn.itcraft.dyenums.sample;

import cn.itcraft.dyenums.core.EnumRegistry;
import cn.itcraft.dyenums.loader.file.PropDyEnumsLoader;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Demo class demonstrating multi-language error code support.
 * Shows how to:
 * 1. Use predefined error codes
 * 2. Get messages in different languages
 * 3. Dynamically add new error codes
 * 4. Load error codes from configuration
 *
 * @author Helly
 * @since 1.0.0
 */
public class ErrorCodeDemo {

    public static void main(String[] args) throws IOException {
        EnumRegistry.clear();
        registerPredefinedErrorCodes();

        System.out.println("=== Error Code Multi-Language Demo ===\n");

        demoBasicUsage();
        demoMultiLanguageSupport();
        demoDynamicAddition();
        demoConfigLoading();

        EnumRegistry.clear();
    }

    private static void registerPredefinedErrorCodes() {
        EnumRegistry.register(ErrorCode.class, ErrorCode.ERR_00001);
        EnumRegistry.register(ErrorCode.class, ErrorCode.ERR_00002);
        EnumRegistry.register(ErrorCode.class, ErrorCode.ERR_00003);
        EnumRegistry.register(ErrorCode.class, ErrorCode.ERR_00004);
        EnumRegistry.register(ErrorCode.class, ErrorCode.ERR_00005);
        EnumRegistry.register(ErrorCode.class, ErrorCode.ERR_00006);
        EnumRegistry.register(ErrorCode.class, ErrorCode.ERR_00007);
        EnumRegistry.register(ErrorCode.class, ErrorCode.ERR_00008);
        EnumRegistry.register(ErrorCode.class, ErrorCode.ERR_00009);
        EnumRegistry.register(ErrorCode.class, ErrorCode.ERR_00010);
    }

    private static void demoBasicUsage() {
        System.out.println("1. Basic Usage - Get error code by code:");
        System.out.println("----------------------------------------");

        Optional<ErrorCode> error = EnumRegistry.valueOf(ErrorCode.class, "err_00001");
        error.ifPresent(err -> {
            System.out.println("Code: " + err.getCode());
            System.out.println("Name: " + err.getName());
            System.out.println("Order: " + err.getOrder());
            System.out.println("Supported Locales: " + err.getSupportedLocales());
        });

        System.out.println();
    }

    private static void demoMultiLanguageSupport() {
        System.out.println("2. Multi-Language Support:");
        System.out.println("--------------------------");

        ErrorCode err = ErrorCode.ERR_00001;

        System.out.println("Error: " + err.getCode() + " - " + err.getName());
        System.out.println("Chinese (zh): " + err.getMessageZh());
        System.out.println("English (en): " + err.getMessageEn());
        System.out.println("Portuguese (pt): " + err.getMessagePt());
        System.out.println("Russian (ru): " + err.getMessageRu());

        System.out.println("\nUsing Locale object:");
        System.out.println("Locale.CHINA: " + err.getMessage(Locale.CHINA));
        System.out.println("Locale.US: " + err.getMessage(Locale.US));

        System.out.println("\nUnsupported locale returns code:");
        System.out.println("Japanese (ja): " + err.getMessage("ja"));

        System.out.println();
    }

    private static void demoDynamicAddition() {
        System.out.println("3. Dynamic Addition - Add new error code at runtime:");
        System.out.println("-----------------------------------------------------");

        int beforeCount = EnumRegistry.getCount(ErrorCode.class);
        System.out.println("Error codes before: " + beforeCount);

        Map<String, String> messages = new HashMap<>();
        messages.put("zh", "自定义业务错误");
        messages.put("en", "Custom business error");
        messages.put("pt", "Erro de negócio personalizado");
        messages.put("ru", "Пользовательская бизнес-ошибка");
        messages.put("ja", "カスタムビジネスエラー");

        ErrorCode customError = ErrorCode.create(
                "err.custom.001",
                "自定义错误",
                999,
                messages
                                                );
        EnumRegistry.register(ErrorCode.class, customError);

        int afterCount = EnumRegistry.getCount(ErrorCode.class);
        System.out.println("Error codes after: " + afterCount);

        Optional<ErrorCode> loaded = EnumRegistry.valueOf(ErrorCode.class, "err_custom_001");
        loaded.ifPresent(err -> {
            System.out.println("\nDynamically added error:");
            System.out.println("Code: " + err.getCode());
            System.out.println("Name: " + err.getName());
            System.out.println("All messages: " + err.getAllMessages());
            System.out.println("Japanese: " + err.getMessage("ja"));
        });

        System.out.println();
    }

    private static void demoConfigLoading() throws IOException {
        System.out.println("4. Configuration Loading:");
        System.out.println("--------------------------");

        Properties props = new Properties();
        props.setProperty("ErrorCode.err.cfg.001",
                          "配置错误1|配置文件加载失败|Config load failed|Falha ao carregar configuração|Ошибка загрузки конфигурации|901");
        props.setProperty("ErrorCode.err.cfg.002",
                          "配置错误2|配置项缺失|Missing config|Configuração ausente|Отсутствует конфигурация|902");

        System.out.println("Loading from Properties object...");
        PropDyEnumsLoader<ErrorCode> loader = new PropDyEnumsLoader<>(props);
        int loaded = loader.load(ErrorCode.class, ErrorCode::fromValueString);
        System.out.println("Loaded " + loaded + " error codes from config");

        Optional<ErrorCode> cfgError = EnumRegistry.valueOf(ErrorCode.class, "err_cfg_001");
        cfgError.ifPresent(err -> {
            System.out.println("\nConfig-loaded error:");
            System.out.println("Code: " + err.getCode());
            System.out.println("Name: " + err.getName());
            System.out.println("Chinese: " + err.getMessageZh());
            System.out.println("English: " + err.getMessageEn());
        });

        System.out.println();

        demoListAllErrorCodes();
    }

    private static void demoListAllErrorCodes() {
        System.out.println("5. List All Error Codes:");
        System.out.println("-------------------------");

        List<ErrorCode> allCodes = EnumRegistry.values(ErrorCode.class);
        System.out.println("Total error codes: " + allCodes.size());
        System.out.println();

        System.out.printf("%-15s %-15s %-30s%n", "Code", "Name", "English Message");
        System.out.println("-------------------------");

        for (ErrorCode err : allCodes) {
            System.out.printf("%-15s %-15s %-30s%n",
                              err.getCode(),
                              err.getName(),
                              err.getMessageEn());
        }

        System.out.println();
    }
}
