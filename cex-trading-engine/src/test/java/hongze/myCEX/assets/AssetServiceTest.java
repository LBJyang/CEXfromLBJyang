package hongze.myCEX.assets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import hongze.myCEX.enums.AssetEnum;

class AssetServiceTest {

	static final Long CEX = 1L;
	static final Long UserA = 2000L;
	static final Long UserB = 3000L;
	static final Long UserC = 4000L;

	AssetService assetService;

	@BeforeEach
	void setUp() throws Exception {
		assetService = new AssetService();
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
		// TODO Auto-generated method stub
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
		verify();
	}

// sum of asset must be 0 for each asset.
	private void verify() {
		BigDecimal total = BigDecimal.ZERO;
		for (AssetEnum assetId : AssetEnum.values()) {
			for (Long userId : assetService.userAssets.keySet()) {
				Asset asset = assetService.getAsset(userId, assetId);
				if (asset != null) {
					total = total.add(asset.available).add(asset.frozen);
				}
			}
			assertBDEquals(0, total);
			total = BigDecimal.ZERO;
		}
	}

	@Test
	void tryTransfer() {
		// amount = 0
		assertTrue(assetService.tryTransfer(Transfer.AVAILABLE_TO_AVAILABLE, UserA, UserB, AssetEnum.USDT,
				BigDecimal.valueOf(0), true));
		// amout < 0
		assertThrows(IllegalArgumentException.class, () -> assetService.tryTransfer(Transfer.AVAILABLE_TO_AVAILABLE,
				UserA, UserB, AssetEnum.USDT, BigDecimal.valueOf(-1), true));
		// amount > 0,not bigger than balance
		assetService.tryTransfer(Transfer.AVAILABLE_TO_AVAILABLE, UserA, UserB, AssetEnum.USDT,
				BigDecimal.valueOf(23000), true);
		assertBDEquals(100000, assetService.getAsset(UserA, AssetEnum.USDT).available);
		assertBDEquals(479000, assetService.getAsset(UserB, AssetEnum.USDT).available);

		// amount > 0,bigger than balance
		assertFalse(assetService.tryTransfer(Transfer.AVAILABLE_TO_AVAILABLE, UserA, UserB, AssetEnum.USDT,
				BigDecimal.valueOf(100001), true));
		assertBDEquals(100000, assetService.getAsset(UserA, AssetEnum.USDT).available);
		assertBDEquals(479000, assetService.getAsset(UserB, AssetEnum.USDT).available);
	}

	@Test
	void tryFreeze() {
//freeze 23000
		assetService.tryFreeze(UserA, AssetEnum.USDT, BigDecimal.valueOf(23000));
		assertBDEquals(100000, assetService.getAsset(UserA, AssetEnum.USDT).available);
		assertBDEquals(23000, assetService.getAsset(UserA, AssetEnum.USDT).frozen);
		// freeze 100001
		assertFalse(assetService.tryFreeze(UserA, AssetEnum.USDT, BigDecimal.valueOf(100001)));
		assertBDEquals(100000, assetService.getAsset(UserA, AssetEnum.USDT).available);
		assertBDEquals(23000, assetService.getAsset(UserA, AssetEnum.USDT).frozen);
	}

	@Test
	void unFreeze() {
		// freeze 23000
		assetService.tryFreeze(UserA, AssetEnum.USDT, BigDecimal.valueOf(23000));
		assertBDEquals(100000, assetService.getAsset(UserA, AssetEnum.USDT).available);
		assertBDEquals(23000, assetService.getAsset(UserA, AssetEnum.USDT).frozen);

		// unfreeze 13000
		assetService.unFreeze(UserA, AssetEnum.USDT, BigDecimal.valueOf(13000));
		assertBDEquals(113000, assetService.getAsset(UserA, AssetEnum.USDT).available);
		assertBDEquals(10000, assetService.getAsset(UserA, AssetEnum.USDT).frozen);

		// unfreeze 10001
		assertThrows(RuntimeException.class,
				() -> assetService.unFreeze(UserA, AssetEnum.USDT, BigDecimal.valueOf(10001)));
		assertBDEquals(113000, assetService.getAsset(UserA, AssetEnum.USDT).available);
		assertBDEquals(10000, assetService.getAsset(UserA, AssetEnum.USDT).frozen);
	}

	@Test
	void transfer() {
		// A available -> A frozen
		assetService.transfer(Transfer.AVAILABLE_TO_FROZEN, UserA, UserA, AssetEnum.USDT, BigDecimal.valueOf(23000));
		assertBDEquals(100000, assetService.getAsset(UserA, AssetEnum.USDT).available);
		assertBDEquals(23000, assetService.getAsset(UserA, AssetEnum.USDT).frozen);
		// A frozen -> C available
		assetService.transfer(Transfer.FROZEN_TO_AVAILABLE, UserA, UserC, AssetEnum.USDT, BigDecimal.valueOf(13000));
		assertBDEquals(10000, assetService.getAsset(UserA, AssetEnum.USDT).frozen);
		assertBDEquals(13000, assetService.getAsset(UserC, AssetEnum.USDT).available);
		// A frozen -> B frozen
		assertThrows(RuntimeException.class, () -> assetService.transfer(Transfer.FROZEN_TO_AVAILABLE, UserA, UserB,
				AssetEnum.USDT, BigDecimal.valueOf(10001)));
	}
}
