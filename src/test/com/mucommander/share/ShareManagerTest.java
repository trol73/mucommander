package com.mucommander.share;

import java.util.Set;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Note that the ShareManager really does not contain any logic so this test is maybe a little obsolete.
 *
 * @author Mathias
 */
public class ShareManagerTest {

    private ShareProvider mockProvider;

    public ShareManagerTest() {
    }

    @BeforeClass
    public void setUpClass() throws Exception {
        mockProvider = mock(ShareProvider.class);
        when(mockProvider.getDisplayName()).thenReturn("testProvider");
    }

    @Test
    public void setAndGetProvider() {
        assertTrue(ShareManager.getProviders().isEmpty());

        ShareManager.registerProvider(mockProvider);

        Set<ShareProvider> providers = ShareManager.getProviders();

        assertTrue(providers.size() == 1);
        assertEquals(providers.iterator().next().getDisplayName(), "testProvider");

    }

    @Test
    public void unregisterProvider() {
        ShareManager.registerProvider(mockProvider);

        Set<ShareProvider> providers = ShareManager.getProviders();
        assertTrue(providers.size() == 1);
        assertEquals(providers.iterator().next(), mockProvider);

        ShareManager.unregisterProvider(mockProvider);
        assertTrue(providers.isEmpty());

    }
}
