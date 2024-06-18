package hongze.myCEX.user;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import hongze.myCEX.ApiError;
import hongze.myCEX.ApiException;
import hongze.myCEX.enums.UserType;
import hongze.myCEX.model.ui.PasswordAuthEntity;
import hongze.myCEX.model.ui.UserEntity;
import hongze.myCEX.model.ui.UserProfileEntity;
import hongze.myCEX.support.AbstractDbService;
import hongze.myCEX.util.HashUtil;
import hongze.myCEX.util.RandomUtil;

@Component
@Transactional
public class UserService extends AbstractDbService {

	public UserProfileEntity getUserProfile(Long userId) {
		return db.get(UserProfileEntity.class, userId);
	}

	@Nullable
	public UserProfileEntity fetchUserProfileByEmail(String email) {
		return db.from(UserProfileEntity.class).where("email = ?", email).first();
	}

	public UserProfileEntity getUserProfileByEmail(String email) {
		UserProfileEntity userProfile = fetchUserProfileByEmail(email);
		if (userProfile == null) {
			throw new ApiException(ApiError.AUTH_SIGNIN_FAILED);
		}
		return userProfile;
	}

	public UserProfileEntity signup(String email, String name, String passwd) {
		final long ts = System.currentTimeMillis();
		// insert user:
		var user = new UserEntity();
		user.type = UserType.TRADER;
		user.createdAt = ts;
		db.insert(user);
		// insert user profile:
		var up = new UserProfileEntity();
		up.userId = user.id;
		up.email = email;
		up.name = name;
		up.createdAt = up.updatedAt = ts;
		db.insert(up);
		// insert password auth:
		var pa = new PasswordAuthEntity();
		pa.userId = user.id;
		pa.random = RandomUtil.createRandomString(32);
		pa.passwd = HashUtil.hmacSha256(passwd, pa.random);
		db.insert(pa);
		return up;
	}

	public UserProfileEntity signin(String email, String passwd) {
		UserProfileEntity userProfile = getUserProfileByEmail(email);
		// find PasswordAuthEntity by user id:
		PasswordAuthEntity pa = db.fetch(PasswordAuthEntity.class, userProfile.userId);
		if (pa == null) {
			throw new ApiException(ApiError.USER_CANNOT_SIGNIN);
		}
		// check password hash:
		String hash = HashUtil.hmacSha256(passwd, pa.random);
		if (!hash.equals(pa.passwd)) {
			throw new ApiException(ApiError.AUTH_SIGNIN_FAILED);
		}
		return userProfile;
	}
}
