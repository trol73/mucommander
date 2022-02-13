/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.auth;

import com.mucommander.bookmark.XORCipher;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.FileURL;
import com.mucommander.io.backup.BackupInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Hashtable;
import java.util.Map;
import javax.xml.parsers.SAXParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class takes care of parsing the credentials XML file and adding parsed {@link CredentialsMapping} instances
 * to {@link CredentialsManager}.
 *
 * @author Maxence Bernard
 * @see CredentialsWriter
 */
class CredentialsParser extends DefaultHandler implements CredentialsConstants {
	private static Logger logger;
	
    // Variables used for XML parsing
    private FileURL url;
    private Map<String, String> urlProperties;
    private String login;
    private String password;
    private StringBuilder characters;

    /** muCommander version that was used to write the credentials file */
    private String version;

    /** Contains the encryption method used to encrypt/decrypt passwords */
    private String encryptionMethod;


    /**
     * Creates a new CredentialsParser.
     */
    public CredentialsParser() {
    }


    /**
     * Parses the given XML credentials file. Should only be called by CredentialsManager.
     */
    void parse(AbstractFile file) throws Exception {
        characters = new StringBuilder();
        try (InputStream in  = new BackupInputStream(file)) {
            SAXParserFactory.newInstance().newSAXParser().parse(in, this);
        }
    }

    /**
     * Returns the muCommander version that was used to write the credentials file, <code>null</code> if it is unknown.
     * <p>
     * Note: the version attribute was introduced in muCommander 0.8.4.
     *
     * @return the muCommander version that was used to write the credentials file, <code>null</code> if it is unknown.
     */
    public String getVersion() {
        return version;
    }


    ///////////////////////////////////
    // ContentHandler implementation //
    ///////////////////////////////////

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        characters.setLength(0);

        switch (qName) {
            case ELEMENT_CREDENTIALS:
            // Reset parsing variables
            url = null;
            urlProperties = null;
            login = null;
            password = null;
                break;
        // Property element (properties will be set when credentials element ends
            case ELEMENT_PROPERTY:
                if (urlProperties == null)
                urlProperties = new Hashtable<>();
            urlProperties.put(attributes.getValue(ATTRIBUTE_NAME), attributes.getValue(ATTRIBUTE_VALUE));
                break;
        // Root element, the 'encryption' attribute specifies which encoding was used to encrypt passwords
            case ELEMENT_ROOT:
            encryptionMethod = attributes.getValue("encryption");
            version = attributes.getValue(ATTRIBUTE_VERSION);
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        switch (qName) {
            case ELEMENT_CREDENTIALS:
                if (url == null || login == null || password == null) {
                    getLogger().info("Missing value, credentials ignored: url=" + url + " login=" + login);
                return;
            }

            // Copy properties into FileURL instance (if any)
                if (urlProperties != null) {
                    for (String key : urlProperties.keySet())
                    url.setProperty(key, urlProperties.get(key));
            }

            // Decrypt password
                try {
                    password = XORCipher.decryptXORBase64(password);
                } catch (IOException e) {
                    getLogger().info("Password could not be decrypted: " + password + ", credentials will be ignored");
                return;
            }

            // Add credentials to persistent credentials list
            CredentialsManager.getPersistentCredentialMappings().add(new CredentialsMapping(new Credentials(login, password), url, true));
                break;
            case ELEMENT_URL:
                try {
                    url = FileURL.getFileURL(characters.toString().trim());
                } catch (MalformedURLException e) {
                    getLogger().info("Malformed URL: " + characters + ", location will be ignored");
        }
                break;
            case ELEMENT_LOGIN:
            login = characters.toString().trim();
                break;
            case ELEMENT_PASSWORD:
            password = characters.toString().trim();
                break;
        }
    }

    @Override
    public void characters(char[] ch, int offset, int length) {
        characters.append(ch, offset, length);
    }


    private static Logger getLogger() {
        if (logger == null) {
            logger = LoggerFactory.getLogger(CredentialsParser.class);
        }
        return logger;
    }
}
