package lett.malcolm.consciouscalculator.smartlinkswikitool;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class DocumentInfo {
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private File file;
	private Set<String> labels = new HashSet<>();
	private LocalDate createdDate;

	// path relative to the search root, used for display
	private String relativePath;
	
	public DocumentInfo(File file, String relativePath) {
		this.file = file;
		this.relativePath = relativePath;
	}
	
	public DocumentInfo(File file, String relativePath, Set<String> labels, LocalDate createdDate) {
		this.file = file;
		this.relativePath = relativePath;
		this.labels = labels;
		this.createdDate = createdDate;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Document{");
		builder.append(getRelativePath());

//		if (getLinkPath() != null) {
//			builder.append(",");
//			builder.append("link=\"").append(getLinkPath()).append("\"");
//		}
		
		if (createdDate != null) {
			builder.append(",");
			builder.append("created ").append(getCreatedDateForDisplay());
		}
		
		if (!labels.isEmpty()) {
			builder.append(",");
			builder.append("labels: ");
			builder.append(labels.stream().collect(Collectors.joining(",")));
		}
		builder.append("}");
		return builder.toString();
	}

	public File getFile() {
		return file;
	}
	
	public void setFile(File file) {
		this.file = file;
	}
	
	public String getRelativePath() {
		return relativePath;
	}
	
	public String getLinkPath() {
		// TODO
		return file.getName();
	}
	
	public Set<String> getLabels() {
		return labels;
	}
	
	public void setLabels(Collection<String> labels) {
		if (labels == null) {
			labels = Collections.emptySet();
		}
		this.labels = new HashSet<>(labels);
	}
	
	public LocalDate getCreatedDate() {
		return createdDate;
	}
	
	public void setCreatedDate(LocalDate createdDate) {
		this.createdDate = createdDate;
	}
	
	public String getCreatedDateForDisplay() {
		if (createdDate == null) {
			return null;
		}
		
		return createdDate.format(FORMATTER);
	}
	
	
}
