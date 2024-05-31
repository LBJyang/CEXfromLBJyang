package hongze.myCEX.model.trade;

import java.math.BigDecimal;

import hongze.myCEX.enums.Direction;
import hongze.myCEX.enums.OrderStatus;
import hongze.myCEX.model.support.EntitySupport;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class OrderEntity implements EntitySupport, Comparable<OrderEntity> {
	
	@Id
	@Column(nullable = false, updatable = false)
	public Long id;
	
	public long sequenceId;
	public Direction direction;

	public Long userId;
	public BigDecimal price;
	public OrderStatus status;

	public BigDecimal quantity;
	public BigDecimal unfilledQuantity;

	public long createdAt;
	public long updatedAt;

	private int version;

	public void updateOrder(BigDecimal unfilledQuantity, OrderStatus status, long updatedAt) {
		version++;
		this.unfilledQuantity = unfilledQuantity;
		this.status = status;
		this.updatedAt = updatedAt;
		version++;
	}

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
