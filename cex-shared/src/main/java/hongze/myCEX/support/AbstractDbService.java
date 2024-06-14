package hongze.myCEX.support;

import org.springframework.beans.factory.annotation.Autowired;

import hongze.myCEX.db.DbTemplate;

/**
 * Service with db support.
 */
public abstract class AbstractDbService extends LoggerSupport {

    @Autowired
    protected DbTemplate db;
}
