package com.mucommander.share.impl.imgur;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.impl.ProxyFile;
import com.mucommander.commons.file.impl.local.LocalFile;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.job.TransferFileJob;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.main.MainFrame;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Mathias
 */
public class ImgurJob extends TransferFileJob {
    
    private String currentFile;
    private ImgurAPI imgurAPI;
    
    public ImgurJob(ImgurAPI imgurAPI, ProgressDialog progressDialog, MainFrame mainFrame, FileSet files) {
        super(progressDialog, mainFrame, files);
        this.imgurAPI = imgurAPI;
    }
    
    @Override
    protected boolean hasFolderChanged(AbstractFile folder) {
        // This job does not modify anything
        return false;
    }
    
    @Override
    protected boolean processFile(AbstractFile af, Object recurseParams) {
        Logger.getLogger(ImgurJob.class.getName()).log(Level.INFO, af.getBaseName());
        currentFile = af.getBaseName();
        
        File file = null;

        //Local file - no need to buffer
        if (af instanceof LocalFile || af instanceof ProxyFile) {
            file = new File(af.getAbsolutePath());
        } else {
            //Remote file - buffer first
            Logger.getLogger(ImgurJob.class.getName()).log(Level.INFO, "Buffering remote file");
            try {
                File tmpFile = File.createTempFile(af.getBaseName(), "." + af.getExtension());
                FileUtils.copyInputStreamToFile(af.getInputStream(), tmpFile);
                file = tmpFile;
            } catch (IOException ex) {
                Logger.getLogger(ImgurJob.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if (file != null) {
            Future future = imgurAPI.uploadAsync(file, new ImgurAPI.Callback() {
                
                @Override
                public void completed(String url) {
                    openUrlInDefaultBrowser(url);
                }
            });
            
            
            while (future.isCancelled() || !future.isDone()) {
                Thread.yield();
            }
            jobCompleted();
            return true;
            
        }
        return false;
    }
    
    @Override
    public String getStatusString() {
        
        String status;
        if (currentFile == null) {
            status = Translator.get("share_dialog.connecting");
        } else {
            status = Translator.get("share_dialog.uploading", currentFile);
        }
        
        return status;
    }
    
    private void openUrlInDefaultBrowser(String url) {
        Logger.getLogger(ImgurJob.class.getName()).log(Level.INFO, "Opening url {0}", url);
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI(url));
            } catch (IOException ex) {
                Logger.getLogger(ImgurJob.class.getName()).log(Level.SEVERE, null, ex);
            } catch (URISyntaxException ex) {
                Logger.getLogger(ImgurJob.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec("xdg-open " + url);
            } catch (IOException ex) {
                Logger.getLogger(ImgurJob.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
