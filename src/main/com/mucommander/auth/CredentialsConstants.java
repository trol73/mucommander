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

/**
 * Contains XML elements and attributes used to parse and write the credentials file.
 *
 * @author Maxence Bernard
 */
interface CredentialsConstants {

    /** Root element */
    String ELEMENT_ROOT     = "credentials_list";

    /** Element for each credential item, containg a URL, login and password */
    String ELEMENT_CREDENTIALS = "credentials";

    /** Element containing the credentials' URL */
    String ELEMENT_URL      = "url";

    /** Element containing the credentials' login */
    String ELEMENT_LOGIN    = "login";

    /** Element containing the credentials' (encrypted) password*/
    String ELEMENT_PASSWORD = "password";

    /** Element that defines a property (name/value pair) */
    String ELEMENT_PROPERTY = "property";

    /** Name attribute of the property element */

    String ATTRIBUTE_NAME = "name";

    /** Value attribute of the property element */
    String ATTRIBUTE_VALUE = "value";

    /** Name of the root element's attribute containing the muCommander version that was used to create the credentials file */
    String ATTRIBUTE_VERSION = "version";

    /** Root element's attribute containing the encryption method used for passwords */
    String ATTRIBUTE_ENCRYPTION = "encryption";

    /** Weak password encryption method */
    String WEAK_ENCRYPTION_METHOD = "weak";

}
