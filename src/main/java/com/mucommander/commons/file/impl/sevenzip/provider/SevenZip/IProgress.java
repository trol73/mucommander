package com.mucommander.commons.file.impl.sevenzip.provider.SevenZip;

public interface IProgress {
    int SetTotal(long total);
    int SetCompleted(long completeValue);
}

