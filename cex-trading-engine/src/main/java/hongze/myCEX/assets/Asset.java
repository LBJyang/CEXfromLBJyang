package hongze.myCEX.assets;

import java.math.BigDecimal;

public class Asset {
	public BigDecimal available;
	public BigDecimal frozen;

	public Asset() {
		available = BigDecimal.ZERO;
		frozen = BigDecimal.ZERO;
	}

	public Asset(BigDecimal available, BigDecimal frozen) {
		this.available = available;
		this.frozen = frozen;
	}

	public BigDecimal getAvailable() {
		return available;
	}

	public BigDecimal getFrozen() {
		return frozen;
	}

	public BigDecimal getTotal() {
		return available.add(frozen);
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return String.format("[available = %04.2f,frozen = %04.2f]", available, frozen);
	}

}
