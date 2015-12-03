package com.mucommander.commons.file.impl.sevenzip.provider.SevenZip;

public interface ICompressSetOutStream {
    int SetOutStream(java.io.OutputStream inStream);
    int ReleaseOutStream() throws java.io.IOException;
}

