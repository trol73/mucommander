package com.mucommander.commons.file.impl.sevenzip.provider.SevenZip;

public interface ICompressSetOutStreamSize {
    int INVALID_OUTSIZE = -1;

    int SetOutStreamSize(long outSize);
}

