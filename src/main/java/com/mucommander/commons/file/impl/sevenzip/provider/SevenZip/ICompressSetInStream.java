package com.mucommander.commons.file.impl.sevenzip.provider.SevenZip;

public interface ICompressSetInStream {
    int SetInStream(java.io.InputStream inStream);
    int ReleaseInStream() throws java.io.IOException ;
}

