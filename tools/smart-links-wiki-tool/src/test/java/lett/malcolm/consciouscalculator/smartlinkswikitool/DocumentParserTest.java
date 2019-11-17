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
