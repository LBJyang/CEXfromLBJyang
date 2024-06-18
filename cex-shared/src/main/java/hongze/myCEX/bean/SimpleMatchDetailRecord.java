package hongze.myCEX.bean;

import java.math.BigDecimal;

import hongze.myCEX.enums.MatchType;

public record SimpleMatchDetailRecord(BigDecimal price, BigDecimal quantity, MatchType type) {
}
