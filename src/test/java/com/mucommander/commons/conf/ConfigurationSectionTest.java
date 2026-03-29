package com.mucommander.commons.conf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;

/**
 * A test case for the {@link Configuration} class.
 * @author Nicolas Rinaudo
 */
public class ConfigurationSectionTest {
    /**
     * Tests the {@link ConfigurationSection#removeVariable(String)} method.
     * @param value value to which the variable should be set before being removed.
     */
//    @Test(dataProvider = "removeVariable")
    @ParameterizedTest
    @ValueSource(strings = {"value"})
    public void testRemoveVariable(String value) {
        ConfigurationSection section = new ConfigurationSection();

        assert section.setVariable("var", value);
        assertVariable(section, "var", value);

        assert value == null ? section.removeVariable("var") == null : section.removeVariable("var").equals(value);
    }



    /**
     * Makes sure that the specified variable has the specified value.
     * @param section section in which the variable to test is located.
     * @param var     name of the variable to test.
     * @param value   expected variable value.
     */
    private void assertVariable(ConfigurationSection section, String var, String value) {
        if(value == null || value.isEmpty())
            assert section.getVariable(var) == null: "Expected null but found " + section.getVariable(var);
        else {
            assert section.getVariable(var).equals(value): "Expected " + value + " but found " + section.getVariable(var);
        }
    }

    /**
     * Tests the {@link ConfigurationSection#setVariable(String, String)} method.
     * @param first    first value to which the test variable should be set.
     * @param second   second value to which the test variable should be set.
     * @param expected expected return value of the second call to
     *                 {@link ConfigurationSection#setVariable(String, String)}.
     */
    @ParameterizedTest
    @CsvSource({
        "value, other, true",
        "value, value, false",
        "value, null, true",
        "value, ,   true"
    })
    public void testSetVariable(String first, String second, boolean expected) {
        ConfigurationSection section = new ConfigurationSection();

        assert section.setVariable("var", first);
        assertVariable(section, "var", first);

        assert expected == section.setVariable("var", second);
        assertVariable(section, "var", second);
    }



    /**
     * Utility method for {@link #testVariableNames()}.
     * @param section section to explore.
     * @param count   number of expected variable names.
     */
    private void assertVariableNames(ConfigurationSection section, int count) {
        // No expected variables, make sure that the section reflects that.
        if(count == 0) {
            assert !section.hasVariables();
            assert section.isEmpty();
            assert section.variableNames().isEmpty();
        }

        // Makes sure that the section contains exactly var1, var2..., var<count>.
        // We have to go through a set here: the order in which we'll iterate over the variable names is unreliable.
        else {
            assert section.hasVariables();
            assert !section.isEmpty();

            // Populates a set will all the expected variable names.
            Set<String> expectedNames = new HashSet<>(count);
            for(int i = 0; i < count; i++)
                expectedNames.add("var" + i);

            // Makes sure that we can remove all of the section's variables, and that none remains afterward.
            Set<String> names = section.variableNames();
            for (String name : names) {
                assert expectedNames.remove(name);
            }
            assert expectedNames.isEmpty();
        }
    }

    /**
     * Tests the {@link ConfigurationSection#variableNames()} method.
     */
    @Test
    public void testVariableNames() {
        ConfigurationSection section = new ConfigurationSection();

        // create 10 variables.
        for(int i = 0; i < 10; i++)
            section.setVariable("var" + i, "value");

        assertVariableNames(section, 10);

        section.setVariable("var9", "");
        assertVariableNames(section, 9);

        section.setVariable("var8", null);
        assertVariableNames(section, 8);

        section.removeVariable("var7");
        assertVariableNames(section, 7);


        for(int i = 0; i < 7; i++)
            section.removeVariable("var" + i);
        assertVariableNames(section, 0);
    }


    /**
     * Tests the list value helpers.
     */
    @Test
    public void testLists() {
        ValueList list = new ValueList("1,2,3,4", ",");
        assert ConfigurationSection.getListValue("1,2,3,4", ",").equals(list);
        assert ConfigurationSection.getValue(list, ",").equals("1,2,3,4");
    }

    /**
     * Tests the long value helpers.
     */
    @Test
    public void testLongs() {
        for(long i = 0; i < 10; i++) {
            assert ConfigurationSection.getLongValue(Long.toString(i)) == i;
            assert ConfigurationSection.getValue(i).equals(Long.toString(i));
        }

        assert ConfigurationSection.getLongValue(null) == 0;
    }

