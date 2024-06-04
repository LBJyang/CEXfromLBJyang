package hongze.myCEX.match;

import java.math.BigDecimal;

import hongze.myCEX.model.trade.OrderEntity;

public record MatchDetailRecord(BigDecimal price, BigDecimal quantity, OrderEntity takerOrder, OrderEntity makerOrder) {

}
