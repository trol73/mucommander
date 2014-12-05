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

package com.mucommander.text;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import com.mucommander.RuntimeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.util.ResourceLoader;
import com.mucommander.commons.io.bom.BOMReader;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;


/**
 * This class takes care of all text localization issues by loading all text entries from a dictionary file on startup
 * and translating them into the current language on demand.
 *
 * <p>All public methods are static to make it easy to call them throughout the application.</p>
 *
 * <p>See dictionary file for more information about th dictionary file format.</p>
 *
 * @author Maxence Bernard, Arik Hadas
 */
public class Translator {
	private static final Logger LOGGER = LoggerFactory.getLogger(Translator.class);
	
    /** List of all available languages in the dictionary file */
    private static List<Locale> availableLanguages = new ArrayList<>();

    /** Current language */
    private static Locale language;

    private static ResourceBundle bundle;
    /**
     * Prevents instance creation.
     */
    private Translator() {
    }

    static {
        registerLocale(Locale.forLanguageTag("ar-SA"));
        registerLocale(Locale.forLanguageTag("be-BY"));
        registerLocale(Locale.forLanguageTag("ca-ES"));
        registerLocale(Locale.forLanguageTag("cs-CZ"));
        registerLocale(Locale.forLanguageTag("da-DA"));
        registerLocale(Locale.forLanguageTag("de-DE"));
        registerLocale(Locale.forLanguageTag("en-GB"));
        registerLocale(Locale.forLanguageTag("en-US"));
        registerLocale(Locale.forLanguageTag("es-ES"));
        registerLocale(Locale.forLanguageTag("fr-FR"));
        registerLocale(Locale.forLanguageTag("hu-HU"));
        registerLocale(Locale.forLanguageTag("it-IT"));
        registerLocale(Locale.forLanguageTag("ja-JP"));
        registerLocale(Locale.forLanguageTag("ko-KR"));
        registerLocale(Locale.forLanguageTag("no-NO"));
        registerLocale(Locale.forLanguageTag("nl-NL"));
        registerLocale(Locale.forLanguageTag("pl-PL"));
        registerLocale(Locale.forLanguageTag("pt-BR"));
        registerLocale(Locale.forLanguageTag("ro-RO"));
        registerLocale(Locale.forLanguageTag("ru-RU"));
        registerLocale(Locale.forLanguageTag("sk-SK"));
        registerLocale(Locale.forLanguageTag("sl-SL"));
        registerLocale(Locale.forLanguageTag("sv-SV"));
        registerLocale(Locale.forLanguageTag("tr-TR"));
        registerLocale(Locale.forLanguageTag("uk-UA"));
        registerLocale(Locale.forLanguageTag("zh-CN"));
        registerLocale(Locale.forLanguageTag("zh-TW"));
    }


    public static void registerLocale(Locale locale) {
        availableLanguages.add(locale);
    }


