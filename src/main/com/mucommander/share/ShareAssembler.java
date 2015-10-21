package com.mucommander.share;

import com.mucommander.share.impl.imgur.ImgurProvider;

/**
 *
 * @author Mathias
 */
public class ShareAssembler {

    public ShareAssembler() {
        ShareManager.registerProvider(new ImgurProvider());
    }

}
