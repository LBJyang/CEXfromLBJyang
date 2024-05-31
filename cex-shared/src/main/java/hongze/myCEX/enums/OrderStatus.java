package hongze.myCEX.enums;

public enum OrderStatus {
	PENDING(false), FULLY_FILLED(true), PARTIAL_FILLED(false), PARTIAL_CANCELLED(false), FULLY_CANCELLED(false);

	public final boolean isFinalStatus;

	private OrderStatus(boolean isFinalStatus) {
		// TODO Auto-generated constructor stub
		this.isFinalStatus = isFinalStatus;
	}
}
