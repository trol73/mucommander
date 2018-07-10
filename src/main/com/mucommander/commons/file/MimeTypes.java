package com.mucommander.commons.file;

import com.mucommander.commons.file.util.ResourceLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * This Hashtable maps file extensions to their mime type.
 *
 * @author Maxence Bernard
 */
public class MimeTypes extends Hashtable<String, String> {

	private final static MimeTypes MIME_TYPES = new MimeTypes();

	/** Name of the 'mime.types' resource file located in the same package as this class */
	private static final String MIME_TYPES_RESOURCE_NAME = "mime.types";

	private MimeTypes() {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(ResourceLoader.getPackageResourceAsStream(MimeTypes.class.getPackage(), MIME_TYPES_RESOURCE_NAME)))) {
			String line;
			while ((line = br.readLine()) != null) {
				try {
					StringTokenizer st = new StringTokenizer(line);
					String description = st.nextToken();

					while (st.hasMoreTokens())
						put(st.nextToken(), description);
				} catch (Exception e) {
					// If a line contains an error, catch the exception and go to the next line
				}
			}
		} catch (IOException ignore) {
		}
	}


	/**
	 * Returns the MIME type of the given file (determined by the file extension), <code>null</code>
	 * if the type is unknown (unknown or no extension) or if the file is a folder.
	 *
	 * @param file the given file
	 *
	 * @return the MIME type
	 */
	public static String getMimeType(AbstractFile file) {
		if (file.isDirectory()) {
			return null;
		}

		String name = file.getName();
		int pos = name.lastIndexOf('.');
		if (pos < 0) {
			return null;
		}

		return MIME_TYPES.get(name.substring(pos+1, name.length()).toLowerCase());
	}

}