package cn.itcraft.dyenums;

import cn.itcraft.dyenums.core.BaseDyEnumTest;
import cn.itcraft.dyenums.core.EnumRegistryTest;
import cn.itcraft.dyenums.integration.EnumIntegrationTest;
import cn.itcraft.dyenums.loader.db.DatabaseDyEnumsLoaderTest;
import cn.itcraft.dyenums.loader.file.FileBasedDyEnumsLoaderTest;
import cn.itcraft.dyenums.loader.file.PropDyEnumsLoaderTest;
import cn.itcraft.dyenums.spring.SpringIntegrationTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for all dyenums tests.
 *
 * @author Helly
 * @since 1.0.0
 */
@Suite.SuiteClasses({
        BaseDyEnumTest.class,
        EnumRegistryTest.class,
        FileBasedDyEnumsLoaderTest.class,
        PropDyEnumsLoaderTest.class,
        DatabaseDyEnumsLoaderTest.class,
        SpringIntegrationTest.class,
        EnumIntegrationTest.class
})
@RunWith(Suite.class)
public class DyEnumTestSuite {
}
