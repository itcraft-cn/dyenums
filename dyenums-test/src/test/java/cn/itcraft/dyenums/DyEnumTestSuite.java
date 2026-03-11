package cn.itcraft.dyenums;

import cn.itcraft.dyenums.core.BaseDyEnumTest;
import cn.itcraft.dyenums.core.EnumRegistryTest;
import cn.itcraft.dyenums.integration.EnumIntegrationTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Helly Guo
 * <p>
 * Created on 2026-03-11 14:10
 */
@Suite.SuiteClasses({
        BaseDyEnumTest.class,
        EnumRegistryTest.class,
        EnumIntegrationTest.class
})
@RunWith(Suite.class)
public class DyEnumTestSuite {
}
