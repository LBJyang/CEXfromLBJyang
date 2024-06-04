package hongze.myCEX.match;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import hongze.myCEX.bean.OrderBookBean;
import hongze.myCEX.enums.Direction;
import hongze.myCEX.enums.OrderStatus;
import hongze.myCEX.model.trade.OrderEntity;

@Component
public class MatchEngine {
	public final OrderBook buyBook = new OrderBook(Direction.BUY);
	public final OrderBook sellBook = new OrderBook(Direction.SELL);
	public BigDecimal marketPrice = BigDecimal.ZERO;
	private long sequenceId;

	public MatchResult processOrder(long sequenceId, OrderEntity order) {
		return switch (order.direction) {
		case SELL -> processOrder(sequenceId, order, this.buyBook, this.sellBook);
		case BUY -> processOrder(sequenceId, order, sellBook, buyBook);
		default -> throw new IllegalArgumentException("Unexpected value: " + order);
		};
	}

	private MatchResult processOrder(long sequenceId, OrderEntity takerOrder, OrderBook makerBook,
			OrderBook anotherBook) {
		this.sequenceId = sequenceId;
		long ts = takerOrder.createdAt;
		MatchResult matchResult = new MatchResult(takerOrder);
		BigDecimal takerUnfilledQuantity = takerOrder.quantity;
		for (;;) {
			OrderEntity makerOrder = makerBook.getFirst();
			if (makerOrder == null) {
				break;// counter order not found
			}
			if (takerOrder.direction == Direction.SELL && takerOrder.price.compareTo(makerOrder.price) > 0) {
				break;// sell order price is higher than the highest bid price.
			}
			if (takerOrder.direction == Direction.BUY && takerOrder.price.compareTo(makerOrder.price) < 0) {
				break; // buy order price is lower than the lowest bid price.
			}
			// update market price
			this.marketPrice = makerOrder.price;
			// update matched quantity
			BigDecimal matchedQuantity = takerUnfilledQuantity.min(takerOrder.unfilledQuantity);
			// update matchResult
			matchResult.add(marketPrice, matchedQuantity, makerOrder);
			// update the orders' quantities
			takerUnfilledQuantity = takerUnfilledQuantity.subtract(matchedQuantity);
			BigDecimal makerUnfilledQuantity = makerOrder.unfilledQuantity.subtract(matchedQuantity);
			if (makerUnfilledQuantity.signum() == 0) {// makerOrder is completely fulfilled,remove it from maker book
														// and update the maker order.
				makerOrder.updateOrder(makerUnfilledQuantity, OrderStatus.FULLY_FILLED, ts);
				makerBook.removeOrder(makerOrder);
			} else {// maker order is partial fulfilled,just update the order.
				makerOrder.updateOrder(makerUnfilledQuantity, OrderStatus.PARTIAL_FILLED, ts);
			}
			// taker order completely fulfilled,break;
			if (takerUnfilledQuantity.signum() == 0) {
				takerOrder.updateOrder(takerUnfilledQuantity, OrderStatus.FULLY_FILLED, ts);
				break;
			}
		}
		// if the taker order is not completely fulfilled,updateOrder and update
		// another order book.
		if (takerUnfilledQuantity.signum() > 0) {
			takerOrder.updateOrder(takerUnfilledQuantity,
					takerUnfilledQuantity.compareTo(takerOrder.quantity) == 0 ? OrderStatus.PENDING
							: OrderStatus.PARTIAL_FILLED,
					ts);
			anotherBook.addOrder(takerOrder);
		}
		return matchResult;
	}

	public void cancel(long ts, OrderEntity order) {
		// remove from order book
		OrderBook book = order.direction == Direction.BUY ? this.buyBook : this.sellBook;
		if (!book.removeOrder(order)) {
			throw new IllegalArgumentException("Order not found in order book.");
		}
		// update order
		OrderStatus status = order.unfilledQuantity.compareTo(order.quantity) == 0 ? OrderStatus.FULLY_CANCELLED
				: OrderStatus.PARTIAL_CANCELLED;
		order.updateOrder(order.unfilledQuantity, status, ts);
	}

	public OrderBookBean getOrderBook(int maxDepth) {
		return new OrderBookBean(this.sequenceId, this.marketPrice, this.buyBook.getOrderBook(maxDepth),
				this.sellBook.getOrderBook(maxDepth));
	}
}
