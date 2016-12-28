package com.mucommander.commons.file;

import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Used to import system properties and disable specific class tests at runtime.
 * @author Nicolas Rinaudo
 */
public class FileTestTransformer implements IMethodInterceptor {
    // - Constants -----------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    private static final String PATH_PROPERTY = "test.properties.file";



    // - Initialisation ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    static {
        String      path;
        InputStream in;
        Properties  properties;

        // Makes sure the required System property is set.
        if((path = System.getProperty(PATH_PROPERTY)) == null)
            throw new IllegalStateException(PATH_PROPERTY + " not set.");

        // Loads the properties.
        in = null;
        try {
            in = new FileInputStream(new File(path));
            properties = new Properties();
            properties.load(in);
        }
        catch(IOException e) {throw new IllegalStateException(e);}
        finally {
            if(in != null) {
                try {in.close();}
                catch(IOException e) {
                    // Nothing we can do about this.
                }
            }
        }

        // Configures the system properties.
        for(Object key: properties.keySet())
            System.setProperty(key.toString(), properties.get(key).toString());
    }



    // - IAnnotationTransformer implementation -------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------

    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        List<IMethodInstance> out;
        String value;

        out = new ArrayList<>();
        for(IMethodInstance method: methods) {
            Class aClass;

            aClass = method.getMethod().getTestClass().getRealClass();
            if(aClass == null || (value = System.getProperty(aClass.getName() + "#enabled")) == null ||
                    Boolean.parseBoolean(value))
                out.add(method);
        }

        return out;
    }
}
