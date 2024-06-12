package hongze.myCEX.model.trade;

import hongze.myCEX.model.support.EntitySupport;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "events", uniqueConstraints = @UniqueConstraint(name = "UNI_PREV_ID", columnNames = { "previousId" }))
public class EventEntity implements EntitySupport {
	@Column(nullable = false, updatable = false)
	@Id
	public long sequenceId;
	@Column(nullable = false, updatable = false)
	public long previousId;
	@Column(nullable = false, updatable = false, length = VAR_CHAR_10000)
	public String data;
	@Column(nullable = false, updatable = false)
	public long createdAt;

	@Override
	public String toString() {
		return "EventEntity [sequenceId=" + sequenceId + ", previousId=" + previousId + ", data=" + data
				+ ", createdAt=" + createdAt + "]";
	}
}
