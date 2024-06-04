package hongze.myCEX.bean;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hongze.myCEX.bean.util.JsonUtil;

public class OrderBookBean {
	public OrderBookBean(long sequenceId, BigDecimal price, List<OrderBookItemBean> buy, List<OrderBookItemBean> sell) {
		// TODO Auto-generated constructor stub
		this.sequenceId = sequenceId;
		this.price = price;
		this.buy = buy;
		this.sell = sell;
	}

	public static final String EMPTY = JsonUtil.writeJson(new OrderBookBean(0, BigDecimal.ZERO, List.of(), List.of()));

	public List<OrderBookItemBean> buy;

	public List<OrderBookItemBean> sell;
	@JsonIgnore
	public long sequenceId;

	public BigDecimal price;
}
