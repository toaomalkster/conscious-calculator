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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Reads a markdown file, and extracts meta-data from it.
 * @author Malcolm Lett
 */
public class DocumentParser {
	static final int MAX_METADATA_READ_LINES = 10;
	static final Pattern METADATA_LINE_PATTERN = Pattern.compile("\\(([^)]*)\\)");
	static final Pattern METADATA_SECTION_SEPARATOR = Pattern.compile("[.;]");
	static final Pattern ADDED_PATTERN = Pattern.compile("Added[:]? ([0-9-\\\\/]+)", Pattern.CASE_INSENSITIVE);
	static final Pattern LABELS_PATTERN = Pattern.compile("Labels[:]? (.+)", Pattern.CASE_INSENSITIVE);
	static final Pattern LIST_PATTERN = Pattern.compile("List[:]? (.+)", Pattern.CASE_INSENSITIVE);
	static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	
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
		String path = MyFileUtils.relativePathOf(file, root);
		List<String> lines = readLines(file, path);
		
		DocumentInfo doc = new DocumentInfo(file, path);
		readMetadata(lines, doc);
		
		// also check for macros
		if (lines.stream().anyMatch(l -> isListMacroLine(l))) {
			doc.setHasListMacro(true);
		}
		return doc;
	}

	public boolean isListMacroLine(String line) {
		return getListMacroLabel(line) != null;
	}
	
	/**
	 * Gets the label from the list macro on the given line, if present.
	 * @param line
	 * @return null if not a list-macro line, or label cannot be determined
	 */
	public String getListMacroLabel(String line) {
		// find and parse 'list' macro
		for (String section: extractMetadataSections(line)) {
			Matcher listMatcher = LIST_PATTERN.matcher(section);
			if (listMatcher.matches()) {
				return listMatcher.group(1);
			}
		}
		
		return null;
	}
	
	private void readMetadata(List<String> fileLines, DocumentInfo doc) {
		// reverse order for easier logic
		Collections.reverse(fileLines);
		
		// examine meta-data lines within bottom few lines
		int count = 0;
		for (String line: fileLines) {
			if (++count > MAX_METADATA_READ_LINES) {
				break;
			}
			
			// extract sections if line suitable
			String addedDateText = null;
			String labelsText = null;
			for (String section: extractMetadataSections(line)) {
				// extract added date
				Matcher addedMatcher = ADDED_PATTERN.matcher(section);
				if (addedMatcher.matches()) {
					addedDateText = addedMatcher.group(1);
				}
				
				// extract labels
				Matcher labelsMatcher = LABELS_PATTERN.matcher(section);
				if (labelsMatcher.matches()) {
					labelsText = labelsMatcher.group(1);
				}
			}
			
			// parse added date
			if (addedDateText != null) {
				try {
					LocalDate date = DATE_FORMATTER.parse(addedDateText, LocalDate::from);
					doc.setCreatedDate(date);
				} catch (DateTimeParseException e) {
					System.out.println("["+doc.getRelativePath()+"]: Unable to parse Added date '"+addedDateText+"'");
				}
			}
			
			// parse labels
			if (labelsText != null) {
				List<String> labels = Arrays.stream(labelsText.split(","))
					.map(StringUtils::trimToNull)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
				doc.setLabels(labels);
			}
		}
	}
	
	private List<String> extractMetadataSections(String line) {
		// check if line is suitable
		// form: "...(metadata)..."
		Matcher lineMatcher = METADATA_LINE_PATTERN.matcher(line);
		if (lineMatcher.find()) {
			String metadataText = lineMatcher.group(1);
			
			// extract sections
			// form: "xxxx. xxxx. xxxx."
			// form: "xxxx; xxxx; xxxx"
			String[] sections = METADATA_SECTION_SEPARATOR.split(metadataText);
			return Arrays.stream(sections)
				.map(StringUtils::trimToNull)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		}
		
		return Collections.emptyList();
	}

	private List<String> readLines(File file, String displayPath) {
		try {
			return FileUtils.readLines(file, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new IllegalArgumentException("Error reading file "+displayPath+": "+e.getMessage(), e);
		}
	}
}
