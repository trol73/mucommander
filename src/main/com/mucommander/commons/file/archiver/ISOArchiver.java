package com.mucommander.commons.file.archiver;

import com.github.stephenc.javaisotools.eltorito.impl.ElToritoConfig;
import com.github.stephenc.javaisotools.iso9660.ConfigException;
import com.github.stephenc.javaisotools.iso9660.ISO9660Directory;
import com.github.stephenc.javaisotools.iso9660.ISO9660File;
import com.github.stephenc.javaisotools.iso9660.ISO9660RootDirectory;
import com.github.stephenc.javaisotools.iso9660.impl.ISO9660Config;
import com.github.stephenc.javaisotools.iso9660.impl.ISOImageFileHandler;
import com.github.stephenc.javaisotools.joliet.impl.JolietConfig;
import com.github.stephenc.javaisotools.rockridge.impl.RockRidgeConfig;
import com.github.stephenc.javaisotools.sabre.HandlerException;
import com.github.stephenc.javaisotools.sabre.StreamHandler;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileAttributes;
import com.mucommander.commons.file.impl.iso.MuCreateISO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Archiver implementation using the ISO9660 archive format.
 *
 * @author Jeppe Vennekilde
 */
public class ISOArchiver extends Archiver {
    private StreamHandler streamHandler;
    private final ISO9660Config config;
    private final ISO9660RootDirectory root;
    //Adds support for longer file names & wider range of characters
    private final boolean enableJoliet = true;
    //Adds support for deeper directory hierarchies and even bigger file names (up to 255 bytes)
    private final boolean enableRockRidge = true;
    //Adds support for creation of bootable iso files (not implemented)
    private final boolean enableElTorito = false;
    private MuCreateISO createISOProcess = null;

    ISOArchiver(AbstractFile file) {
        super(null);
        supportStream = false;
        
        config = new ISO9660Config();
        try {
            config.allowASCII(false);
            config.setInterchangeLevel(1);
            //The rock ridge extension of ISO9660 allow directory depth to exceed 8
            config.restrictDirDepthTo8(!enableRockRidge);
            config.setPublisher(System.getProperty("user.name"));
            //Max length of volume is 32 chars
            config.setVolumeID(file.getName().substring(0, Math.min(file.getName().length(), 31)));
            config.setDataPreparer(System.getProperty("user.name"));
            config.forceDotDelimiter(true);
        } catch (ConfigException ex) {
            Logger.getLogger(ISOArchiver.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        root = new ISO9660RootDirectory();
        
        try {
            streamHandler = new ISOImageFileHandler(new File(file.getPath()));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ISOArchiver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    @Override
    public OutputStream createEntry(String entryPath, FileAttributes attributes) {
        try {
            if (attributes.isDirectory()) {
                String[] split = entryPath.split("\\\\");
                ISO9660Directory dir = new ISO9660Directory(split[split.length-1]);
                ISO9660Directory parent = getParentDirectory(entryPath);
                if (parent != null) {
                    parent.addDirectory(dir);
                }
            } else {
                try {
                    ISO9660File file = new ISO9660File(new File(attributes.getPath()));
                    ISO9660Directory parent = getParentDirectory(entryPath);
                    if (parent != null) {
                        parent.addFile(file);
                    }
                } catch (HandlerException ex) {
                    Logger.getLogger(ISOArchiver.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
        return null;    // TODO !!!! NPE here !!!
    }
    
    /**
     * Get the ISO9660Directory parent object the path belongs to
     *
     * @param isoPath the sub directory/file of the parent directory it will return
     * @return an ISO9660Directory that is the parent of the provided path
     */
    private ISO9660Directory getParentDirectory(String isoPath){
        String[] directories = isoPath.split("\\\\");
        //Initial directory (root)
        ISO9660Directory parent = root;
        for (int i = 0; i < directories.length - 1; i++){
            ISO9660Directory dir = containsDirectory(parent,directories[i]);
            if (dir == null) {
                return null;
            }
            parent = dir;
        }
        return parent;
    }
    
    /**
     * Check if an ISO9660Directory contain a provided sub directory
     *
     * @param parentDirectory the directory that will be searched
     * @param isoSubDirPath the ISO path that will be used for reference to see 
     * if the parent directory contains the sub directory
     * @return an ISO9660Directory that is sub directory of the parent directory
     * null if it does not contain the sub directory
     */
    private ISO9660Directory containsDirectory(ISO9660Directory parentDirectory, String isoSubDirPath){
        for (ISO9660Directory directory : parentDirectory.getDirectories()) {
            if (directory.getName().equals(isoSubDirPath)){
                return directory;
            }
        }
        return null;
    }

    @Override
    public String getProcessingFile() {
        return createISOProcess != null ? createISOProcess.getProcessingFile() : null;
    }
    
    @Override
    public long totalWrittenBytes(){
        return createISOProcess != null ? createISOProcess.totalWrittenBytes(): 0;
    }
    
    @Override
    public long writtenBytesCurrentFile(){
        return createISOProcess != null ? createISOProcess.writtenBytesCurrentFile(): 0;
    }
    
    @Override
    public long currentFileLength(){
        return createISOProcess != null ? createISOProcess.currentFileLength(): 0;
    }
    
    @Override
    public void postProcess() throws IOException {
        if (root.hasSubDirs() || !root.getFiles().isEmpty()) {
            createISOProcess = new MuCreateISO(streamHandler, root);

            RockRidgeConfig rrConfig = null;
            if (enableRockRidge) {
                // Rock Ridge support
                rrConfig = new RockRidgeConfig();
                rrConfig.setMkisofsCompatibility(false);
                rrConfig.hideMovedDirectoriesStore(true);
                rrConfig.forcePortableFilenameCharacterSet(true);
            }

            JolietConfig jolietConfig = null;
            if (enableJoliet) {
                // Joliet support
                jolietConfig = new JolietConfig();
                try {
                    if (config.getPublisher() instanceof String){
                        jolietConfig.setPublisher((String) config.getPublisher());
                    } else {
                        try {
                            jolietConfig.setPublisher((File) config.getPublisher());
                        } catch (HandlerException ex) {
                            Logger.getLogger(ISOArchiver.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } 
                    //Max volume id is 16 in the joliet config
                    jolietConfig.setVolumeID(config.getVolumeID().substring(0,Math.min(config.getVolumeID().length(), 15)));
                    if (config.getDataPreparer() != null){
                        if(config.getDataPreparer() instanceof String){
                            jolietConfig.setDataPreparer((String) config.getDataPreparer());
                        } else {
                            try {
                                jolietConfig.setDataPreparer((File) config.getDataPreparer());
                            } catch (Exception ex) {
                                Logger.getLogger(ISOArchiver.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } 
                    }
                    jolietConfig.forceDotDelimiter(true);
                } catch (ConfigException ex) {
                    Logger.getLogger(ISOArchiver.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            //ELTorito adds support for bootable ISO files, which is not supported at this time
            //As this is for archiving, not creation of bootable ISO files (yet)
            ElToritoConfig elToritoConfig = null;

            try {
                createISOProcess.process(config, rrConfig, jolietConfig, elToritoConfig);
            } catch (HandlerException ex) {
                Logger.getLogger(ISOArchiver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void close() throws IOException {
    }
    
}
