package hongze.myCEX.enums;

public enum Direction {
	BUY(1), SELL(0);

	public final int value;

	private Direction(int value) {
		// TODO Auto-generated constructor stub
		this.value = value;
	}

	public Direction negate() {
		return this == BUY ? SELL : BUY;
	}

	public static Direction of(int intvalue) {
		if (intvalue == 1) {
			return BUY;
		}
		if (intvalue == 0) {
			return SELL;
		}
		throw new IllegalArgumentException("Invalid Direction Value!");
	}
}
