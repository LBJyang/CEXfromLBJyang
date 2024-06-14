package hongze.myCEX.sequencer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import hongze.myCEX.message.event.AbstractEvent;
import hongze.myCEX.messaging.MessageTypes;
import hongze.myCEX.model.trade.EventEntity;
import hongze.myCEX.model.trade.UniqueEventEntity;
import hongze.myCEX.support.AbstractDbService;

@Component
@Transactional(rollbackFor = Throwable.class)
public class SequenceHandler extends AbstractDbService {
	private long lastTimeStamp = 0;

	/**
	 * @param messageTypes
	 * @param sequence
	 * @param messages
	 * @return
	 */
	public List<AbstractEvent> sequenceMessages(final MessageTypes messageTypes, final AtomicLong sequence,
			final List<AbstractEvent> messages) {
		final long t = System.currentTimeMillis();
		if (t < lastTimeStamp) {
			logger.warn("[sequence] current time {} is turned back from {}!", t, lastTimeStamp);
		} else {
			lastTimeStamp = t;
		}

		List<UniqueEventEntity> uniquesEntities = null;
		Set<String> uniqueKeys = null;
		List<AbstractEvent> sequenceMessages = new ArrayList<AbstractEvent>(messages.size());
		List<EventEntity> events = new ArrayList<EventEntity>(messages.size());

		for (AbstractEvent message : sequenceMessages) {
			UniqueEventEntity unique = null;
			final String uniqueId = message.uniqueId;
			if (uniqueId != null) {
				// check whether it is already in db or uniqueKeys.
				// db is all the UniqueId,uniqueKey is this list.
				if (uniqueKeys != null && uniqueKeys.contains(uniqueId)
						|| db.fetch(UniqueEventEntity.class, uniqueId) != null) {
					logger.warn("ignore processed unique message:{}", message);
					continue;
				}
				// add to uniquesEntities and uniqueKeys.
				unique = new UniqueEventEntity();
				unique.uniqueId = uniqueId;
				unique.createdAt = message.createdAt;
				if (uniquesEntities == null) {
					uniquesEntities = new ArrayList<UniqueEventEntity>();
				}
				uniquesEntities.add(unique);
				if (uniqueKeys == null) {
					uniqueKeys = new HashSet<String>();
				}
				uniqueKeys.add(uniqueId);
				logger.info("unique event {} sequenced.", uniqueId);
			}
			// sequence message and add to sequenceMessages
			final long previousId = sequence.get();
			final long currentId = sequence.incrementAndGet();

			message.sequenceId = currentId;
			message.previousId = previousId;
			message.createdAt = lastTimeStamp;

			if (unique != null) {
				unique.sequenceId = currentId;
			}
			// create eventEntity and add to events
			EventEntity event = new EventEntity();
			event.sequenceId = currentId;
			event.previousId = previousId;
			event.createdAt = lastTimeStamp;
			event.data = messageTypes.serialize(message);
			events.add(event);
		}
		// add to db
		db.insert(events);
		if (uniquesEntities != null) {
			db.insert(uniquesEntities);
		}
		return sequenceMessages;
	}

	public long getMaxSequenceId() {
		EventEntity last = db.from(EventEntity.class).orderBy("sequenceId").desc().first();
		if (last == null) {
			logger.info("no max sequenceId found. set max sequenceId = 0.");
			return 0;
		}
		this.lastTimeStamp = last.createdAt;
		logger.info("find max sequenceId = {}, last timestamp = {}", last.sequenceId, this.lastTimeStamp);
		return last.sequenceId;
	}
}
