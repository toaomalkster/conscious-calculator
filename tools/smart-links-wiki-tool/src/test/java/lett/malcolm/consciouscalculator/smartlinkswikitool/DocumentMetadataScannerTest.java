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
