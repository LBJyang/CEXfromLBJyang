package hongze.myCEX.order;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import hongze.myCEX.assets.AssetService;
import hongze.myCEX.enums.AssetEnum;
import hongze.myCEX.enums.Direction;
import hongze.myCEX.model.trade.OrderEntity;

@Component
public class OrderService {
	final AssetService assetService;

	public OrderService(@Autowired AssetService assetService) {
		this.assetService = assetService;
	}

	public ConcurrentMap<Long, OrderEntity> getActiveOrder() {
		return activeOrders;
	}

	// track all active orders
	final ConcurrentMap<Long, OrderEntity> activeOrders = new ConcurrentHashMap<Long, OrderEntity>();
	// track some user's all orders
	final ConcurrentMap<Long, ConcurrentMap<Long, OrderEntity>> userOrders = new ConcurrentHashMap<Long, ConcurrentMap<Long, OrderEntity>>();

	// get OrderEntity by orderId
	public OrderEntity getOrder(Long orderId) {
		return activeOrders.get(orderId);
	}

	// get some user's all orders
	public ConcurrentMap<Long, OrderEntity> getUserOrder(Long userId) {
		return userOrders.get(userId);
	}

	// create order,if fail,return null
	public OrderEntity createOrder(long sequenceId, long ts, Long orderId, Long userId, Direction direction,
			BigDecimal price, BigDecimal quantity) {
		switch (direction) {
		case SELL -> {
			if (!assetService.tryFreeze(userId, AssetEnum.USDT, price.multiply(quantity))) {
				return null;
			}
		}
		case BUY -> {
			if (!assetService.tryFreeze(userId, AssetEnum.BTC, price.multiply(quantity))) {
				return null;
			}
		}
		default -> throw new IllegalArgumentException("Invalid Direction!");
		}
		OrderEntity order = new OrderEntity();
		order.sequenceId = sequenceId;
		order.id = orderId;
		order.userId = userId;
		order.direction = direction;
		order.price = price;
		order.quantity = quantity;
		order.unfilledQuantity = quantity;
		order.createdAt = order.updatedAt = ts;
		// add order to active orders
		this.activeOrders.put(orderId, order);
		// add order to user orders
		ConcurrentMap<Long, OrderEntity> uOrders = this.userOrders.get(userId);
		if (uOrders == null) {
			uOrders = new ConcurrentHashMap<Long, OrderEntity>();
			this.userOrders.put(userId, uOrders);

		}
		uOrders.put(orderId, order);
		return order;
	}

	public void removeOrder(Long orderId) {
		// remove from activeOrders
		OrderEntity removed = activeOrders.remove(orderId);
		if (removed == null) {
			throw new IllegalArgumentException("Order not found by orderId in active orders: " + orderId);
		}
		// remove from userOrders
		ConcurrentMap<Long, OrderEntity> uOrders = userOrders.get(removed.userId);
		if (uOrders == null) {
			throw new IllegalArgumentException("User orders not found by userId: " + removed.userId);
		}
		if (uOrders.remove(orderId) == null) {
			throw new IllegalArgumentException("Order not found by orderId in user orders: " + orderId);
		}
	}
}
