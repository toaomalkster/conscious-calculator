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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class DocumentInfo {
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	
	private final File file;
	private Set<String> labels = new HashSet<>();
	private LocalDate createdDate;
	private boolean hasListMacro;

	// path relative to the search root, used for display
	private final String relativePath;
	
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
			builder.append("labels:");
			builder.append(labels.stream().collect(Collectors.joining(",")));
		}
		
		if (hasListMacro) {
			builder.append(",");
			builder.append("macros:list");
		}
		builder.append("}");
		return builder.toString();
	}

	public File getFile() {
		return file;
	}
	
	public String getRelativePath() {
		return relativePath;
	}
	
	/**
	 * GitHub Wiki handles relative paths behind the scenes, so I only have to provide the page name.
	 * 
	 * This also turns the filename into a page name:
	 * - Long-Term-Memory-Searches.md => [[Long Term Memory Searches]]
	 * @return
	 */
	public String getWikiLink() {
		String fileName = file.getName();
		
		String pageName = fileName.substring(0, fileName.length() - ".md".length());
		pageName = pageName.replace("-", " ");
		
		return "[[" + pageName + "]]";
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

	public boolean isHasListMacro() {
		return hasListMacro;
	}

	public void setHasListMacro(boolean hasListMacro) {
		this.hasListMacro = hasListMacro;
	}

	public static DateTimeFormatter getFormatter() {
		return FORMATTER;
	}
	
	
}
