package hongze.myCEX.sequencer;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

import hongze.myCEX.message.event.AbstractEvent;
import hongze.myCEX.messaging.MessageConsumer;
import hongze.myCEX.messaging.MessageProducer;
import hongze.myCEX.messaging.MessageTypes;
import hongze.myCEX.messaging.Messaging;
import hongze.myCEX.messaging.MessagingFactory;
import hongze.myCEX.support.LoggerSupport;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
public class SequenceService extends LoggerSupport implements CommonErrorHandler {
	private static final String GROUP_ID = "SequencerGroup";
	@Autowired
	private SequenceHandler sequenceHandler;
	@Autowired
	private MessagingFactory messagingFactory;
	@Autowired
	private MessageTypes messageTypes;

	private MessageProducer<AbstractEvent> messageProducer;
	private AtomicLong sequence;
	private Thread jobThread;
	private boolean running;
	private boolean crash;

	@PostConstruct
	public void init() {
		Thread thread = new Thread(() -> {
			logger.info("start sequence job...");
			// init kafka producer
			this.messageProducer = this.messagingFactory.createMessageProducer(Messaging.Topic.TRADE,
					AbstractEvent.class);

			// init kafka consumer
			logger.info("create message consumer for {}...", getClass().getName());
			MessageConsumer consumer = this.messagingFactory.createBatchMessageListener(Messaging.Topic.SEQUENCE,
					GROUP_ID, this::processMessages, this);

			// set the sequence
			this.sequence = new AtomicLong(this.sequenceHandler.getMaxSequenceId());

			// start running
			this.running = true;
			while (running) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					break;
				}
			}

			// close message consumer:
			logger.info("close message consumer for {}...", getClass().getName());
			consumer.stop();
			System.exit(1);
		});
		this.jobThread = thread;
		thread.start();
	}

	@PreDestroy
	public void shutdown() {
		logger.info("shutdown sequence service...");
		running = false;
		if (jobThread != null) {
			jobThread.interrupt();
			try {
				jobThread.join(5000);
			} catch (InterruptedException e) {
				logger.error("interrupt job thread failed", e);
			}
			jobThread = null;
		}
	}

	@Override
	public void handleBatch(Exception thrownException, ConsumerRecords<?, ?> data, Consumer<?, ?> consumer,
			MessageListenerContainer container, Runnable invokeListener) {
		logger.error("batch error!", thrownException);
		panic();
	}

	private void sendMessages(List<AbstractEvent> messages) {
		this.messageProducer.sendMessages(messages);
	}

	private synchronized void processMessages(List<AbstractEvent> messages) {
		if (crash || !running) {
			panic();
			return;
		}
		if (logger.isInfoEnabled()) {
			logger.info("do sequence for {} messages...", messages.size());
		}
		long start = System.currentTimeMillis();
		List<AbstractEvent> sequenced = null;
		try {
			sequenced = this.sequenceHandler.sequenceMessages(messageTypes, sequence, messages);
		} catch (Throwable e) {
			logger.error("exception when do sequence", e);
			shutdown();
			panic();
			throw new Error(e);
		}
		if (logger.isInfoEnabled()) {
			long end = System.currentTimeMillis();
			logger.info("sequenced {} messages in {} millseconds.current sequence id is {}.", messages.size(),
					end - start, this.sequence.get());
		}
		sendMessages(messages);
	}

	private void panic() {
		this.crash = true;
		this.running = false;
		System.exit(1);
	}
}
