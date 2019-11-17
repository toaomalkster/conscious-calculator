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
