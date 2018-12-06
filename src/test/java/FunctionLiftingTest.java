import io.vavr.Function1;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;

/**
 * Created by mtumilowicz on 2018-12-06.
 */
public class FunctionLiftingTest {

    @Test
    public void lift() {
        ActiveUserRepository activeUserRepository = new ActiveUserRepository();

        Stream.of(InactiveUser.builder().id(1).banDate(LocalDate.parse("2014-10-12")).warn(15).build(),
                InactiveUser.builder().id(2).banDate(LocalDate.parse("2016-10-12")).warn(0).build())
                .map(Function1.lift(x -> x.activate(Clock.fixed(Instant.parse("2016-12-03T10:15:30Z"), ZoneId.systemDefault()))))
                .forEach(option -> option.peek(activeUserRepository::add));
        
        assertTrue(activeUserRepository.existsAll(List.of(2)));
    }
    
    @Test
    public void liftTry() {
        Stream.of(InactiveUser.builder().id(1).banDate(LocalDate.parse("2014-10-12")).warn(15).build())
                .map(Function1.liftTry(x -> x.activate(Clock.fixed(Instant.parse("2016-12-03T10:15:30Z"), ZoneId.systemDefault()))))
                .forEach(System.out::println);
    }
}