    private static Locale getLocale() {
        String localeNameFromConf = MuConfigurations.getPreferences().getVariable(MuPreference.LANGUAGE);
        if (localeNameFromConf == null) {
            // language is not set in preferences, use system's language
            // Try to match language with the system's language, only if the system's language
            // has values in dictionary, otherwise use default language (English).
            Locale defaultLocale = Locale.getDefault();
            LOGGER.info("Language not set in preferences, trying to match system's language ("+defaultLocale+")");
            return defaultLocale;
        }

        LOGGER.info("Using language set in preferences: "+localeNameFromConf);
        switch (localeNameFromConf) {
            // for backward compatibility
            case "EN": return Locale.forLanguageTag("en-US");
            case "en_GB": return Locale.forLanguageTag("en-GB");
            case "FR": return Locale.forLanguageTag("fr-FR");
            case "DE": return Locale.forLanguageTag("de-DE");
            case "ES": return Locale.forLanguageTag("es-ES");
            case "CS": return Locale.forLanguageTag("cs-CZ");
            case "zh_CN": return Locale.forLanguageTag("zh-CN");
            case "zh_TW": return Locale.forLanguageTag("zh-TW");
            case "PL": return Locale.forLanguageTag("pl-PL");
            case "HU": return Locale.forLanguageTag("hu-HU");
            case "RU": return Locale.forLanguageTag("ru-RU");
            case "SL": return Locale.forLanguageTag("sl-SL");
            case "RO": return Locale.forLanguageTag("ro-RO");
            case "IT": return Locale.forLanguageTag("it-IT");
            case "KO": return Locale.forLanguageTag("ko-KR");
            case "pt_BR": return Locale.forLanguageTag("pt-BR");
            case "NL": return Locale.forLanguageTag("nl-NL");
            case "SK": return Locale.forLanguageTag("sk-SK");
            case "JA": return Locale.forLanguageTag("ja-JP");
            case "SV": return Locale.forLanguageTag("sv-SV");
            case "DA": return Locale.forLanguageTag("da-DA");
            case "UA": return Locale.forLanguageTag("uk-UA");
            case "AR": return Locale.forLanguageTag("ar-SA");
            case "BE": return Locale.forLanguageTag("be-BY");
            case "NB": return Locale.forLanguageTag("no-NO");
            case "TR": return Locale.forLanguageTag("tr-TR");
            case "CA": return Locale.forLanguageTag("ca-ES");
            default: return Locale.forLanguageTag(localeNameFromConf);
        }
    }

    public static void init() throws IOException {
        Locale locale = getLocale();

        // Determines if language is one of the languages declared as available
        if (availableLanguages.contains(locale)) {
            // Language is available
            bundle = ResourceBundle.getBundle("dictionary", locale, new UTF8Control());
            LOGGER.debug("Language " + locale + " is available.");
        } else {
            // Language is not available, fall back to default language
            bundle = ResourceBundle.getBundle("dictionary", new UTF8Control());
            LOGGER.debug("Language " + locale + " is not available, falling back to English");
        }
        // Set preferred language in configuration file
        MuConfigurations.getPreferences().setVariable(MuPreference.LANGUAGE, locale.toLanguageTag());

        LOGGER.debug("Current language has been set to "+Translator.language);
    }

    /**
     * Returns the current language as a language code ("EN", "FR", "pt_BR", ...).
     *
     * @return lang a language code
     */
    public static String getLanguage() {
        return language.getLanguage();
    }
	
	
    /**
     * Returns an array of available languages, expressed as language codes ("EN", "FR", "pt_BR"...).
     * The returned array is sorted by language codes in case insensitive order.
     *
     * @return an array of language codes.
     */
    public static List<Locale>  getAvailableLanguages() {
        return availableLanguages;
    }


    /**
     * Returns <code>true</code> if the given entry's key has a value in the current language.
     * If the <code>useDefaultLanguage</code> parameter is <code>true</code>, entries that have no value in the
     * {@link #getLanguage() current language} but one in English will be considered as having
     * a value (<code>true</code> will be returned).
     *
     * @param key key of the requested dictionary entry (case-insensitive)
     * @param useDefaultLanguage if <code>true</code>, entries that have no value in the {@link #getLanguage() current
     * language} but one in English will be considered as having a value
     * @return <code>true</code> if the given key has a corresponding value in the current language.
     */
    public static boolean hasValue(String key, boolean useDefaultLanguage) {
        return bundle.containsKey(key);
    }

    /**
     * Returns the localized text String for the given key expressd in the current language, or in the default language
     * if there is no value for the current language. Entry parameters (%1, %2, ...), if any, are replaced by the
     * specified values.
     *
     * @param key key of the requested dictionary entry (case-insensitive)
     * @param paramValues array of parameters which will be used as values for variables.
     * @return the localized text String for the given key expressed in the current language
     */
    public static String get(String key, String... paramValues) {
        String text;
        try {
            text = bundle.getString(key);
        } catch (Exception e) {
            text = key;
            System.out.println("No value for " + key+" in language " + language + ", using English value");
            LOGGER.debug("No value for " + key + " in language " + language + ", using English value");
new Exception().printStackTrace();
        }

        // Replace %1, %2 ... parameters by their value
        if (paramValues != null) {
            int pos = -1;
            for (int i = 0; i<paramValues.length; i++) {
                while (++pos < text.length()-1 && (pos = text.indexOf("%"+(i+1), pos)) != -1) {
                    text = text.substring(0, pos) + paramValues[i] + text.substring(pos + 2, text.length());
                }
            }
        }

        return text;
    }


