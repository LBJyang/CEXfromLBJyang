package hongze.myCEX.match;

import java.math.BigDecimal;

public record OrderKey(long sequenceId, BigDecimal price) {

}
