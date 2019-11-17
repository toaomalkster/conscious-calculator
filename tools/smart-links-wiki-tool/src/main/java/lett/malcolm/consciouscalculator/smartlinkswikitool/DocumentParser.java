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
	static final Pattern ADDED_PATTERN = Pattern.compile("Added[:]? ([0-9-\\\\/]+)");
	static final Pattern LABELS_PATTERN = Pattern.compile("Labels[:]? ([^.;]+)($|.|;)");
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
		return doc;
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
			
			// check if line suitable
			String metadataText = null;
			Matcher lineMatcher = METADATA_LINE_PATTERN.matcher(line);
			if (lineMatcher.find()) {
				metadataText = lineMatcher.group(1);
			}
			
			// extract added date
			String addedDateText = null;
			if (metadataText != null) {
				Matcher addedMatcher = ADDED_PATTERN.matcher(metadataText);
				if (addedMatcher.find()) {
					addedDateText = addedMatcher.group(1);
				}
			}
			
			// extract labels
			String labelsText = null;
			if (metadataText != null) {
				Matcher labelsMatcher = LABELS_PATTERN.matcher(metadataText);
				if (labelsMatcher.find()) {
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

	private List<String> readLines(File file, String displayPath) {
		try {
			return FileUtils.readLines(file, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new IllegalArgumentException("Error reading file "+displayPath+": "+e.getMessage(), e);
		}
	}
}
