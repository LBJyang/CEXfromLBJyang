package hongze.myCEX.store;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import hongze.myCEX.db.DbTemplate;
import hongze.myCEX.message.event.AbstractEvent;
import hongze.myCEX.messaging.MessageTypes;
import hongze.myCEX.model.support.EntitySupport;
import hongze.myCEX.model.trade.EventEntity;
import hongze.myCEX.support.LoggerSupport;

public class StoreService extends LoggerSupport {
	@Autowired
	MessageTypes messageTypes;
	@Autowired
	DbTemplate dbTemplate;

	public List<AbstractEvent> loadEventsFromDB(long lastEventId) {
		List<EventEntity> events = this.dbTemplate.from(EventEntity.class).where("sequenceId > ?", lastEventId)
				.orderBy("sequenceId").limit(100000).list();
		return events.stream().map(event -> (AbstractEvent) messageTypes.deserialize(event.data))
				.collect(Collectors.toList());
	}

	public void insertIgnore(List<? extends EntitySupport> list) {
		dbTemplate.insertIgnore(list);
	}
}
