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
