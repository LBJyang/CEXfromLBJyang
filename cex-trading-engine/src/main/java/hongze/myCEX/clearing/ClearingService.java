package hongze.myCEX.clearing;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;

import hongze.myCEX.assets.AssetService;
import hongze.myCEX.assets.Transfer;
import hongze.myCEX.enums.AssetEnum;
import hongze.myCEX.match.MatchDetailRecord;
import hongze.myCEX.match.MatchResult;
import hongze.myCEX.model.trade.OrderEntity;
import hongze.myCEX.order.OrderService;
import hongze.myCEX.support.LoggerSupport;

public class ClearingService extends LoggerSupport {
	final AssetService assetService;
	final OrderService orderService;

	public ClearingService(@Autowired AssetService assetService, @Autowired OrderService orderService) {
		this.assetService = assetService;
		this.orderService = orderService;
	}

	public void clearMatchResult(MatchResult result) {
		OrderEntity taker = result.takerOrder;
		switch (taker.direction) {
		case BUY -> {// when buy,the price is the maker price,always bigger than taker price
			for (MatchDetailRecord detail : result.matchDetails) {
				if (logger.isDebugEnabled()) {
					logger.debug(
							"clear buy matched detail: price = {}, quantity = {}, takerOrderId = {}, makerOrderId = {}, takerUserId = {}, makerUserId = {}",
							detail.price(), detail.quantity(), detail.takerOrder().id, detail.makerOrder().id,
							detail.takerOrder().userId, detail.makerOrder().userId);
				}
				OrderEntity maker = detail.makerOrder();
				BigDecimal matched = detail.quantity();
				// if the taker price is higher,unfreeze the unused quote.
				if (taker.price.compareTo(maker.price) > 0) {
					BigDecimal unfreezeQuote = taker.price.subtract(maker.price).multiply(matched);
					logger.debug("unfree extra unused quote {} back to taker user {}", unfreezeQuote, taker.userId);
					assetService.unFreeze(taker.userId, AssetEnum.USDT, unfreezeQuote);
				}
				// transfer the USDT from buyer to seller
				assetService.transfer(Transfer.FROZEN_TO_AVAILABLE, taker.id, maker.id, AssetEnum.USDT,
						maker.price.multiply(matched));
				// transfer the BTC from seller to buyer
				assetService.transfer(Transfer.FROZEN_TO_AVAILABLE, maker.id, taker.id, AssetEnum.BTC, matched);
				if (maker.unfilledQuantity.signum() == 0) {
					orderService.removeOrder(maker.id);
				}
			}
			if (taker.unfilledQuantity.signum() == 0) {
				orderService.removeOrder(taker.id);
			}
		}
		case SELL -> {
		}
		default -> throw new IllegalArgumentException("Invalid direction.");
		}
	}

	public void clearCancelledOrder(OrderEntity order) {
		switch (order.direction) {
		case BUY -> assetService.unFreeze(order.id, AssetEnum.USDT, order.unfilledQuantity.multiply(order.price));
		case SELL -> assetService.unFreeze(order.id, AssetEnum.BTC, order.unfilledQuantity);
		default -> throw new IllegalArgumentException("Unexpected Direction!");
		}
		orderService.removeOrder(order.id);
	}
}
