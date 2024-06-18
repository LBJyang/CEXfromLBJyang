package hongze.myCEX.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import hongze.myCEX.bean.SimpleMatchDetailRecord;
import hongze.myCEX.model.trade.MatchDetailEntity;
import hongze.myCEX.model.trade.OrderEntity;
import hongze.myCEX.support.AbstractDbService;

@Component
public class HistoryService extends AbstractDbService {

	public List<OrderEntity> getHistoryOrders(Long userId, int maxResults) {
		return db.from(OrderEntity.class).where("userId = ?", userId).orderBy("id").desc().limit(maxResults).list();
	}

	public OrderEntity getHistoryOrder(Long userId, Long orderId) {
		OrderEntity entity = db.fetch(OrderEntity.class, orderId);
		if (entity == null || entity.userId.longValue() != userId.longValue()) {
			return null;
		}
		return entity;
	}

	public List<SimpleMatchDetailRecord> getHistoryMatchDetails(Long orderId) {
		List<MatchDetailEntity> details = db.select("price", "quantity", "type").from(MatchDetailEntity.class)
				.where("orderId = ?", orderId).orderBy("id").list();
		return details.stream().map(e -> new SimpleMatchDetailRecord(e.price, e.quantity, e.type))
				.collect(Collectors.toList());
	}
}
