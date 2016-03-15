package io.dropwizard.revolver;

import io.dropwizard.revolver.persistence.InMemoryPersistenceProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author phaneesh
 */
@Slf4j
public class RevolverBundleTest extends BaseRevolverTest {


    @Test
    public void testBootstrap() {
        assertTrue(bundle.getPersistenceProvider() instanceof InMemoryPersistenceProvider);
        assertNotNull(bundle.getRevolverConfig(configuration));
        assertNotNull(RevolverBundle.matchPath("test", "v1/test"));
        assertNotNull(RevolverBundle.getHttpCommand("test"));
    }


}
