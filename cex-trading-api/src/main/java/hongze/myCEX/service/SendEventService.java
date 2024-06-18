package hongze.myCEX.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import hongze.myCEX.message.event.AbstractEvent;
import hongze.myCEX.messaging.MessageProducer;
import hongze.myCEX.messaging.Messaging;
import hongze.myCEX.messaging.MessagingFactory;
import jakarta.annotation.PostConstruct;

@Component
public class SendEventService {

    @Autowired
    private MessagingFactory messagingFactory;

    private MessageProducer<AbstractEvent> messageProducer;

    @PostConstruct
    public void init() {
        this.messageProducer = messagingFactory.createMessageProducer(Messaging.Topic.SEQUENCE, AbstractEvent.class);
    }

    public void sendMessage(AbstractEvent message) {
        this.messageProducer.sendMessage(message);
    }
}
