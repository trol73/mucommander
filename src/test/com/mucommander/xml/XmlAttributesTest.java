package com.mucommander.xml;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Iterator;

/**
 * Runs test on the {@link XmlAttributes} class.
 * @author Nicolas Rinaudo
 */
public class XmlAttributesTest {
    // - Test constants ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /** Name of the first test attribute. */
    private static final String TEST_ATTRIBUTE_1 = "attribute1";
    /** Name of the second test attribute. */
    private static final String TEST_ATTRIBUTE_2 = "attribute2";
    /** First value of the test attribute. */
    private static final String TEST_VALUE_1     = "value1";
    /** Second value of the test attribute. */
    private static final String TEST_VALUE_2     = "value2";



    // - Instance fields -----------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /** Instance used to test the XmlAttributes class. */
    private XmlAttributes attributes;



    // - Initialisation ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Initialises the test case.
     */
    @BeforeMethod
    public void setUp() {
        attributes = new XmlAttributes();
    }



    // - Test code -----------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Runs the basic tests.
     */
    @Test
    public void testAttributes() {
        // Makes sure an attribute that has been added is properly retrieved.
        attributes.add(TEST_ATTRIBUTE_1, TEST_VALUE_1);
        assert TEST_VALUE_1.equals(attributes.getValue(TEST_ATTRIBUTE_1));

        // Makes sure an attribute that has been overwritten is properly retrieved.
        attributes.add(TEST_ATTRIBUTE_1, TEST_VALUE_2);
        assert TEST_VALUE_2.equals(attributes.getValue(TEST_ATTRIBUTE_1));

        // Makes sure the clear method works.
        attributes.clear();
        assert attributes.getValue(TEST_ATTRIBUTE_1) == null;
    }

    /**
     * Runs tests on the {@link XmlAttributes#names()} method.
     */
    @Test
    public void testNames() {
        Iterator<String> names;
        String   buffer;

        // Makes sure the names method works on an empty set of attributes.
        names = attributes.names();
        assert !names.hasNext();

        // Makes sure the names method works on a set of attributes that only contains
        // one element.
        attributes.add(TEST_ATTRIBUTE_1, TEST_VALUE_1);
        names = attributes.names();
        assert names.hasNext();
        buffer = names.next();
        assert TEST_ATTRIBUTE_1.equals(buffer);
        assert TEST_VALUE_1.equals(attributes.getValue(buffer));
        assert !names.hasNext();

        // Makes sure the names method works on a set of attributes that contains more
        // than one element.
        attributes.add(TEST_ATTRIBUTE_2, TEST_VALUE_2);
        names = attributes.names();
        assert names.hasNext();
        checkAttribute(names.next());
        assert names.hasNext();
        checkAttribute(names.next());
        assert !names.hasNext();

        // Makes sure the iterator is read-only.
        names = attributes.names();
        try {
            names.remove();
            throw new AssertionError();
        }
        catch(Exception e) {}
    }

    /**
     * Makes sure the specified attribute name has the right value.
     */
    private void checkAttribute(String name) {
        switch (name) {
            case TEST_ATTRIBUTE_1:
                assert TEST_VALUE_1.equals(attributes.getValue(TEST_ATTRIBUTE_1));
                break;
            case TEST_ATTRIBUTE_2:
                assert TEST_VALUE_2.equals(attributes.getValue(TEST_ATTRIBUTE_2));
                break;
            default:
                throw new AssertionError();
        }
    }
}
