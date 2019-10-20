package lett.malcolm.consciouscalculator.xunused;

/**
 * Holds the guid reference of another event.
 * 
 * Treated as a simple-type by Data Rules.
 */
public class EventRef {
	private final String guid;

	public EventRef(String guid) {
		this.guid = guid;
	}
	
	public String guid() {
		return guid;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((guid == null) ? 0 : guid.hashCode());
		return result;
	}

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
		EventRef other = (EventRef) obj;
		if (guid == null) {
			if (other.guid != null) {
				return false;
			}
		} else if (!guid.equals(other.guid)) {
			return false;
		}
		return true;
	}
	
}
