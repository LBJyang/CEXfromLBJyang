package hongze.myCEX.messaging;

import java.util.List;

import hongze.myCEX.message.AbstractMessage;


@FunctionalInterface
public interface BatchMessageHandler<T extends AbstractMessage> {

    void processMessages(List<T> messages);

}
