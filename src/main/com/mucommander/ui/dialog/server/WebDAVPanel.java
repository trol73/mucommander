package com.mucommander.ui.dialog.server;

import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.FileProtocols;
import com.mucommander.commons.file.FileURL;
import com.mucommander.utils.text.Translator;
import com.mucommander.ui.main.MainFrame;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 *
 * @author Mathias
 */
public class WebDAVPanel extends ServerPanel {

    private JTextField serverField;
    private JTextField usernameField;
    private JPasswordField passwordField;

    private static String lastServer = "";
    private static String lastUsername = System.getProperty("user.name");
    private static String lastPassword = "";

    WebDAVPanel(ServerConnectDialog dialog, final MainFrame mainFrame) {
        super(dialog, mainFrame);

        // Server field, initialized to last server entered
        serverField = new JTextField(lastServer);
        serverField.selectAll();
        addTextFieldListeners(serverField, true);
        addRow(Translator.get("server_connect_dialog.server"), serverField, 15);

        // Username field, initialized to last username
        usernameField = new JTextField(lastUsername);
        usernameField.selectAll();
        addTextFieldListeners(usernameField, false);
        addRow(Translator.get("server_connect_dialog.username"), usernameField, 15);

//        // Password field, initialized to ""
        passwordField = new JPasswordField("");
        
        passwordField.selectAll();
        addTextFieldListeners(passwordField, false);
        addRow(Translator.get("password"), passwordField, 15);

    }

    private void updateValues() {
        lastServer = serverField.getText();
        lastUsername = usernameField.getText();
        lastPassword = passwordField.getText();
    }

    ////////////////////////////////
    // ServerPanel implementation //
    ////////////////////////////////
    @Override
    FileURL getServerURL() throws MalformedURLException {
        updateValues();

        
        int port = FileURL.getRegisteredHandler(FileProtocols.WEBDAV).getStandardPort();
        
        String url = FileProtocols.WEBDAV + "://"+lastUsername+":"+lastPassword+"@" + lastServer;
        
        try {
            URI uri = new URI(lastServer);
            if(uri.getScheme() != null){
                if(uri.getScheme().equalsIgnoreCase("http")){
                    port = 80;
                } else if(uri.getScheme().equalsIgnoreCase("https")){
                    port = 443;
                }
                url = FileProtocols.WEBDAV + "://" + lastUsername + ":" + lastPassword + "@" + uri.getHost() + ":" + port + "" + uri.getPath();
            }
        } catch (URISyntaxException ex) {
            Logger.getLogger(WebDAVPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        FileURL fileUrl = FileURL.getFileURL(url);

        fileUrl.setCredentials(new Credentials(lastUsername, lastPassword));

        // Set port
        fileUrl.setPort(port);

        return fileUrl;
    }

    @Override
    boolean usesCredentials() {
        return true;
    }

    @Override
    public void dialogValidated() {
        updateValues();
    }
}
