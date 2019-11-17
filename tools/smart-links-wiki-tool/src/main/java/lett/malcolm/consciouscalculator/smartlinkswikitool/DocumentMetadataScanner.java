package lett.malcolm.consciouscalculator.smartlinkswikitool;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Recursively scans a folder for markdown documents and extracts their meta-data.
 * @author Malcolm Lett
 */
public class DocumentMetadataScanner {
	private File root;

	public DocumentMetadataScanner(File root) {
		this.root = root;
	}
	
	public List<DocumentInfo> findDocuments() {
		List<DocumentInfo> documents = new ArrayList<>();
		addDocuments(root, documents);
		
		// apply consistent ordering
		documents.sort(Comparator.comparing((DocumentInfo d) -> d.getFile().getName()));
		
		return documents;
	}
	
	private void addDocuments(File path, List<DocumentInfo> documents) {
		for (File file: path.listFiles(isMarkdownFile())) {
			documents.add(documentInfoOf(file));
		}
		
		for (File subdir: path.listFiles(isDirectory())) {
			addDocuments(subdir, documents);
		}
	}
	
	private DocumentInfo documentInfoOf(File file) {
		return new DocumentParser(root, file).getMetadata();
	}
	
	private FileFilter isMarkdownFile() {
		return new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile() && file.getName().endsWith(".md");
			}
		};
	}
	
	private FileFilter isDirectory() {
		return new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		};
	}
}
