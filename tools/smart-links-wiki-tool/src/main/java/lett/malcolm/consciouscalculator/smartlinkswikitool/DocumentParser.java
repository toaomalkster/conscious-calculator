package lett.malcolm.consciouscalculator.smartlinkswikitool;

import java.io.File;
import java.util.List;

/**
 * Reads a markdown file, and extracts meta-data from it.
 * @author Malcolm Lett
 */
public class DocumentParser {
	private File root;
	private File file;
	
	/**
	 * @param root used to calculate relative paths of files
	 * @param file
	 */
	public DocumentParser(File root, File file) {
		this.root = root;
		this.file = file;
	}
	
	/**
	 * Reads the file for meta-data information.
	 * @return
	 */
	public DocumentInfo getMetadata() {
		String relativePath = root.toPath().relativize(file.toPath()).toString();
		
		// standardise on path separators
		relativePath = relativePath.replace(File.separator, "/");
		
		return new DocumentInfo(file, relativePath);
	}
	
	private List<String> readLines(File file) {
		//File
		return null;
	}
}
