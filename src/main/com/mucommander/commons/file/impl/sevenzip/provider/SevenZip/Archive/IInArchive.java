package com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Archive;

import java.io.IOException;

import com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.IInStream;

public interface IInArchive {
    int NExtract_NAskMode_kExtract = 0;
    int NExtract_NAskMode_kTest = 1;
    int NExtract_NAskMode_kSkip = 2;
    
    int NExtract_NOperationResult_kOK = 0;
    int NExtract_NOperationResult_kUnSupportedMethod = 1;
    int NExtract_NOperationResult_kDataError = 2;
    int NExtract_NOperationResult_kCRCError = 3;
    
    // Static-SFX (for Linux) can be big.
    long kMaxCheckStartPosition = 1 << 22;
    
    SevenZipEntry getEntry(int index);
    
    int size();
    
    int close() throws IOException ;
    
    int Extract(int [] indices, int numItems,
            int testModeSpec, IArchiveExtractCallback extractCallbackSpec) throws java.io.IOException;
    
    int Open(IInStream stream) throws IOException;
    
    int Open(
            IInStream stream, // InStream *stream
            long maxCheckStartPosition // const UInt64 *maxCheckStartPosition,
            // IArchiveOpenCallback *openArchiveCallback */
            ) throws java.io.IOException;

    
}

