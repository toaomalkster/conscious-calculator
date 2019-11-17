package lett.malcolm.consciouscalculator.smartlinkswikitool;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

/**
 * Currently can only handle one section per file.
 * @author Malcolm Lett
 */
public class LinksListDocumentUpdater {
	private DocumentInfo doc;
	private File targetFile;
	private List<DocumentInfo> knownDocuments;
	
	public LinksListDocumentUpdater(DocumentInfo doc, File targetFile, List<DocumentInfo> knownDocuments) {
		this.doc = doc;
		this.targetFile = targetFile;
		this.knownDocuments = knownDocuments;
	}
	
	public void executeUpdate() {
		String content = readContent(doc.getFile(), doc.getRelativePath());
		
		String toReplace = identifySectionToReplace(content);
		
		String replacement = generateNewListSection("work-in-progress", doc.getRelativePath());
		
		content = content.replace(toReplace, replacement);
		writeContent(targetFile, content);
	}
	
	private String readContent(File file, String displayPath) {
		try {
			return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new IllegalArgumentException("Error reading file "+displayPath+": "+e.getMessage(), e);
		}
	}

	private void writeContent(File file, String content) {
		try {
			FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new IllegalArgumentException("Error writing file "+file+": "+e.getMessage(), e);
		}
	}
	
	private String identifySectionToReplace(String content) {
		// TODO
		return "_(List: work-in-progress)_";
	}
	
	private String generateNewListSection(String label, String displayPath) {
		List<DocumentInfo> docs = findDocumentsByLabel(label);
		if (docs.isEmpty()) {
			System.out.println("["+displayPath+"] No documents found with label '"+label+"'");
		}
		
		boolean first = true;
		StringBuilder buf = new StringBuilder();
		for (DocumentInfo doc: docs) {
			if (first) {
				// blank line at start
				buf.append("\n");
			}

			buf.append("* [" + doc.getLinkPath() + "]");
			buf.append("\n");
			
			first = false;
		}
		return buf.toString();
	}
	
	/**
	 * Applies a standardised order too.
	 * And puts newest first.
	 * @param label
	 * @return
	 */
	private List<DocumentInfo> findDocumentsByLabel(String label) {
		return knownDocuments.stream()
			.filter(d -> containsIgnoreCase(d.getLabels(), label))
			.sorted(createdThenAlphabetical().reversed())
			.collect(Collectors.toList());
	}
	
	private Comparator<DocumentInfo> createdThenAlphabetical() {
		return Comparator.nullsLast(Comparator.comparing((DocumentInfo d) -> d.getCreatedDate()))
				.thenComparing((DocumentInfo d) -> d.getFile().getName());
	}
	
	private boolean containsIgnoreCase(Collection<String> c, String ref) {
		for (String item: c) {
			if (item.equalsIgnoreCase(ref)) {
				return true;
			}
		}
		return false;
	}
}
