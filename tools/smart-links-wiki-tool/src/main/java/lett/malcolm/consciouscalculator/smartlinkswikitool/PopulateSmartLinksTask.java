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
import java.util.List;
import java.util.stream.Collectors;

public class PopulateSmartLinksTask {
	private File root;
	private File target;
	
	public PopulateSmartLinksTask(File root, File target) {
		this.root = root;
		this.target = target;
		
		// if no target, overwrite in-place
		if (target == null) {
			this.target = root;
		}
	}

	public void run() {
		DocumentMetadataScanner scanner = new DocumentMetadataScanner(root);
		List<DocumentInfo> docs = scanner.findDocuments();
		System.out.println("Found " + docs.size() + " markdown files");
		System.out.println(docs);
		
		// filter
		List<DocumentInfo> docsToUpdate = docs.stream().filter(d -> d.isHasListMacro()).collect(Collectors.toList());
		
		// process
		for (DocumentInfo doc: docsToUpdate) {
			File targetFile = new File(target, doc.getRelativePath());
			if (new LinksListDocumentUpdater(doc, targetFile, docs).executeUpdate()) {
				System.out.println("["+doc.getRelativePath()+"] Updated links");
			}
		}
	}
}
