package com.mucommander.commons.file;

/**
 * This interface contains a set of known protocol names, that can be found in {@link FileURL}. 
 *
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public interface FileProtocols {

    /** Protocol for local or locally mounted files. */
    String FILE      = "file";

    /** Protocol for files served by an FTP server. */
    String FTP       = "ftp";

    /** Protocol for files served by a web server using HTTP. */
    String HTTP      = "http";

    /** Protocol for files served by an HDFS (Hadoop distributed filesystem) cluster. */
    String HDFS      = "hdfs";

    /** Protocol for files served by a web server using HTTPS. */
    String HTTPS     = "https";

    /** Protocol for files served by an NFS server. */
    String NFS       = "nfs";

    /** Protocol for files served by an Amazon S3 (or protocol-compatible) server. */
    String S3        = "s3";

    /** Protocol for files served by an SFTP server (not to be confused with FTPS or SCP). */
    String SFTP      = "sftp";

    /** Protocol for files served by a SMB/CIFS server. */
    String SMB       = "smb";

    /** Protocol for files served by a web server using Webdav/HTTP. */
    String WEBDAV    = "webdav";

    /** Protocol for files served by a web server using Webdav/HTTPS. */
    String WEBDAVS   = "webdavs";
    
    /** Protocol for files served by a web server using vSphere. */
    String VSPHERE   = "vsphere";

    /** Protocol for files on android devices. */
    String ADB       = "adb";

    /** Protocol for avrdude programmer. */
    String AVR       = "avr";

}
