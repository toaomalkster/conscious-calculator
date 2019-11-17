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

import java.util.regex.Matcher;

import org.junit.jupiter.api.Test;

public class DocumentParserTest {
	@Test
	public void metadataLineRegexWorksGivenGoodMetadataLine() {
		Matcher m = DocumentParser.METADATA_LINE_PATTERN.matcher("_(Added 2019-11-17. Labels: work-in-progress)_");
		
		assertThat(m.find(), is(true));
		assertThat(m.group(1), is("Added 2019-11-17. Labels: work-in-progress"));
	}

	@Test
	public void metadataLineRegexWorksGivenMetadataLineWithOtherThingsAfter() {
		Matcher m = DocumentParser.METADATA_LINE_PATTERN.matcher("_(Added 2019-11-17. Labels: work-in-progress)_(something else)");
		
		assertThat(m.find(), is(true));
		assertThat(m.group(1), is("Added 2019-11-17. Labels: work-in-progress"));
	}
	
	@Test
	public void addedDateRegexWorksGivenGoodText() {
		Matcher m = DocumentParser.ADDED_PATTERN.matcher("Added 2019-11-17. Labels: work-in-progress");
		
		assertThat(m.find(), is(true));
		assertThat(m.group(1), is("2019-11-17"));
	}

	@Test
	public void addedDateRegexWorksGivenGoodText2() {
		Matcher m = DocumentParser.ADDED_PATTERN.matcher("Labels: work-in-progress. Added 2019-11-17");
		
		assertThat(m.find(), is(true));
		assertThat(m.group(1), is("2019-11-17"));
	}
	
	@Test
	public void labelsRegexWorksGivenGoodText() {
		Matcher m = DocumentParser.LABELS_PATTERN.matcher("Added 2019-11-17. Labels: work-in-progress,philosophy, and others");
		
		assertThat(m.find(), is(true));
		assertThat(m.group(1), is("work-in-progress,philosophy, and others"));
	}

	@Test
	public void labelsRegexWorksGivenGoodText2() {
		Matcher m = DocumentParser.LABELS_PATTERN.matcher("Labels: work-in-progress,philosophy, and others. Added 2019-11-17");
		
		assertThat(m.find(), is(true));
		assertThat(m.group(1), is("work-in-progress,philosophy, and others"));
	}
}
