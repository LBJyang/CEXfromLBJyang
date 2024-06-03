package hongze.myCEX.match;

import java.util.Comparator;
import java.util.TreeMap;

import hongze.myCEX.enums.Direction;
import hongze.myCEX.model.trade.OrderEntity;

public class OrderBook {
	public final Direction direction;
	public final TreeMap<OrderKey, OrderEntity> book;

	public OrderBook(Direction direction) {
		// TODO Auto-generated constructor stub
		this.direction = direction;
		this.book = new TreeMap<OrderKey, OrderEntity>(direction == Direction.BUY ? SORT_BUY : SORT_SELL);
	}

	/**
	 * Buy orders are sorted by the higher price first.
	 */
	private static final Comparator<OrderKey> SORT_BUY = new Comparator<OrderKey>() {

		@Override
		public int compare(OrderKey o1, OrderKey o2) {
			int cmp = o2.price().compareTo(o1.price());
			return cmp == 0 ? Long.compare(o1.sequenceId(), o2.sequenceId()) : cmp;
		}
	};

	/**
	 * Sell orders are sorted by the lower price first.
	 */
	private static final Comparator<OrderKey> SORT_SELL = new Comparator<OrderKey>() {

		@Override
		public int compare(OrderKey o1, OrderKey o2) {
			int cmp = o1.price().compareTo(o2.price());
			return cmp == 0 ? Long.compare(o1.sequenceId(), o2.sequenceId()) : cmp;
		}
	};

	/**
	 * Get first order.
	 */
	public OrderEntity getFirst() {
		return this.book.isEmpty() ? null : this.book.firstEntry().getValue();
	}

	public boolean addOrder(OrderEntity order) {
		return this.book.put(new OrderKey(order.sequenceId, order.price), order) == null;
	}

	public boolean removeOrder(OrderEntity order) {
		return this.book.remove(new OrderKey(order.sequenceId, order.price)) != null;
	}

	public boolean existOrder(OrderEntity order) {
		return this.book.containsKey(new OrderKey(order.sequenceId, order.price));
	}

	public int size() {
		// TODO Auto-generated method stub
		return this.book.size();
	}
	
	
}
