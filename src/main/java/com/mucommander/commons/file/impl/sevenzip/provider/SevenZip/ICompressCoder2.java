package com.mucommander.commons.file.impl.sevenzip.provider.SevenZip;

import com.mucommander.commons.file.impl.sevenzip.provider.Common.RecordVector;

public interface ICompressCoder2 {
    int Code(
            RecordVector<java.io.InputStream>  inStreams,
            Object useless1, // const UInt64 ** /* inSizes */,
            int numInStreams,
            RecordVector<java.io.OutputStream> outStreams,
            Object useless2, // const UInt64 ** /* outSizes */,
            int numOutStreams,
            ICompressProgressInfo progress) throws java.io.IOException;
    
    void close() throws java.io.IOException ; // destructor
}
