package hongze.myCEX.match;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import hongze.myCEX.bean.OrderBookItemBean;
import hongze.myCEX.enums.Direction;
import hongze.myCEX.model.trade.OrderEntity;

/**
 * The order book. Given different direction,there are different treemaps. In
 * the sell book,the price is from high to low,but the buy book is reverse.You
 * can add or remove order from the book,and also you can check whether the
 * order is in the book using Method existOrder. The getOrderBook is to get the
 * order book detail,each price to amount.
 */
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

	public boolean add(OrderEntity order) {
		return this.book.put(new OrderKey(order.sequenceId, order.price), order) == null;
	}

	public boolean remove(OrderEntity order) {
		return this.book.remove(new OrderKey(order.sequenceId, order.price)) != null;
	}

	public boolean exist(OrderEntity order) {
		return this.book.containsKey(new OrderKey(order.sequenceId, order.price));
	}

	public int size() {
		return this.book.size();
	}

	/**
	 * Get the order book detail,each price to amount.
	 */
	public List<OrderBookItemBean> getOrderBook(int maxDepth) {
		List<OrderBookItemBean> items = new ArrayList<OrderBookItemBean>();
		OrderBookItemBean prevItem = null;
		for (OrderKey key : this.book.keySet()) {
			OrderEntity order = this.book.get(key);
			if (prevItem == null) {
				prevItem = new OrderBookItemBean(order.price, order.quantity);
				items.add(prevItem);
			} else {
				if (order.price.compareTo(prevItem.price) == 0) {
					prevItem.quantity.add(order.quantity);
				} else {
					if (items.size() >= maxDepth) {
						break;
					}
					prevItem = new OrderBookItemBean(order.price, order.quantity);
					items.add(prevItem);
				}
			}
		}
		return items;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		if (this.book.isEmpty()) {
			return "empty.";
		}
		List<String> orders = new ArrayList<String>(10);
		for (Entry<OrderKey, OrderEntity> entry : this.book.entrySet()) {
			OrderEntity order = entry.getValue();
			orders.add("  " + order.price + " " + order.unfilledQuantity + " " + order.toString());
		}
		if (direction == Direction.SELL) {
			Collections.reverse(orders);
		}
		return String.join("\n", orders);
	}
}
