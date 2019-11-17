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

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class MyFileUtilsTest {
	@Test
	public void readsLinesExactGivenCarriageReturnsOnly() throws IOException {
		String text = "line 1\nline 2\nline 3";
		
		assertThat(MyFileUtils.readLinesWithEndings(text),
				contains("line 1\n", "line 2\n", "line 3"));
	}

	@Test
	public void readsLinesExactGivenLinefeedsOnly() throws IOException {
		String text = "line 1\rline 2\rline 3";
		
		assertThat(MyFileUtils.readLinesWithEndings(text),
				contains("line 1\r", "line 2\r", "line 3"));
	}

	@Test
	public void readsLinesExactGivenWindowsLineEndings() throws IOException {
		String text = "line 1\r\nline 2\r\nline 3";
		
		assertThat(MyFileUtils.readLinesWithEndings(text),
				contains("line 1\r\n", "line 2\r\n", "line 3"));
	}

	@Test
	public void readsLinesExactGivenMixedEndings() throws IOException {
		String text = "line 1\r\nline 2\rline 3\n";
		
		assertThat(MyFileUtils.readLinesWithEndings(text),
				contains("line 1\r\n", "line 2\r", "line 3\n"));
	}
	
	@Test
	public void readsLinesExactGivenEmptyLines() throws IOException {
		assertThat(MyFileUtils.readLinesWithEndings("\nhello\n"), contains("\n", "hello\n"));
		assertThat(MyFileUtils.readLinesWithEndings("\rhello\r"), contains("\r", "hello\r"));
		assertThat(MyFileUtils.readLinesWithEndings("\r\nhello\r\n"), contains("\r\n", "hello\r\n"));
	}
}
