package hongze.myCEX.model.trade;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hongze.myCEX.enums.Direction;
import hongze.myCEX.enums.OrderStatus;
import hongze.myCEX.model.support.EntitySupport;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "orders")
public class OrderEntity implements EntitySupport, Comparable<OrderEntity> {

	@Id
	@Column(nullable = false, updatable = false)
	public Long id;

	@Column(nullable = false, updatable = false)
	public long sequenceId;

	@Column(nullable = false, updatable = false, length = VAR_CHAR)
	public Direction direction;

	@Column(nullable = false, updatable = false)
	public Long userId;

	@Column(nullable = false, updatable = false, precision = PRECISION, scale = SCALE)
	public BigDecimal price;
	@Column(nullable = false, updatable = false, length = VAR_CHAR)
	public OrderStatus status;
	@Column(nullable = false, updatable = false, precision = PRECISION, scale = SCALE)
	public BigDecimal quantity;
	@Column(nullable = false, updatable = false, precision = PRECISION, scale = SCALE)
	public BigDecimal unfilledQuantity;
	@Column(nullable = false, updatable = false)
	public long createdAt;
	@Column(nullable = false, updatable = false)
	public long updatedAt;

	private int version;

	public void updateOrder(BigDecimal unfilledQuantity, OrderStatus status, long updatedAt) {
		version++;
		this.unfilledQuantity = unfilledQuantity;
		this.status = status;
		this.updatedAt = updatedAt;
		version++;
	}

	@org.springframework.lang.Nullable
	public OrderEntity copy() {
		OrderEntity entity = new OrderEntity();
		int ver = this.version;
		entity.unfilledQuantity = this.unfilledQuantity;
		entity.status = this.status;
		entity.updatedAt = this.updatedAt;
		if (ver != this.version) {
			return null;
		}
		entity.id = this.id;
		entity.sequenceId = this.sequenceId;
		entity.createdAt = this.createdAt;
		entity.price = this.price;
		entity.direction = this.direction;
		entity.userId = this.userId;
		entity.quantity = this.quantity;
		return entity;
	}

	@Override
	public int compareTo(OrderEntity o) {
		return Long.compare(this.id.longValue(), o.id.longValue());
	}

	@Transient
	@JsonIgnore
	public int getVersion() {
		return version;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return this.id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if (this == obj) {
			return true;
		}
		if (obj instanceof OrderEntity) {
			OrderEntity entity = (OrderEntity) obj;
			return this.id.longValue() == entity.id.longValue();
		}
		return false;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "OrderEntity [id=" + id + ", sequenceId=" + sequenceId + ", direction=" + direction + ", userId="
				+ userId + ", status=" + status + ", price=" + price + ", createdAt=" + createdAt + ", updatedAt="
				+ updatedAt + ", version=" + version + ", quantity=" + quantity + ", unfilledQuantity="
				+ unfilledQuantity + "]";
	}

}
