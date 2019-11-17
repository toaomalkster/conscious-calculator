package lett.malcolm.consciouscalculator.smartlinkswikitool;

import java.io.File;

/**
 * Reads a markdown file, and extracts meta-data from it.
 * @author Malcolm Lett
 */
public class DocumentParser {
	private File file;
	
	public DocumentParser(File file) {
		this.file = file;
	}
	
	/**
	 * Reads the file for meta-data information.
	 * @return
	 */
	public DocumentInfo getMetadata() {
		return new DocumentInfo(file);
	}
}
