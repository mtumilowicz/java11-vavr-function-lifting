import lombok.Builder;
import lombok.Value;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Created by mtumilowicz on 2018-12-06.
 */
@Value
@Builder
class BlockedUser {
    int id;
    int warn;
    LocalDate banDate;
    
    ActiveUser activate(Clock clock) {
        if (warn > 10) {
            throw new BusinessException("id = " + id + ": warns has to be <= 10");
        }
        if (ChronoUnit.DAYS.between(banDate, LocalDate.now(clock)) < 14) {
            throw new BusinessException("id = " + id + "minimum ban time is 14 days!");
        }
        return ActiveUser.builder().id(id).build();
    }
}
