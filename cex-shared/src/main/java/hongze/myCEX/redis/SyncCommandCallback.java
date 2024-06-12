package hongze.myCEX.redis;

import io.lettuce.core.api.sync.RedisCommands;

@FunctionalInterface
public interface SyncCommandCallback<T> {
	T doInConnections(RedisCommands<String, String> commands);
}
