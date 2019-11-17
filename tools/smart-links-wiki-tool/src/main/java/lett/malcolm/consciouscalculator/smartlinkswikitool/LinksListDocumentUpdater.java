/**
 * Conscious Calculator - Emulation of a conscious calculator.
 * Copyright © 2019 Malcolm Lett (malcolm.lett at gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package lett.malcolm.consciouscalculator.smartlinkswikitool;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

/**
 * Currently can only handle one section per file.
 * @author Malcolm Lett
 */
public class LinksListDocumentUpdater {
	static final Pattern LIST_PATTERN = Pattern.compile("\\* \\[.\\](\\(.\\))", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	
	private DocumentInfo doc;
	private File targetFile;
	private List<DocumentInfo> knownDocuments;
	
	public LinksListDocumentUpdater(DocumentInfo doc, File targetFile, List<DocumentInfo> knownDocuments) {
		this.doc = doc;
		this.targetFile = targetFile;
		this.knownDocuments = knownDocuments;
	}
	
	public boolean executeUpdate() {
		DocumentParser parser = new DocumentParser(null, doc.getFile());
		
		List<String> contentWithLineEndings = readContent(doc.getFile(), doc.getRelativePath());
		
		ListRequest toReplace = identifySectionToReplace(contentWithLineEndings, parser);
		
		List<String> replacementWithoutLineEndings = generateNewListSection("work-in-progress", doc.getRelativePath());
		
		contentWithLineEndings = replaceSection(contentWithLineEndings, toReplace, replacementWithoutLineEndings);
		writeContent(targetFile, contentWithLineEndings);
		
		return true;
	}
	
	/**
	 * 
	 * @param file
	 * @param displayPath
	 * @return exact lines, with line endings
	 */
	private List<String> readContent(File file, String displayPath) {
		try {
			return MyFileUtils.readLinesWithEndings(file);
		} catch (IOException e) {
			throw new IllegalArgumentException("Error reading file "+displayPath+": "+e.getMessage(), e);
		}
	}

	/**
	 * 
	 * @param file
	 * @param content lines, with line endings
	 */
	private void writeContent(File file, List<String> content) {
		try {
			StringBuilder buf = new StringBuilder();
			for (String line: content) {
				buf.append(line);
			}
			FileUtils.writeStringToFile(file, buf.toString(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new IllegalArgumentException("Error writing file "+file+": "+e.getMessage(), e);
		}
	}
	
	/**
	 * 
	 * @param content
	 * @param parser
	 * @return null if nothing
	 */
	private ListRequest identifySectionToReplace(List<String> content, DocumentParser parser) {
		ListRequest request = new ListRequest();
		
		boolean found = false;
		int lineIdx = 0;
		for (String line: content) {
			String lineWithoutEnding = MyFileUtils.trimLineEnding(line);
			
			if (!found && parser.isListMacroLine(line)) {
				request.setStartLineIdx(lineIdx + 1);
				request.setLabel(parser.getListMacroLabel(line));
				found = true;
			}
			else if (found && !line.startsWith("*") && !lineWithoutEnding.isEmpty()) {
				// stop BEFORE first non-empty line that doesn't start with '*' --- these lines must be kept
				break;
			}
			else if (found && !line.startsWith("*") && lineWithoutEnding.isEmpty() && lineIdx - request.getStartLineIdx() > 1) {
				// stop AFTER first empty line after '*' --- replace this last blank line
				request.setEndLineIdx(lineIdx + 1);
				break;
			}
			request.setEndLineIdx(lineIdx + 1);
			
			lineIdx++;
		}

		// final sanity checks
		if (request.getLabel() != null) {
			return request;
		}
		return null;
	}

	/**
	 * 
	 * @param label
	 * @param displayPath
	 * @return lines, without line endings
	 */
	private List<String> generateNewListSection(String label, String displayPath) {
		List<DocumentInfo> docs = findDocumentsByLabel(label);
		if (docs.isEmpty()) {
			System.out.println("["+displayPath+"] No documents found with label '"+label+"'");
		}
		
		List<String> result = new ArrayList<>();

		// blank line at start
		result.add("");

		// list
		for (DocumentInfo doc: docs) {
			result.add("* " + doc.getWikiLink());
		}
		
		// blank line at end
		result.add("");
		
		return result;
	}
	
	/**
	 * Usually this will REPLACE.
	 * But when toReplace.endLineIdx == toReplace.startLineIdx, then it INSERTS instead.
	 * @param content original content lines, with line endings
	 * @param toReplace
	 * @param newSection lines, without line endings
	 * @return new full content, after updates
	 */
	private List<String> replaceSection(List<String> content, ListRequest toReplace, List<String> newSection) {
		List<String> result = new ArrayList<>();
		for (int idx = 0; idx < content.size(); idx++) {
			if (idx < toReplace.getStartLineIdx() || idx >= toReplace.getEndLineIdx()) {
				result.add(content.get(idx));
			}
			
			// add BEFORE next line, in case we are on the very last line already
			if ((idx+1) == toReplace.getStartLineIdx()) {
				String lineTerminator;
				try {
					lineTerminator = MyFileUtils.getLineEnding(content.get(idx));
				} catch (IllegalArgumentException e) {
					// fallback: current line doesn't have a line-ending (it's probably the last line), so:
					// - determine line terminator from previous line
					// - add retrospectively add line terminator to current line
					//   (can just add to the list, because it all gets concatenated)
					lineTerminator = MyFileUtils.getLineEnding(content.get(idx-1));
					result.add(lineTerminator);
				}
				
				for (String newSectionLine: newSection) {
					result.add(newSectionLine + lineTerminator);
				}
			}
		}
		return result;
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
	
	private static class ListRequest {
		private Integer startLineIdx;
		private Integer endLineIdx; // INSERT if equal to startLineIdx, otherwise REPLACE
		private String label;
		
		public Integer getStartLineIdx() {
			return startLineIdx;
		}

		public void setStartLineIdx(Integer startLineIdx) {
			this.startLineIdx = startLineIdx;
		}

		public Integer getEndLineIdx() {
			return endLineIdx;
		}

		public void setEndLineIdx(Integer endLineIdx) {
			this.endLineIdx = endLineIdx;
		}

		public String getLabel() {
			return label;
		}
		
		public void setLabel(String label) {
			this.label = label;
		}
	}
}
