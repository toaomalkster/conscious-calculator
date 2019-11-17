package lett.malcolm.consciouscalculator.smartlinkswikitool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public abstract class MyFileUtils {
	/**
	 * Constructs the relative path of the file, relative to root.
	 * With standardised folder separators (unix-style).
	 * @param file
	 * @return
	 */
	public static String relativePathOf(File file, File root) {
		String relativePath = root.toPath().relativize(file.toPath()).toString();
		
		// standardise on path separators
		return relativePath.replace(File.separator, "/");
	}
	
	/**
	 * Reads all file lines, including line endings.
	 * Useful for doing manipulations on a file without loosing its particular line ending encodings.s
	 * @param file
	 * @return
	 * @throws IOException on any I/O error
	 */
	public static List<String> readLinesWithEndings(File file) throws IOException {
		return readLinesWithEndings(new FileReader(file));
	}

	/**
	 * Reads all file lines, including line endings.
	 * Useful for doing manipulations on a file without loosing its particular line ending encodings.s
	 * @param file
	 * @return
	 * @throws IOException on any I/O error
	 */
	public static List<String> readLinesWithEndings(String text) throws IOException {
		return readLinesWithEndings(new StringReader(text));
	}
	
	public static String getLineEnding(String lineWithLineEnding) {
		if (lineWithLineEnding.endsWith("\r\n")) {
			return "\r\n";
		}
		if (lineWithLineEnding.endsWith("\n")) {
			return "\n";
		}
		if (lineWithLineEnding.endsWith("\r")) {
			return "\r";
		}
		throw new IllegalArgumentException("No line ending");
	}
	
	/**
	 * Removes any line ending, if present
	 * @param lineWithLineEnding
	 * @return
	 */
	public static String trimLineEnding(String lineWithLineEnding) {
		int len = lineWithLineEnding.length();
		if (lineWithLineEnding.endsWith("\r\n")) {
			return lineWithLineEnding.substring(0, len - 2);
		}
		else if (lineWithLineEnding.endsWith("\n") || lineWithLineEnding.endsWith("\r")) {
			return lineWithLineEnding.substring(0, len - 1);
		}
		else {
			// no line ending already
			return lineWithLineEnding;
		}
	}

	/**
	 * Reads all file lines, including line endings.
	 * Useful for doing manipulations on a file without loosing its particular line ending encodings.s
	 * @param file
	 * @return
	 * @throws IOException on any I/O error
	 */
	private static List<String> readLinesWithEndings(Reader textReader) throws IOException {
		List<String> lines = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(textReader)) {
			String line;
			while ((line = readLineExact(reader)) != null) {
				lines.add(line);
			}
		}
		return lines;
	}
	
	/**
     * Reads a line of text.  A line is considered to be terminated by any one
     * of a line feed ('\n'), a carriage return ('\r'), or a carriage return
     * followed immediately by a linefeed.
     * The terminators are always included.
     * 
     * Empty lines are returned as-is.
	 * @return null if at EOF
	 */
	static String readLineExact(BufferedReader reader) throws IOException {
		StringBuilder buf = new StringBuilder();
		
		int ch, prev = -1;
		while ((ch = reader.read()) != -1) {
			if (ch == '\n') {
				// line completed
				buf.append((char) ch);
				break;
			}
			else if (ch != '\n' && ch != '\r' && prev == '\r') {
				// this is the first character in the next line:
				// - reset back by one character
				// - return what we've got
				reader.reset();
				break;
			}
			buf.append((char) ch);
			reader.mark(2);
			prev = ch;
		}
		
		if (buf.length() == 0) {
			// EOF
			return null;
		}
		return buf.toString();
	}

}
