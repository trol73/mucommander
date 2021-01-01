package com.mucommander.utils.xml;

import java.util.*;

/**
 * Container for XML attributes.
 * <p>
 * This class is meant for use with {@link com.mucommander.utils.xml.XmlWriter}.
 * It's used to hold a list of XML attributes that will be passed to one of
 * the {@link com.mucommander.utils.xml.XmlWriter#startElement(String,XmlAttributes) element opening}
 * methods.
 *
 * @author Nicolas Rinaudo, Arik Hadas
 */
public class XmlAttributes {
    /** Contains the XML attributes. */
    private final Map<String, String> attributes = new HashMap<>();
    /** Contains the XML attribute names in the order they were added */
    private final LinkedList<String> names = new LinkedList<>();

    /**
     * Returns the value associated with the specified attribute name.
     * @param name name of the attribute whose value should be retrieved.
     * @return the value associated with the specified attribute name if found,
     *         <code>null</code> otherwise.
     */
    public String getValue(String name) {
        return attributes.get(name);
    }

    /**
     * Clears the list of all previously defined attributes.
     */
    public void clear() {
        names.clear(); attributes.clear();
    }

    /**
     * Adds the specified attribute to this container.
     * @param name  name of the attribute to whose value should be set.
     * @param value value to which the attribute should be set.
     */
    public void add(String name, String value) {
        names.add(name); attributes.put(name, value);
    }

    /**
     * Returns an iterator on the attributes contained by this instance.
     * @return an iterator on the attributes contained by this instance.
     */
    public Iterator<String> names() {
        return names.iterator();
    }
}
