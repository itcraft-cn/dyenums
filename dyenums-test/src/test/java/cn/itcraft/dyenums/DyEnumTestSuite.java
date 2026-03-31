package cn.itcraft.dyenums;

import cn.itcraft.dyenums.core.BaseDyEnumTest;
import cn.itcraft.dyenums.core.EnumRegistryTest;
import cn.itcraft.dyenums.integration.EnumIntegrationTest;
import cn.itcraft.dyenums.loader.db.DatabaseDyEnumsLoaderTest;
import cn.itcraft.dyenums.loader.file.FileBasedDyEnumsLoaderTest;
import cn.itcraft.dyenums.loader.file.PropDyEnumsLoaderTest;
import cn.itcraft.dyenums.sample.ErrorCodeTest;
import cn.itcraft.dyenums.spring.SpringIntegrationTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        BaseDyEnumTest.class,
        EnumRegistryTest.class,
        FileBasedDyEnumsLoaderTest.class,
        PropDyEnumsLoaderTest.class,
        DatabaseDyEnumsLoaderTest.class,
        SpringIntegrationTest.class,
        ErrorCodeTest.class,
        EnumIntegrationTest.class
})
public class DyEnumTestSuite {
}