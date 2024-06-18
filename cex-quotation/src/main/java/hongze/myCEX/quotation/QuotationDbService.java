package hongze.myCEX.quotation;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import hongze.myCEX.model.quotation.DayBarEntity;
import hongze.myCEX.model.quotation.HourBarEntity;
import hongze.myCEX.model.quotation.MinBarEntity;
import hongze.myCEX.model.quotation.SecBarEntity;
import hongze.myCEX.model.quotation.TickEntity;
import hongze.myCEX.support.AbstractDbService;

@Component
@Transactional
public class QuotationDbService extends AbstractDbService {

    public void saveBars(SecBarEntity sec, MinBarEntity min, HourBarEntity hour, DayBarEntity day) {
        if (sec != null) {
            this.db.insertIgnore(sec);
        }
        if (min != null) {
            this.db.insertIgnore(min);
        }
        if (hour != null) {
            this.db.insertIgnore(hour);
        }
        if (day != null) {
            this.db.insertIgnore(day);
        }
    }

    public void saveTicks(List<TickEntity> ticks) {
        this.db.insertIgnore(ticks);
    }
}
