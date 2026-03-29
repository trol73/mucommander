package com.mucommander.commons.file.impl.sevenzip.provider.SevenZip;

public interface ICompressProgressInfo {
    long INVALID = -1;

    int SetRatioInfo(long inSize, long outSize);
}
