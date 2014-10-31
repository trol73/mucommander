package com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Archive.SevenZip;

import com.mucommander.commons.file.impl.sevenzip.provider.Common.IntVector;
import com.mucommander.commons.file.impl.sevenzip.provider.Common.LongVector;

public class ArchiveDatabaseEx extends ArchiveDatabase {
    InArchiveInfo ArchiveInfo = new InArchiveInfo();
    LongVector packStreamStartPositions = new LongVector();
    IntVector folderStartPackStreamIndex = new IntVector();
   
    IntVector folderStartFileIndex = new IntVector();
    IntVector fileIndexToFolderIndexMap = new IntVector();
    
    void Clear() {
        super.Clear();
        ArchiveInfo.Clear();
        packStreamStartPositions.clear();
        folderStartPackStreamIndex.clear();
        folderStartFileIndex.clear();
        fileIndexToFolderIndexMap.clear();
    }
    
    void FillFolderStartPackStream() {
        folderStartPackStreamIndex.clear();
        folderStartPackStreamIndex.Reserve(Folders.size());
        int startPos = 0;
        for(int i = 0; i < Folders.size(); i++) {
            folderStartPackStreamIndex.add(startPos);
            startPos += Folders.get(i).PackStreams.size();
        }
    }
    
    void FillStartPos() {
        packStreamStartPositions.clear();
        packStreamStartPositions.Reserve(PackSizes.size());
        long startPos = 0;
        for(int i = 0; i < PackSizes.size(); i++) {
            packStreamStartPositions.add(startPos);
            startPos += PackSizes.get(i);
        }
    }
    
    public void Fill()  throws java.io.IOException {
        FillFolderStartPackStream();
        FillStartPos();
        FillFolderStartFileIndex();
    }
    
    public long GetFolderFullPackSize(int folderIndex) {
        int packStreamIndex = folderStartPackStreamIndex.get(folderIndex);
        Folder folder = Folders.get(folderIndex);
        long size = 0;
        for (int i = 0; i < folder.PackStreams.size(); i++)
            size += PackSizes.get(packStreamIndex + i);
        return size;
    }
    
    
    void FillFolderStartFileIndex() throws java.io.IOException {
        folderStartFileIndex.clear();
        folderStartFileIndex.Reserve(Folders.size());
        fileIndexToFolderIndexMap.clear();
        fileIndexToFolderIndexMap.Reserve(Files.size());
        
        int folderIndex = 0;
        int indexInFolder = 0;
        for (int i = 0; i < Files.size(); i++) {
            FileItem file = Files.get(i);
            boolean emptyStream = !file.HasStream;
            if (emptyStream && indexInFolder == 0) {
                fileIndexToFolderIndexMap.add(InArchive.kNumNoIndex);
                continue;
            }
            if (indexInFolder == 0) {
                // v3.13 incorrectly worked with empty folders
                // v4.07: Loop for skipping empty folders
                for (;;) {
                    if (folderIndex >= Folders.size())
                        throw new java.io.IOException("Incorrect Header"); // CInArchiveException(CInArchiveException::kIncorrectHeader);
                    folderStartFileIndex.add(i); // check it
                    if (NumUnPackStreamsVector.get(folderIndex) != 0)
                        break;
                    folderIndex++;
                }
            }
            fileIndexToFolderIndexMap.add(folderIndex);
            if (emptyStream)
                continue;
            indexInFolder++;
            if (indexInFolder >= NumUnPackStreamsVector.get(folderIndex)) {
                folderIndex++;
                indexInFolder = 0;
            }
        }
    }
    
    public long GetFolderStreamPos(int folderIndex, int indexInFolder) {
        return ArchiveInfo.DataStartPosition +
                packStreamStartPositions.get(folderStartPackStreamIndex.get(folderIndex) +
                indexInFolder);
    }
}