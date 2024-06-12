package hongze.myCEX.message;

import java.util.List;

import hongze.myCEX.model.quotation.TickEntity;

public class TickMessage extends AbstractMessage {

	public long sequenceId;

	public List<TickEntity> ticks;

}
