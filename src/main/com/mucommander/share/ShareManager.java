package com.mucommander.share;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Mathias
 */
public class ShareManager {

    private final static Set<ShareProvider> providers = new HashSet();

    public static Set<ShareProvider> getProviders() {

        return providers;
    }

    public static void registerProvider(ShareProvider provider) {
        providers.add(provider);
    }
    
    public static void unregisterProvider(ShareProvider provider){
        providers.remove(provider);
    }
}
