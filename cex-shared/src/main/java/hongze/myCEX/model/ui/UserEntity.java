package hongze.myCEX.model.ui;

import hongze.myCEX.enums.UserType;
import hongze.myCEX.model.support.EntitySupport;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class UserEntity implements EntitySupport {

	/**
	 * Primary key: auto-increment long.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false, updatable = false)
	public Long id;

	@Column(nullable = false, updatable = false, length = VAR_CHAR)
	public UserType type;

	/**
	 * Created time (milliseconds).
	 */
	@Column(nullable = false, updatable = false)
	public long createdAt;

	@Override
	public String toString() {
		return "UserEntity [id=" + id + ", type=" + type + ", createdAt=" + createdAt + "]";
	}
}
