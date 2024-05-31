package hongze.myCEX.assets;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import hongze.myCEX.enums.AssetEnum;
import hongze.myCEX.support.LoggerSupport;

public class AssetService extends LoggerSupport {
	final ConcurrentMap<Long, ConcurrentMap<AssetEnum, Asset>> userAssets = new ConcurrentHashMap<Long, ConcurrentMap<AssetEnum, Asset>>();

	public ConcurrentMap<Long, ConcurrentMap<AssetEnum, Asset>> getUserAssets() {
		return this.userAssets;
	}

	public Map<AssetEnum, Asset> getAssets(Long userId) {
		Map<AssetEnum, Asset> assets = userAssets.get(userId);
		if (assets == null) {
			return Map.of();
		}
		return assets;
	}

	public Asset getAsset(Long userId, AssetEnum assetId) {
		Map<AssetEnum, Asset> assets = userAssets.get(userId);
		if (assets == null) {
			return null;
		}
		return assets.get(assetId);
	}

//basic transfer
	public boolean tryTransfer(Transfer type, Long fromUser, Long toUser, AssetEnum assetId, BigDecimal amount,
			boolean checkBalance) {
		if (amount.signum() == 0) {
			return true;
		}
		if (amount.signum() < 0) {
			throw new IllegalArgumentException("Negative Amount!");
		}
		Asset fromAsset = getAsset(fromUser, assetId);
		Asset toAsset = getAsset(toUser, assetId);
		if (fromAsset == null) {
			fromAsset = initAsset(fromUser, assetId);
		}
		if (toAsset == null) {
			toAsset = initAsset(toUser, assetId);
		}
		return switch (type) {
		case AVAILABLE_TO_AVAILABLE -> {
			if (checkBalance && fromAsset.available.compareTo(amount) < 0) {
				yield false;
			}
			fromAsset.available = fromAsset.available.subtract(amount);
			toAsset.available = toAsset.available.add(amount);
			yield true;
		}
		case AVAILABLE_TO_FROZEN -> {
			if (checkBalance && fromAsset.available.compareTo(amount) < 0) {
				yield false;
			}
			fromAsset.available = fromAsset.available.subtract(amount);
			toAsset.frozen = toAsset.frozen.add(amount);
			yield true;
		}
		case FROZEN_TO_AVAILABLE -> {
			if (checkBalance && fromAsset.frozen.compareTo(amount) < 0) {
				yield false;
			}
			fromAsset.frozen = fromAsset.frozen.subtract(amount);
			toAsset.available = toAsset.available.add(amount);
			yield true;
		}
		default -> {
			throw new IllegalArgumentException("Unexpected value: " + type);
		}
		};
	}

//just transfer
	public void transfer(Transfer type, Long fromUser, Long toUser, AssetEnum assetId, BigDecimal amount) {
		if (!tryTransfer(type, fromUser, toUser, assetId, amount, true)) {
			throw new RuntimeException("Transfer failed for AVAILABLE_TO_AVAILABLE, from user " + fromUser + " to user "
					+ toUser + ", asset = " + assetId + ", amount = " + amount);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("transfer asset {},from {} to {},amount {}", assetId, fromUser, toUser, amount);
		}
	}

//freeze some account's asset
	public boolean tryFreeze(Long userId, AssetEnum assetId, BigDecimal amount) {
		boolean result = tryTransfer(Transfer.AVAILABLE_TO_FROZEN, userId, userId, assetId, amount, true);
		if (result && logger.isDebugEnabled()) {
			logger.debug("freeze user {} asset {} amount {}", userId, assetId, amount);
		}
		return result;
	}

//unfreeze some account's asset
	public void unFreeze(Long userId, AssetEnum assetId, BigDecimal amount) {
		if (!tryTransfer(Transfer.FROZEN_TO_AVAILABLE, userId, userId, assetId, amount, true)) {
			throw new RuntimeException(
					"Unfreeze failed for user " + userId + ", asset = " + assetId + ", amount = " + amount);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("unfreeze user {} asset {} amount {}", userId, assetId, amount);
		}
	}

	public Asset initAsset(Long userId, AssetEnum assetId) {
		ConcurrentMap<AssetEnum, Asset> map = userAssets.get(userId);
		if (map == null) {
			map = new ConcurrentHashMap<AssetEnum, Asset>();
			userAssets.put(userId, map);
		}
		Asset asset = new Asset();
		map.put(assetId, asset);
		return asset;
	}
}
