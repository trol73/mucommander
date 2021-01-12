package com.mucommander.commons.file;

/**
 * This class is a container for a login and password pair, used to authenticate a location on a filesystem.
 *
 * @see com.mucommander.commons.file.FileURL
 * @author Maxence Bernard
 */
public final class Credentials {

    private final String login;
    private final String password;
	
    /**
     * Creates a new instance with the supplied login and password.
     * Any provided null values will be replaced by empty strings.
     *
     * @param login    the login part as a string
     * @param password the password part as a string
     */
    public Credentials(String login, String password) {
        this.login = login == null ? "" : login;
        this.password = password == null ? "" : password;
    }

    /**
     * Returns the login part. The returned login may be an empty string but never <code>null</code>.
     *
     * @return the login part.
     */
    public String getLogin() {
        return login;
    }
	
    /**
     * Returns the password part. The returned password may be an empty string but never <code>null</code>.
     *
     * @return the password part.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the password as a masked string, each of the characters replaced by '*' characters.
     *
     * @return the password as a masked string.
     */
    public String getMaskedPassword() {
        int passwordLength = password.length();
        StringBuilder maskedPasswordSb = new StringBuilder(passwordLength);
        for (int i = 0; i < passwordLength; i++) {
            maskedPasswordSb.append('*');
        }
        return maskedPasswordSb.toString();
    }

    /**
     * Returns <code>true</code> if these credentials are empty.
     * <p>
     * Credentials are said to be empty if both login and password are empty strings.
     *
     * @return <code>true</code> if these credentials are empty, <code>false</code> otherwise.
     */
    public boolean isEmpty() {
        return "".equals(login) && "".equals(password);
    }


    /**
     * This method is equivalent to calling {@link #equals(Object, boolean)} with <code>false</code>:
     * two Credentials instances with the same login but a different password are considered equal.
     *
     * @param o the Object to test for equality
     * @return true if this and the specified instance are equal
     * @see #equals(Object, boolean)
     */
    @Override
    public boolean equals(Object o) {
        return equals(o, false);
    }

    /**
     * Returns <code>true</code> if these Credentials and the specified instance are equal. For credentials to be equal,
     * their login (as returned by {@link #getLogin()} must be equal. If the password-sensitive parameter is enabled,
     * their passwords (as returned by {@link #getPassword()} must also match.
     *
     * <p>
     * Empty Credentials and <code>null</code> are considered equal: if a <code>null</code> instance is specified,
     * <code>true</code> is returned if these Credentials are {@link #isEmpty() empty}).
     *
     * @param o the Object to test for equality
     * @param passwordSensitive true if passwords need to be equal for credentials instances to match
     * @return true if this and the specified instance are equal
     */
    public boolean equals(Object o, boolean passwordSensitive) {
        // Empty Credentials and null are equivalent
        if (o == null) {
            return isEmpty();
        }
        if (!(o instanceof Credentials)) { // Note: this class is declared final so we don't need to worry about subclasses
            return false;
        }

        Credentials credentials = (Credentials)o;
        return credentials.login.equals(this.login) && (!passwordSensitive || credentials.password.equals(this.password));
    }

    /**
     * Returns a cloned instance of these Credentials.
     *
     * @return a cloned instance of these Credentials
     */
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // Should never happen
            return null;
        }
    }

    public String toString() {
        return login;
    }

    public int hashCode() {
        // Do not take into account the password, as #equals(Object) is password-insensitive
        return login.hashCode();
    }
}

