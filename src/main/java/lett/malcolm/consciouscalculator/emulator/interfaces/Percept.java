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

/**
 * Represents interpreted data with attached meaning.
 * 
 * Immutable, and permitted by Data Rules.
 * Implements equals() and hashCode() according to the needs of {@link DataRules#isSame(Object, Object)}.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Percept {
	private final String guid;
	private final Set<String> references;
	private final Object data;

	@JsonCreator
	public Percept(String guid, Set<String> references, Object data) {
		this.guid = guid;
		this.references = Collections.unmodifiableSet(references);
		this.data = data;
	}

	public Percept(String reference, Object data) {
		this.guid = UUID.randomUUID().toString();
		this.references = Collections.singleton(reference);
		this.data = data;
	}
	
	/**
	 * Deep clone.
	 */
	public Percept clone() {
		return new Percept(guid,
				new HashSet<>(this.references),
				DataRules.clone(this.data));
	}
	
	@Override
	public String toString() {
		if (references.size() == 1 && !references.iterator().next().contains("-")) {
			return toCompactString();
		}
		else {
			return toLongString();
		}
	}
	
	/**
	 * eg: <code>Number#b405f(3)</code>
	 * eg: <code>Expression#34f98[Number#b405f(3) Operator#1a667(+) Number#5b503(5)]</code>
	 * @return
	 */
	private String toCompactString() {
		String ref = references.iterator().next();
		if (ref.endsWith("Fact")) {
			ref = ref.substring(0, ref.length() - "Fact".length());
		}
		
		StringBuilder buf = new StringBuilder();
		buf.append(ref);
		buf.append("#").append(guid.substring(0,5));
		
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
	 * eg: <code>Percept{b405f,ref=NumberFact,3}</code>
	 * eg: <code>Percept{34f98,ref=ExpressionFact,[Percept{b405f,ref=NumberFact,3},Percept{1a667,ref=OperatorFact,+},Percept{5b503,ref=NumberFact,5}]}</code>
	 * @return
	 */
	private String toLongString() {
		StringBuilder buf = new StringBuilder();
		buf.append(getClass().getSimpleName()).append("{");
		
		// guid
		buf.append(guid.substring(0,5)).append(",");
		
		// references
		for (String reference: references) {
			if (reference.contains("-")) {
				// guid ref
				buf.append("ref=").append(reference.substring(0,5)).append(",");
			}
			else {
				// pre-programmed fact ref
				buf.append("ref=").append(reference).append(",");
			}
		}
		
		// content
		buf.append(DataRules.stringOf(data));
		
		buf.append("}");
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
