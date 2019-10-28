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
package lett.malcolm.consciouscalculator.emulator.interfaces;


import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;

import lett.malcolm.consciouscalculator.emulator.events.DataRules;
import lett.malcolm.consciouscalculator.utils.Events;

/**
 * Represents interpreted data with attached meaning.
 * 
 * Immutable, and permitted by Data Rules.
 * Implements equals() and hashCode() according to the needs of {@link DataRules#isSame(Object, Object)}.
 * 
 * @author Malcolm Lett
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Percept {
	private final String guid;
	private final Set<String> references;
	private final Object data;

	/**
	 * Construct new instance with custom identifier.
	 * 
	 * If this represents the raw concept itself, then {@code guid} is the unique concept identifier;
	 * {@code references} is one or more underlying or related concepts; and
	 * {@code data} is probably {@code null}.
	 * 
	 * In some cases, a percept represents a learned 'instance'.
	 * eg: maybe, that 'v' is always the number 5. There it can store a reference to the NumberFact percept,
	 * and data = 5.
	 * 
	 * @param guid identifier of this percept
	 * @param references guids of underlying or related concepts
	 * @param data
	 */
	@JsonCreator
	public Percept(String guid, Set<String> references, Object data) {
		DataRules.assertValid(data);
		if (references == null) {
			references = new HashSet<>();
		}
		
		this.guid = guid;
		this.references = Collections.unmodifiableSet(references);
		this.data = data;
	}

	/**
	 * Construct new instance referencing a single source concept or fact, by guid.
	 * 
	 * If this represents an 'instance' of a concept 'class', then {@code data} should
	 * hold the instance value, and {@code reference} should refer to the GUID of the original
	 * concept 'class'. eg: {@code NumberFact.GUID}.
	 * 
	 * @param reference guid reference of underlying concept or fact
	 * @param data
	 */
	public Percept(String reference, Object data) {
		this(UUID.randomUUID().toString(), Collections.singleton(reference), data);
	}

	/**
	 * Deep exact clone.
	 */
	public Percept clone() {
		return new Percept(guid,
				new HashSet<>(this.references),
				DataRules.clone(this.data));
	}

	/**
	 * Deep clone, with newly generated GUID.
	 */
	public Percept cloneAsNew() {
		return new Percept(UUID.randomUUID().toString(),
				new HashSet<>(this.references),
				DataRules.clone(this.data));
	}
	
	@Override
	public String toString() {
		if (guid.contains("-") && references.size() == 1 && !references.iterator().next().contains("-")) {
			// must be normal guid instance, with one reference to a fact
			return toCompactPerceptInstanceString();
		}
		else {
			return toLongString();
		}
	}
	
	/**
	 * eg: <code>Percept{b405f,ref=NumberFact,3}</code>
	 * eg: <code>Percept{34f98,ref=ExpressionFact,[Percept{b405f,ref=NumberFact,3},Percept{1a667,ref=OperatorFact,+},Percept{5b503,ref=NumberFact,5}]}</code>
	 * @return
	 */
	private String toLongString() {
		StringBuilder buf = new StringBuilder();
		buf.append(getClass().getSimpleName()).append("{");
		
		// guid
		buf.append(Events.toShortGuid(guid())).append(",");
		
		// references
		for (String reference: references) {
			buf.append("ref=").append(Events.toShortGuid(reference)).append(",");
		}
		
		// content
		buf.append(DataRules.stringOf(data));
		
		buf.append("}");
		return buf.toString();
	}

	/**
	 * Produces:
	 * eg: <code>Number#b405f(3)</code>
	 * eg: <code>Expression#34f98[Number#b405f(3) Operator#1a667(+) Number#5b503(5)]</code>

	 * Given:
	 * eg: <code>Percept{b405f,ref=NumberFact,3}</code>
	 * eg: <code>Percept{34f98,ref=ExpressionFact,[Percept{b405f,ref=NumberFact,3},Percept{1a667,ref=OperatorFact,+},Percept{5b503,ref=NumberFact,5}]}</code>
	 * @return
	 */
	private String toCompactPerceptInstanceString() {
		String ref = references.iterator().next();
		if (ref.endsWith("Fact")) {
			ref = ref.substring(0, ref.length() - "Fact".length());
		}
		
		StringBuilder buf = new StringBuilder();
		buf.append(ref);
		buf.append("#").append(Events.toShortGuid(guid()));
		
		if (data() != null && data() instanceof Collection) {
			buf.append("[");
			boolean first = true;
			for (Object item: (Collection<?>)data()) {
				if (!first) buf.append(" ");
				buf.append(DataRules.stringOf(item));
				first = false;
			}
			buf.append("]");
		}
		else {
			buf.append("(").append(DataRules.stringOf(data())).append(")");
		}
		
		return buf.toString();
	}
	
	/**
	 * Produces:
	 * eg: <code>Number()</code>
	 * eg: <code>Expression[Number() Operator() Number()]</code>

	 * Given:
	 * eg: <code>Percept{NumberFact,ref=<>,null}</code>
	 * eg: <code>Percept{ExpressionFact,ref=<>,[Percept{NumberFact,ref=<>,null},Percept{OperatorFact,ref=<>,null},Percept{NumberFact,ref=<>,null}]}</code>
	 * 
	 * Assumes this instance represents a 'percept-type'
	 * @deprecated not in use yet, haven't figured out how to make it work for facts that reference others
	 */
	@Deprecated
	private String toCompactPerceptTypeString() {
		String name = guid();
		if (name.endsWith("Fact")) {
			name = name.substring(0, name.length() - "Fact".length());
		}
		
		StringBuilder buf = new StringBuilder();
		buf.append(name);

		if (data() == null) {
			buf.append("()");
		}
		else if (data() != null && data() instanceof Collection) {
			buf.append("[");
			boolean first = true;
			for (Object item: (Collection<?>)data()) {
				if (!first) buf.append(" ");
				buf.append(DataRules.stringOf(item));
				first = false;
			}
			buf.append("]");
		}
		else {
			buf.append("(").append(DataRules.stringOf(data())).append(")");
		}
		
		return buf.toString();
	}
	
	public String guid() {
		return guid;
	}
	
	/**
	 * Comparative size of the event.
	 * Represents how much space taken up in Working Memory, Short Term Memory, or Long Term Memory. 
	 * @return 0 or positive number
	 */
	public int size() {
		return DataRules.measureSize(data);
	}
	
	/**
	 * GUID references to facts in LTM.
	 * @return non-null set
	 */
	public Set<String> references() {
		return references;
	}
	
	/**
	 * Data always conforms to rules set by {@link DataRules}.
	 */
	public Object data() {
		return data;
	}

	/**
	 * Hash-code, meeting expectations of {@link #equals(Object)} implementation.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + ((guid == null) ? 0 : guid.hashCode());
		result = prime * result + ((references == null) ? 0 : references.hashCode());
		return result;
	}

	/**
	 * Exact equality.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		
		Percept other = (Percept) obj;
		return Objects.equals(this.data, other.data) &&
				Objects.equals(this.guid, other.guid) &&
				Objects.equals(this.references, other.references);
	}
	
	
}
