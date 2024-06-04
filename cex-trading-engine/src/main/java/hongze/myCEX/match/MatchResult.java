package hongze.myCEX.match;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import hongze.myCEX.model.trade.OrderEntity;

public class MatchResult {
	public final OrderEntity takerOrder;
	public final List<MatchDetailRecord> matchDetails = new ArrayList<MatchDetailRecord>();

	public MatchResult(OrderEntity takerOrder) {
		this.takerOrder = takerOrder;
	}

	public void add(BigDecimal price, BigDecimal matchQuantity, OrderEntity makerOrder) {
		this.matchDetails.add(new MatchDetailRecord(price, matchQuantity, this.takerOrder, makerOrder));
	}

	@Override
	public String toString() {
		if (matchDetails.isEmpty()) {
			return "No Match Details.";
		}
		return matchDetails.size() + " matched: "
				+ String.join(",", matchDetails.stream().map(MatchDetailRecord::toString).toArray(String[]::new));
	}
}
