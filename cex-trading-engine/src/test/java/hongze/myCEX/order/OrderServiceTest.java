package hongze.myCEX.order;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import hongze.myCEX.assets.AssetService;
import hongze.myCEX.assets.Transfer;
import hongze.myCEX.enums.AssetEnum;
import hongze.myCEX.enums.Direction;

class OrderServiceTest {
	static final Long CEX = 1L;
	static final Long UserA = 2000L;
	static final Long UserB = 3000L;
	static final Long UserC = 4000L;
	OrderService orderService;
	AssetService assetService;

	@BeforeEach
	void setUp() throws Exception {
		orderService = new OrderService(assetService);
		init();
	}

	/**
	 * A: USDT=123000, BTC=12
	 * 
	 * B: USDT=456000
	 * 
	 * C: BTC=34
	 */
	private void init() {
		assetService.tryTransfer(Transfer.AVAILABLE_TO_AVAILABLE, CEX, UserA, AssetEnum.USDT,
				BigDecimal.valueOf(123000), false);
		assetService.tryTransfer(Transfer.AVAILABLE_TO_AVAILABLE, CEX, UserA, AssetEnum.BTC, BigDecimal.valueOf(12),
				false);
		assetService.tryTransfer(Transfer.AVAILABLE_TO_AVAILABLE, CEX, UserB, AssetEnum.USDT,
				BigDecimal.valueOf(456000), false);
		assetService.tryTransfer(Transfer.AVAILABLE_TO_AVAILABLE, CEX, UserC, AssetEnum.BTC, BigDecimal.valueOf(34),
				false);

		assertBDEquals(-579000, assetService.getAsset(CEX, AssetEnum.USDT).available);
		assertBDEquals(-46, assetService.getAsset(CEX, AssetEnum.BTC).available);
	}

	void assertBDEquals(String value, BigDecimal bd) {
		assertTrue(new BigDecimal(value).compareTo(bd) == 0,
				String.format("Expected %s but actual %s", value, bd.toPlainString()));
	}

	void assertBDEquals(long value, BigDecimal bd) {
		assertBDEquals(String.valueOf(value), bd);
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void createOrderTest() {
		orderService.createOrder(1L, 10010100, 1L, UserA, Direction.SELL, BigDecimal.valueOf(90000),
				BigDecimal.valueOf(0.1));
	}

}