    /**
     * Tests the integer value helpers.
     */
    @Test
    public void testIntegers() {
        for(int i = 0; i < 10; i++) {
            assert ConfigurationSection.getIntegerValue(Integer.toString(i)) == i;
            assert ConfigurationSection.getValue(i).equals(Integer.toString(i));
        }

        assert ConfigurationSection.getIntegerValue(null) == 0;
    }
    
    /**
     * Tests the double value helpers.
     */
    @Test
    public void testDoubles() {
        for(int i = 0; i < 10; i++) {
            assert ConfigurationSection.getDoubleValue(i + ".5") == (i + 0.5d);
            assert ConfigurationSection.getValue((i + 0.5d)).equals(i + ".5");
        }

        assert ConfigurationSection.getDoubleValue(null) == 0f;
    }

    /**
     * Tests the float value helpers.
     */
    @Test
    public void testFloats() {
        for(int i = 0; i < 10; i++) {
            assert ConfigurationSection.getFloatValue(i + ".5") == (i + 0.5f);
            assert ConfigurationSection.getValue((i + 0.5f)).equals(i + ".5");
        }

        assert ConfigurationSection.getFloatValue(null) == 0f;
    }

    /**
     * Tests the boolean value helpers.
     */
    @Test
    public void testBooleans() {
        assert ConfigurationSection.getBooleanValue("true");
        assert ConfigurationSection.getValue(true).equals("true");

        assert !ConfigurationSection.getBooleanValue("false");
        assert !ConfigurationSection.getBooleanValue("!@#");
        assert !ConfigurationSection.getBooleanValue("");
        assert ConfigurationSection.getValue(false).equals("false");

        assert !ConfigurationSection.getBooleanValue(null);
    }



    /**
     * Tests various section related method.
     */
    @Test
    public void testSections() {
        ConfigurationSection section = new ConfigurationSection();

        // Makes sure we can add a section.
        ConfigurationSection buffer = section.addSection("sect");
        assert buffer != null;
        assert section.hasSections();

        // Makes sure that adding the same section twice yields the same instance.
        assert section.addSection("sect") == buffer;
        assert section.hasSections();

        // Makes sure we can remove a section by name.
        assert section.removeSection("sect") == buffer;
        assert section.getSection("sect") == null;
        assert !section.hasSections();

        // Makes sure we can remove a section by value.
        buffer = section.addSection("sect");
        assert section.removeSection(buffer);
        assert buffer.getSection("sect") == null;
        assert !section.hasSections();
        assert !section.removeSection(buffer);
    }

    /**
     * Utility method for {@link #testSectionNames()}.
     * @param section section to explore.
     * @param count   number of expected section names.
     */
    private void assertSectionNames(ConfigurationSection section, int count) {
        // No expected variables, make sure that the section reflects that.
        if(count == 0) {
            assert !section.hasSections();
            assert section.isEmpty();
            assert section.sectionNames().isEmpty();
        }

        // Makes sure that the section contains exactly sect1, sect2..., sect<count>.
        // We have to go through a set here: the order in which we'll iterate over the variable names is unreliable.
        else {
            assert section.hasSections();
            assert !section.isEmpty();

            // Populates a set will all the expected variable names.
            Set<String> expectedNames = new HashSet<>(count);
            for(int i = 0; i < count; i++)
                expectedNames.add("sect" + i);

            // Makes sure that we can remove all of the section's sub-sections, and that none remains afterward.
            Set<String> names = section.sectionNames();
            for (String name : names) {
                assert expectedNames.remove(name);
            }

            assert expectedNames.isEmpty();
        }
    }

    @Test
    public void testSectionNames() {
        ConfigurationSection section = new ConfigurationSection();

        // create 10 variables.
        ConfigurationSection buffer = null;
        for(int i = 0; i < 10; i++)
            buffer = section.addSection("sect" + i);
        assertSectionNames(section, 10);

        // Removes the last section by value, makes sure we're in a coherent state.
        assert section.removeSection(buffer);
        assertSectionNames(section, 9);

        // Removes the last section by name, makes sure we're in a coherent state.
        assert section.removeSection("sect8") != null;
        assertSectionNames(section, 8);

        for(int i = 0; i < 8; i++)
            assert section.removeSection("sect" + i) != null;
        assertSectionNames(section, 0);
    }
}