    /**
     * Based on the number of supplied command line parameters, this method either :
     * <ul>
     * <li>Looks for and reports any missing or unused dictionary entry,
     * using the supplied source folder path to look inside source files
     * for references to dictionary entries.
     * <li>Merges a new language's entries from a dictionary file into a new one.
     * </ul>
     */
    public static void main(String args[]) throws IOException {
//__splitResources();
//if (1==1) return;
        /*	
        // Looks for missing and unused entries
        if(args.length<4) {
        Enumeration languages = dictionaries.keys();
        Vector langsV = new Vector();
        while(languages.hasMoreElements())
        langsV.add(languages.nextElement());
				
        String langs[] = new String[langsV.size()];
        langsV.toArray(langs);
			
        com.mucommander.commons.file.AbstractFile sourceFolder = com.mucommander.commons.file.AbstractFile.getFile(args[0]);
		
        System.out.println("\n##### Looking for missing entries #####");
        checkMissingEntries(sourceFolder, langs);

        System.out.println("\n##### Looking for unused entries #####");
        checkUnusedEntries(sourceFolder, langs);
        }
        // Integrates a new language into the dictionary
        else {
        */
        // Parameters order: originalFile newLanguageFile resultingFile newLanguage
        if (args.length < 4) {
            System.out.println("usage: Translator originalFile newLanguageFile mergedFile newLanguage");
            return;
        }

        addLanguageToDictionary(args[0], args[1], args[2], args[3]);
        /*
          }
        */
    }


