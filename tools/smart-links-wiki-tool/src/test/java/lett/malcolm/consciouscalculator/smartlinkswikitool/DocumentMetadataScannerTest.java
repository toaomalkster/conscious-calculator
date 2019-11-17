package lett.malcolm.consciouscalculator.smartlinkswikitool;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

public class DocumentMetadataScannerTest {
	private static final String SAMPLE_FILE_CLASSPATH_PATH = "/sample-files/Article1.md";
	
	@Test
	public void findsFilesRecursively() {
		DocumentMetadataScanner scanner = new DocumentMetadataScanner(sampleFilesRoot());
		Set<String> paths = scanner.findDocuments().stream().map(d -> d.getRelativePath()).collect(Collectors.toSet());
		
		assertThat(paths, containsInAnyOrder("Article1.md", "Article2.md", "ListPage.md", "folder/Article3.md"));
	}
	
	/**
	 * Works out the filesystem root.
	 * @return
	 */
	private File sampleFilesRoot() {
		try {
			URL url = getClass().getResource(SAMPLE_FILE_CLASSPATH_PATH);
			if (url == null) {
				throw new IllegalArgumentException("Classpath file not found");
			}
			File sampleFile = new File(url.toURI());
			return sampleFile.getParentFile().getCanonicalFile();
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
}
