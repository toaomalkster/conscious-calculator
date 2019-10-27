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
package lett.malcolm.consciouscalculator.emulator;

import static lett.malcolm.consciouscalculator.testutils.MyHamcrest.hasItem;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.time.Clock;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import lett.malcolm.consciouscalculator.emulator.events.PerceptEvent;
import lett.malcolm.consciouscalculator.emulator.facts.ExpressionFact;
import lett.malcolm.consciouscalculator.emulator.facts.NameFact;
import lett.malcolm.consciouscalculator.emulator.facts.NumberFact;
import lett.malcolm.consciouscalculator.emulator.facts.OperatorFact;
import lett.malcolm.consciouscalculator.emulator.interfaces.Event;
import lett.malcolm.consciouscalculator.emulator.interfaces.Percept;

public class LongTermMemoryTests {
	private LongTermMemory memory;
	private Clock clock = Clock.systemDefaultZone();
	
	@Before
	public void setup() {
		memory = new LongTermMemory(clock, 10000);
	}
	
	@Test
	public void constructedWithPreprogrammedFacts() {
		assertThat(memory.get(NameFact.GUID), is(not(nullValue())));
		assertThat(memory.get(NumberFact.GUID), is(not(nullValue())));
		assertThat(memory.get(OperatorFact.GUID), is(not(nullValue())));
		assertThat(memory.get(ExpressionFact.GUID), is(not(nullValue())));
		
		assertThat(memory.get(NameFact.GUID), instanceOf(PerceptEvent.class));
		assertThat(memory.get(NameFact.GUID).guid(), is("NameFact"));
		assertThat(memory.get(NameFact.GUID).data(), instanceOf(Percept.class));
		assertThat(memory.get(NameFact.GUID).references(), is(empty()));
		assertThat(((Percept) memory.get(NameFact.GUID).data()).guid(), is("NameFact"));
		assertThat(((Percept) memory.get(NameFact.GUID).data()).data(), is(nullValue()));
		assertThat(((Percept) memory.get(NameFact.GUID).data()).references(), is(empty()));

		assertThat(memory.get(NumberFact.GUID), instanceOf(PerceptEvent.class));
		assertThat(memory.get(NumberFact.GUID).guid(), is("NumberFact"));
		assertThat(memory.get(NumberFact.GUID).data(), instanceOf(Percept.class));
		assertThat(memory.get(NumberFact.GUID).references(), is(empty()));
		assertThat(((Percept) memory.get(NumberFact.GUID).data()).guid(), is("NumberFact"));
		assertThat(((Percept) memory.get(NumberFact.GUID).data()).data(), is(nullValue()));
		assertThat(((Percept) memory.get(NumberFact.GUID).data()).references(), is(setOf("NumberFact.Name")));
	}

	// found: [PerceptEvent{NumberFact,20ms,0.000,Percept{NumberFact,ref=NumberFact.Name,null}}]
	@Test
	public void findsNumberFactByEvent() {
		Event refEvent = new PerceptEvent(clock, new Percept(NumberFact.GUID, 3));
		
		List<Event> found = memory.search(refEvent);
		//System.out.println(found);
		
		Event number = found.stream().filter(e -> e.guid().equals(NumberFact.GUID)).findFirst().get();
		Percept percept = (Percept) number.data();
		assertThat(number, instanceOf(PerceptEvent.class));
		assertThat(number.guid(), is("NumberFact"));
		assertThat(number.data(), instanceOf(Percept.class));
		assertThat(number.references(), is(empty()));
		assertThat(percept.guid(), is("NumberFact"));
		assertThat(percept.data(), is(nullValue()));
		assertThat(percept.references(), is(setOf("NumberFact.Name")));
	}

	@Test
	public void findsExpressionAndOperatorAndNumberFactsByEvent() {
		Event refEvent = new PerceptEvent(clock, new Percept(ExpressionFact.GUID, listOf(
				new Percept(NumberFact.GUID, 3),
				new Percept(OperatorFact.GUID, "+"),
				new Percept(NumberFact.GUID, 5))));
		
		List<Event> found = memory.search(refEvent);
//		System.out.println("Found " + found.size() + ":");
//		found.forEach(e -> System.out.println("   "+e));

		assertThat(found, hasItem(e -> e.guid().equals(NumberFact.GUID)));
		assertThat(found, hasItem(e -> e.guid().equals(OperatorFact.GUID)));
		assertThat(found, hasItem(e -> e.guid().equals(ExpressionFact.GUID)));
	}
	
	@Test
	public void findsOnlyDirectConceptMatchesByEvent() {
		Event refEvent = new PerceptEvent(clock, new Percept(ExpressionFact.GUID, listOf(
				new Percept(NumberFact.GUID, 3),
				new Percept(OperatorFact.GUID, "+"),
				new Percept(NumberFact.GUID, 5))));
		
		List<Event> found = memory.search(refEvent);
		
		found = found.stream()
			.filter(e -> !e.guid().equals(NumberFact.GUID) &&
					!e.guid().equals(OperatorFact.GUID) &&
					!e.guid().equals(ExpressionFact.GUID))
			.collect(Collectors.toList());
		
		assertThat("Expected nothing other than Number/Operator/Expression facts", found, is(empty()));
	}

	@SuppressWarnings("unchecked")
	private static <T> List<T> listOf(T... values) {
		return Arrays.asList(values);
	}

	@SuppressWarnings("unchecked")
	private static <T> Set<T> setOf(T... values) {
		return new HashSet<>(Arrays.asList(values));
	}
}
