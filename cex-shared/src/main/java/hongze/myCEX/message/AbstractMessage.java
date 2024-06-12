package hongze.myCEX.message;

import java.io.Serializable;

public class AbstractMessage implements Serializable {
	public String refId = null;
	public long createdAt;
}
