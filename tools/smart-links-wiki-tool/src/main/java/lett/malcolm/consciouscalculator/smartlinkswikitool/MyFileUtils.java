package lett.malcolm.consciouscalculator.smartlinkswikitool;

import java.io.File;

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

}