    /**
     * Merges a dictionary file with another one, adding entries of the specified new language.
     * <p>This method is used to merge dictionary files sent by contributors.
     *
     * @param originalFile current version of the dictionary file
     * @param newLanguageFile dictionary file containing new language entries
     * @param resultingFile merged dictionary file
     * @param newLanguage new language
     * @throws IOException if an I/O error occurred
     */
    private static void addLanguageToDictionary(String originalFile, String newLanguageFile, String resultingFile, String newLanguage) throws IOException {
        // Initialize streams
        BufferedReader originalFileReader = new BufferedReader(new BOMReader(new FileInputStream(originalFile)));
        BufferedReader newLanguageFileReader = new BufferedReader(new BOMReader(new FileInputStream(newLanguageFile)));
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(resultingFile), "UTF-8"));

        // Parse new language's entries
        String line;
        int lineNum = 0;
        String key;
        String lang;
        String text;
        StringTokenizer st;
        Map<String, String> newLanguageEntries = new HashMap<>();
        while ((line = newLanguageFileReader.readLine())!=null) {
            try {
                if (!line.trim().startsWith("#") && !line.trim().equals("")) {
                    st = new StringTokenizer(line);

                    // Sets delimiter to ':'
                    key = st.nextToken(":");
                    lang = st.nextToken();

                    if (lang.equalsIgnoreCase(newLanguage)) {
                        // Delimiter is now line break
                        text = st.nextToken("\n");
                        text = text.substring(1, text.length());

                        newLanguageEntries.put(key, text);
                    }
                }
                lineNum++;
            } catch(Exception e) {
            	LOGGER.warn("caught "+e+" at line "+lineNum);
                return;
            }
        }

        // Insert new language entries in resulting file
        boolean keyProcessedForNewLanguage = false;
        String currentKey = null;
        while ((line = originalFileReader.readLine())!=null) {
            boolean emptyLine = line.trim().startsWith("#") || line.trim().equals("");
            if (!keyProcessedForNewLanguage && (emptyLine || (currentKey!=null && !line.startsWith(currentKey+":")))) {
                if(currentKey!=null) {
                    String newLanguageValue = newLanguageEntries.get(currentKey);
                    if(newLanguageValue!=null) {
                        // Insert new language's entry in resulting file
                    	LOGGER.info("New language entry for key="+currentKey+" value="+newLanguageValue);
                        pw.println(currentKey+":"+newLanguage+":"+newLanguageValue);
                    }

                    keyProcessedForNewLanguage = true;
                }
            }

            if (!emptyLine) {
                // Parse entry
                st = new StringTokenizer(line);

                // Set delimiter to ':'
                key = st.nextToken(":");
                lang = st.nextToken();

                if (!key.equals(currentKey)) {
                    currentKey = key;
                    keyProcessedForNewLanguage = false;
                }

                if (lang.equalsIgnoreCase(newLanguage)) {
                    // Delimiter is now line break
                    String existingNewLanguageValue = st.nextToken("\n");
                    existingNewLanguageValue = existingNewLanguageValue.substring(1, existingNewLanguageValue.length());
                    String newLanguageValue = newLanguageEntries.get(currentKey);

                    if(newLanguageValue!=null) {
                        if(!existingNewLanguageValue.equals(newLanguageValue))
                        	LOGGER.warn("Warning: found an updated value for key="+currentKey+", using new value="+newLanguageValue+" existing value="+existingNewLanguageValue);

                        pw.println(currentKey+":"+newLanguage+":"+newLanguageValue);
                    } else {
                    	LOGGER.warn("Existing dictionary has a value for key="+currentKey+" that is missing in the new dictionary file, using existing value= "+existingNewLanguageValue);
                        pw.println(currentKey+":"+newLanguage+":"+existingNewLanguageValue);
                    }

                    keyProcessedForNewLanguage = true;
                } else {
                    pw.println(line);
                }
            } else {
                pw.println(line);
            }
        }

        newLanguageFileReader.close();
        originalFileReader.close();
        pw.close();
    }



    private static void __splitResources() throws IOException {
        BufferedReader br = new BufferedReader(new BOMReader(new FileInputStream("/Users/trol/Projects/java/mucommander/res/runtime/dictionary.txt")));

        Map<String, List<String>> data = new HashMap<>();
        data.put(_l2("EN"), new ArrayList<String>());
        data.put(_l2("en_GB"), new ArrayList<String>());
        data.put(_l2("FR"), new ArrayList<String>());
        data.put(_l2("DE"), new ArrayList<String>());
        data.put(_l2("ES"), new ArrayList<String>());
        data.put(_l2("CS"), new ArrayList<String>());
        data.put(_l2("zh_CN"), new ArrayList<String>());
        data.put(_l2("zh_TW"), new ArrayList<String>());
        data.put(_l2("PL"), new ArrayList<String>());
        data.put(_l2("HU"), new ArrayList<String>());
        data.put(_l2("RU"), new ArrayList<String>());
        data.put(_l2("SL"), new ArrayList<String>());
        data.put(_l2("RO"), new ArrayList<String>());
        data.put(_l2("IT"), new ArrayList<String>());
        data.put(_l2("KO"), new ArrayList<String>());
        data.put(_l2("pt_BR"), new ArrayList<String>());
        data.put(_l2("NL"), new ArrayList<String>());
        data.put(_l2("SK"), new ArrayList<String>());
        data.put(_l2("JA"), new ArrayList<String>());
        data.put(_l2("SV"), new ArrayList<String>());
        data.put(_l2("DA"), new ArrayList<String>());
        data.put(_l2("UA"), new ArrayList<String>());
        data.put(_l2("AR"), new ArrayList<String>());
        data.put(_l2("BE"), new ArrayList<String>());
        data.put(_l2("NB"), new ArrayList<String>());
        data.put(_l2("TR"), new ArrayList<String>());
        data.put(_l2("CA"), new ArrayList<String>());

        Map<String, String> en = new HashMap<>();

        String line;

        int ln = 0;
        while ((line = br.readLine()) != null) {
            if (++ln < 57) {
                continue;
            }
            StringTokenizer st = new StringTokenizer(line);
            String trim = line.trim();
            if (trim.contains("# Translation missing")) {
                continue;
            }
            if (trim.isEmpty()) {
                continue;
            }
            if (trim.isEmpty() || trim.charAt(0) == '#') {
                for (String k : data.keySet()) {
                    //data.get(k).add(line);
                }
            }
            try {
                String key = st.nextToken(":");
                String lang = st.nextToken(":");
                String text = st.nextToken("\n").substring(1);
                if ("EN".equalsIgnoreCase(lang)) {
                    en.put(key, text);
                }
                if (data.get(_l2(lang)) == null) {
//                    System.out.println("!!! => " + lang + "  " + line);
                }
                data.get(_l2(lang)).add(key + " = " + text);


                int pos = 0;

                while ((pos = text.indexOf("$[", pos)) >= 0) {
                    int pos2 = text.indexOf("]", pos+1);
                    String variable = text.substring(pos+2, pos2);
                    String r = en.get(variable);
                    if (r == null) {
                        if ("prefs_dialog.misc_tab".equals(variable)) {
                            r = "Misc";
                        } else if ("prefs_dialog.show_hidden_files".equals(variable)) {
                            r = "Show hidden files";
                        } else if ("bookmarks_menu".equals(variable)) {
                            r = "Bookmarks";
                        } else if ("add_bookmark_dialog.location".equals(variable)) {
                            r = "Location";
                        }
                    }
System.out.println(text + "  " + r);
                    text = text.substring(0, pos) + r + text.substring(pos2+1, text.length());
                }


                //System.out.println(key + " = " + text);
            } catch (NoSuchElementException e) {
//                System.out.println("!!! " + line);
            }
        }

        for (String k : data.keySet()) {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream("/Users/trol/Projects/java/mucommander/res/runtime/out/dictionary_" + k.replace('-', '_') + ".properties"), "UTF-8"));
            List<String> ls = data.get(k);
            for (String s : ls) {
                pw.write(s);
                pw.write('\n');
            }
            pw.close();
        }

    }


    private static String _l2(String lang) {
        switch (lang) {
            // for backward compatibility
            case "EN":
                return "en-US";
            case "en_GB":
                return "en_GB";
            case "FR":
                return "fr-FR";
            case "DE":
                return "de-DE";
            case "ES":
                return "es-ES";
            case "CS":
                return "cs-CZ";
            case "zh_CN":
                return "zh-CN";
            case "ZH_CN":
                return "zh-CN";
            case "zh_TW":
                return "zh-TW";
            case "PL":
                return "pl-PL";
            case "HU":
                return "hu-HU";
            case "RU":
                return "ru-RU";
            case "SL":
                return "sl-SL";
            case "RO":
                return "ro-RO";
            case "IT":
                return "it-IT";
            case "KO":
                return "ko-KR";
            case "pt_BR":
                return "pt-BR";
            case "NL":
                return "nl-NL";
            case "SK":
                return "sk-SK";
            case "JA":
                return "ja-JP";
            case "SV":
                return "sv-SV";
            case "DA":
                return "da-DA";
            case "UA":
                return "uk-UA";
            case "AR":
                return "ar-SA";
            case "BE":
                return "be-BY";
            case "NB":
                return "no-NO";
            case "TR":
                return "tr-TR";
            case "CA":
                return "ca-ES";
            default:
                return "xxx";

        }
    }


    public static class UTF8Control extends ResourceBundle.Control {
        public ResourceBundle newBundle
                (String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
                throws IllegalAccessException, InstantiationException, IOException
        {
            // The below is a copy of the default implementation.
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            ResourceBundle bundle = null;
            InputStream stream = null;
            if (reload) {
                URL url = loader.getResource(resourceName);
                if (url != null) {
                    URLConnection connection = url.openConnection();
                    if (connection != null) {
                        connection.setUseCaches(false);
                        stream = connection.getInputStream();
                    }
                }
            } else {
                stream = loader.getResourceAsStream(resourceName);
            }
            if (stream != null) {
                try {
                    // Only this line is changed to make it to read properties files as UTF-8.
                    bundle = new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"));
                } finally {
                    stream.close();
                }
            }
            return bundle;
        }
    }
}
