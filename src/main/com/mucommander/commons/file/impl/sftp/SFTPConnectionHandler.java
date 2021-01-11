package com.mucommander.commons.file.impl.sftp;

import com.mucommander.commons.file.AuthException;
import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.connection.ConnectionHandler;
import com.sshtools.net.SocketTransport;
import com.sshtools.publickey.InvalidPassphraseException;
import com.sshtools.publickey.SshPrivateKeyFile;
import com.sshtools.publickey.SshPrivateKeyFileFactory;
import com.sshtools.sftp.SftpClient;
import com.sshtools.sftp.SftpStatusException;
import com.sshtools.sftp.SftpSubsystemChannel;
import com.sshtools.ssh.*;
import com.sshtools.ssh.components.SshKeyPair;
import com.sshtools.ssh2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handles connections to SFTP servers.
 *
 * @author Maxence Bernard, Vassil Dichev
 */
class SFTPConnectionHandler extends ConnectionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SFTPConnectionHandler.class);

    Ssh2Client sshClient;
    SftpClient sftpClient;
    SftpSubsystemChannel sftpSubsystem;

    /** 'Password' SSH authentication method */
    private final static String PASSWORD_AUTH_METHOD = "password";

    /** 'Keyboard interactive' SSH authentication method */
    private final static String KEYBOARD_INTERACTIVE_AUTH_METHOD = "keyboard-interactive";

    /** 'Public key' SSH authentication method, not supported at the moment */
    private final static String PUBLIC_KEY_AUTH_METHOD = "publickey";


    SFTPConnectionHandler(FileURL location) {
        super(location);
    }


    //////////////////////////////////////
    // ConnectionHandler implementation //
    //////////////////////////////////////

    @Override
    public void startConnection() throws IOException {
        LOGGER.info("starting connection to {}", realm);
        try {
            FileURL realm = getRealm();

            // Retrieve credentials to be used to authenticate
            final Credentials credentials = getCredentials();

            // Throw an AuthException if no auth information, required for SSH
            if (credentials == null) {
                throwAuthException("Login and password required");  // Todo: localize this entry
            }

            LOGGER.trace("creating SshClient");


            // Override default port (22) if a custom port was specified in the URL
            int port = realm.getPort();
            if (port == -1) {
                port = 22;
            }

            // Connect to server, no host key verification
            SshConnector con = SshConnector.createInstance();
            // Lets do some host key verification
            HostKeyVerification hkv = (hostname, key) -> {
                try {
                    System.out.println("The connected host's key ("+ key.getAlgorithm() + ") is");
                    System.out.println(key.getFingerprint());
                } catch (SshException ignore) {}
                return true;
            };

            con.getContext().setHostKeyVerification(hkv);
            con.getContext().setPreferredPublicKey(Ssh2Context.PUBLIC_KEY_SSHDSS);

            // Init SSH client
            sshClient = (Ssh2Client) con.connect(new SocketTransport(realm.getHost(), port), credentials.getLogin(), true);


//            sshClient.connect(realm.getHost(), port, new IgnoreHostKeyVerification());

            // Retrieve a list of available authentication methods on the server.
            // Some SSH servers support the 'password' auth method (e.g. OpenSSH on Debian unstable), some don't
            // and only support the 'keyboard-interactive' method.
            List<String> authMethods = new ArrayList<>(Arrays.asList(sshClient.getAuthenticationMethods(credentials.getLogin())));
            LOGGER.info("getAvailableAuthMethods()={}", sshClient.getAuthenticationMethods(credentials.getLogin()));

            SshAuthentication authClient = null;
            String privateKeyPath = realm.getProperty(SFTPFile.PRIVATE_KEY_PATH_PROPERTY_NAME);
            // Try public key first. Don't try other methods if there's a key file defined
            if (authMethods.contains(PUBLIC_KEY_AUTH_METHOD) && privateKeyPath != null) {
                LOGGER.info("Using {} authentication method", PUBLIC_KEY_AUTH_METHOD);

                Ssh2PublicKeyAuthentication pk = new Ssh2PublicKeyAuthentication();
                pk.setUsername(credentials.getLogin());

                // Throw an AuthException if problems with private key file
                try {
                    SshPrivateKeyFile pkfile = SshPrivateKeyFileFactory.parse(new FileInputStream(new File(privateKeyPath)));
                    SshKeyPair pair = pkfile.toKeyPair(pkfile.isPassphraseProtected() ? credentials.getPassword() : null);
                    pk.setPrivateKey(pair.getPrivateKey());
                    pk.setPublicKey(pair.getPublicKey());
                } catch (IOException | InvalidPassphraseException e) {
                    e.printStackTrace();
                    privateKeyPath = null;  // try to authorize via password on error
//                    throwAuthException("Invalid private key file or passphrase");  // Todo: localize this entry
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    throwAuthException("Error reading private key file");  // Todo: localize this entry
                }

                authClient = pk;
            }
            // Use 'keyboard-interactive' method only if 'password' auth method is not available and
            // 'keyboard-interactive' is supported by the server
            //else
            if (!authMethods.contains(PASSWORD_AUTH_METHOD) && authMethods.contains(KEYBOARD_INTERACTIVE_AUTH_METHOD) &&
                    privateKeyPath == null) {
                LOGGER.info("Using {} authentication method", KEYBOARD_INTERACTIVE_AUTH_METHOD);

                KBIAuthentication kbi = new KBIAuthentication();
                kbi.setUsername(credentials.getLogin());

                // Fake keyboard password input
                kbi.setKBIRequestHandler((name, instruction, prompts) -> {
                    // Workaround for what seems to be a bug in J2SSH: this method is called twice, first time
                    // with a valid KBIPrompt array, second time with null
                    if (prompts == null) {
                        LOGGER.trace("prompts is null!");
                        return false;
                    }

                    for (int i = 0; i < prompts.length; i++) {
                        LOGGER.trace("prompts[{}]={}", i, prompts[i].getPrompt());
                        prompts[i].setResponse(credentials.getPassword());
                    }
                    return true;
                });

                authClient = kbi;
            }
            // Default to 'password' method, even if server didn't report as being supported
            else if (privateKeyPath == null) {
                LOGGER.info("Using {} authentication method", PASSWORD_AUTH_METHOD);

                Ssh2PasswordAuthentication pwd = new Ssh2PasswordAuthentication();
                pwd.setUsername(credentials.getLogin());
                pwd.setPassword(credentials.getPassword());

                authClient = pwd;
            }

            authenticate(authClient);
            // Init SFTP connections
            sftpClient = new SftpClient(sshClient);
            SshSession session = sshClient.openSessionChannel();

            if (session instanceof Ssh2Session) {
                ((Ssh2Session) session).startSubsystem("sftp");
            }
            sftpSubsystem = new SftpSubsystemChannel(session);
            sftpSubsystem.initialize();
        } catch(IOException | SftpStatusException | SshException | ChannelOpenException e) {
            LOGGER.info("IOException thrown while starting connection", e);
            // Disconnect if something went wrong
            if (sshClient != null && sshClient.isConnected()) {
                sshClient.disconnect();
            }

            sshClient = null;
            sftpClient = null;
            sftpSubsystem = null;

            // Re-throw exception
            if (e instanceof IOException) {
                throw (IOException)e;
            } else {
                throw new IOException(e);
            }
        }
    }

    private void authenticate(SshAuthentication authClient) throws SshException, AuthException {
        try {
            int authResult = sshClient.authenticate(authClient);

            // Throw an AuthException if authentication failed
            if (authResult != SshAuthentication.COMPLETE) {
                throwAuthException("Login or password rejected");   // Todo: localize this entry
            }

            LOGGER.info("authentication complete, authResult={}", authResult);
        } catch(AuthException e) {
            LOGGER.info("Caught exception while authenticating", e);
            e.printStackTrace();
            throw  e;//throwAuthException(e.getMessage());
        }
    }


    @Override
    public synchronized boolean isConnected() {
        return sshClient != null && sshClient.isConnected()
            && sftpClient != null && !sftpClient.isClosed()
            && sftpSubsystem !=null && !sftpSubsystem.isClosed();
    }


    @Override
    public synchronized void closeConnection() {
        if (sftpClient != null) {
            try {
                sftpClient.quit();
            } catch(SshException e) {
                LOGGER.info("IOException caught while calling sftpClient.quit()", e);
            }
        }

        if (sftpSubsystem != null) {
            try {
                sftpSubsystem.close();
            } catch(IOException e) {
                LOGGER.info("IOException caught while calling sftpChannel.close ()");
            }
        }

        if (sshClient != null) {
            sshClient.disconnect();
        }
    }


    @Override
    public void keepAlive() {
        // No-op, keep alive is not available and shouldn't really be necessary, SSH servers such as OpenSSH usually
        // maintain connections open without limit.
    }

}
