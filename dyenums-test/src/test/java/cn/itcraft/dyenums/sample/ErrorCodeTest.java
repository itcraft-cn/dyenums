package cn.itcraft.dyenums.sample;

import cn.itcraft.dyenums.core.EnumRegistry;
import cn.itcraft.dyenums.loader.file.PropDyEnumsLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ErrorCodeTest {

    @BeforeEach
    public void setUp() {
        EnumRegistry.clear();
        registerPredefinedErrorCodes();
    }

    @AfterEach
    public void tearDown() {
        EnumRegistry.clear();
    }

    private void registerPredefinedErrorCodes() {
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

    @Test
    public void testPredefinedErrorCodes() {
        assertEquals(10, EnumRegistry.getCount(ErrorCode.class));

        assertTrue(EnumRegistry.contains(ErrorCode.class, "err_00001"));
        assertTrue(EnumRegistry.contains(ErrorCode.class, "err_00010"));
    }

    @Test
    public void testGetMessageByLocale() {
        ErrorCode err = ErrorCode.ERR_00001;

        assertEquals("系统内部错误", err.getMessage("zh"));
        assertEquals("System error", err.getMessage("en"));
        assertEquals("Erro do sistema", err.getMessage("pt"));
        assertEquals("Системная ошибка", err.getMessage("ru"));
    }

    @Test
    public void testGetMessageByLocaleObject() {
        ErrorCode err = ErrorCode.ERR_00002;

        assertEquals("参数验证失败", err.getMessage(java.util.Locale.CHINA));
        assertEquals("Invalid parameter", err.getMessage(java.util.Locale.US));
    }

    @Test
    public void testGetMessageUnsupportedLocale() {
        ErrorCode err = ErrorCode.ERR_00001;

        assertEquals("err_00001", err.getMessage("ja"));
        assertEquals("err_00001", err.getMessage(""));
        assertEquals("err_00001", err.getMessage((String) null));
    }

    @Test
    public void testConvenienceMethods() {
        ErrorCode err = ErrorCode.ERR_00003;

        assertEquals("无访问权限", err.getMessageZh());
        assertEquals("Access denied", err.getMessageEn());
        assertEquals("Acesso negado", err.getMessagePt());
        assertEquals("Доступ запрещен", err.getMessageRu());
    }

    @Test
    public void testSupportedLocales() {
        ErrorCode err = ErrorCode.ERR_00001;
        Set<String> locales = err.getSupportedLocales();

        assertEquals(4, locales.size());
        assertTrue(locales.contains("zh"));
        assertTrue(locales.contains("en"));
        assertTrue(locales.contains("pt"));
        assertTrue(locales.contains("ru"));
    }

    @Test
    public void testSupportsLocale() {
        ErrorCode err = ErrorCode.ERR_00001;

        assertTrue(err.supportsLocale("zh"));
        assertTrue(err.supportsLocale("en"));
        assertFalse(err.supportsLocale("ja"));
        assertFalse(err.supportsLocale(null));
    }

    @Test
    public void testGetAllMessages() {
        ErrorCode err = ErrorCode.ERR_00001;
        Map<String, String> messages = err.getAllMessages();

        assertEquals(4, messages.size());
        assertEquals("系统内部错误", messages.get("zh"));
        assertEquals("System error", messages.get("en"));
    }

    @Test
    public void testDynamicAddErrorCode() {
        int beforeCount = EnumRegistry.getCount(ErrorCode.class);

        Map<String, String> messages = new HashMap<>();
        messages.put("zh", "测试错误");
        messages.put("en", "Test error");

        ErrorCode customError = ErrorCode.create(
                "err.test.001",
                "测试错误",
                999,
                messages
        );
        EnumRegistry.register(ErrorCode.class, customError);

        int afterCount = EnumRegistry.getCount(ErrorCode.class);
        assertEquals(beforeCount + 1, afterCount);

        Optional<ErrorCode> loaded = EnumRegistry.valueOf(ErrorCode.class, "err.test.001");
        assertTrue(loaded.isPresent());
        assertEquals("测试错误", loaded.get().getMessageZh());
        assertEquals("Test error", loaded.get().getMessageEn());
    }

    @Test
    public void testLoadFromConfig() throws IOException {
        Properties props = new Properties();
        props.setProperty("ErrorCode.err_cfg_001", "配置错误|配置加载失败|Config load failed|Falha ao carregar|Ошибка загрузки|901");
        props.setProperty("ErrorCode.err_cfg_002", "参数错误|参数缺失|Missing param|Parâmetro ausente|Отсутствует параметр|902");

        int beforeCount = EnumRegistry.getCount(ErrorCode.class);
        
        PropDyEnumsLoader<ErrorCode> loader = new PropDyEnumsLoader<>(props);
        int loaded = loader.load(ErrorCode.class, ErrorCode::fromValueString);

        assertEquals(2, loaded);

        ErrorCode err1 = EnumRegistry.valueOf(ErrorCode.class, "err_cfg_001").orElse(null);
        assertNotNull(err1);
        assertEquals("配置错误", err1.getName());
        assertEquals("配置加载失败", err1.getMessageZh());
        assertEquals("Config load failed", err1.getMessageEn());
    }

    @Test
    public void testFromValueString() {
        ErrorCode err = ErrorCode.fromValueString(
                "err.test.fromconfig",
                "测试错误|测试消息|Test message|Mensagem de teste|Тестовое сообщение|500"
        );

        assertEquals("err.test.fromconfig", err.getCode());
        assertEquals("测试错误", err.getName());
        assertEquals(500, err.getOrder());
        assertEquals("测试消息", err.getMessageZh());
        assertEquals("Test message", err.getMessageEn());
        assertEquals("Mensagem de teste", err.getMessagePt());
        assertEquals("Тестовое сообщение", err.getMessageRu());
    }

    @Test
    public void testFromValueString_InvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> {
            ErrorCode.fromValueString("err.invalid", "invalid format");
        });
    }

    @Test
    public void testFromValueString_NullCode() {
        assertThrows(NullPointerException.class, () -> {
            ErrorCode.fromValueString(null, "name|zh|en|pt|ru|1");
        });
    }

    @Test
    public void testFromValueString_NullValueString() {
        assertThrows(NullPointerException.class, () -> {
            ErrorCode.fromValueString("err.test", null);
        });
    }

    @Test
    public void testDuplicateRegistrationOverwrites() {
        ErrorCode original = EnumRegistry.valueOf(ErrorCode.class, "err_00001").orElse(null);
        assertNotNull(original);

        Map<String, String> messages = new HashMap<>();
        messages.put("zh", "新系统错误");
        messages.put("en", "New system error");
        ErrorCode modified = ErrorCode.create("err_00001", "新系统错误", 1, messages);
        EnumRegistry.register(ErrorCode.class, modified);

        assertEquals(10, EnumRegistry.getCount(ErrorCode.class));
        ErrorCode fetched = EnumRegistry.valueOf(ErrorCode.class, "err_00001").orElse(null);
        assertNotNull(fetched);
        assertEquals("新系统错误", fetched.getName());
    }

    @Test
    public void testErrorCodeOrdering() {
        List<ErrorCode> codes = EnumRegistry.values(ErrorCode.class);

        assertEquals("err_00001", codes.get(0).getCode());
        assertEquals("err_00002", codes.get(1).getCode());
        assertEquals("err_00010", codes.get(9).getCode());
    }

    @Test
    public void testExtensibleLanguageSupport() {
        Map<String, String> messages = new HashMap<>();
        messages.put("zh", "中文消息");
        messages.put("en", "English message");
        messages.put("ja", "日本語メッセージ");
        messages.put("ko", "한국어 메시지");
        messages.put("ar", "رسالة عربية");

        ErrorCode extensibleError = ErrorCode.create(
                "err.extensible",
                "可扩展语言错误",
                999,
                messages
        );

        assertEquals("中文消息", extensibleError.getMessage("zh"));
        assertEquals("English message", extensibleError.getMessage("en"));
        assertEquals("日本語メッセージ", extensibleError.getMessage("ja"));
        assertEquals("한국어 메시지", extensibleError.getMessage("ko"));
        assertEquals("رسالة عربية", extensibleError.getMessage("ar"));
    }
}