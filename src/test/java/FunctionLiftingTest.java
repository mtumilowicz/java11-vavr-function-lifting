import io.vavr.Function1;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertTrue;

/**
 * Created by mtumilowicz on 2018-12-06.
 */
public class FunctionLiftingTest {

    @Test
    public void lift() {
//        given
        ActiveUserRepository activeUserRepository = new ActiveUserRepository();

        var cannotBeActive = InactiveUser.builder()
                .id(1)
                .banDate(LocalDate.parse("2014-10-12"))
                .warn(15)
                .build();

        var canBeActive = InactiveUser.builder()
                .id(2)
                .banDate(LocalDate.parse("2016-10-12"))
                .warn(0)
                .build();

//        when
        Stream.of(cannotBeActive, canBeActive)
                .map(Function1.lift(x -> x.activate(
                        Clock.fixed(Instant.parse("2016-12-03T10:15:30Z"), ZoneId.systemDefault())
                )))
                .forEach(option -> option.peek(activeUserRepository::add));

//        then
        assertTrue(activeUserRepository.existsAll(List.of(2)));
    }

    @Test
    public void liftTry() {
//        given
        ActiveUserRepository activeUserRepository = new ActiveUserRepository();

        var cannotBeActive = InactiveUser.builder()
                .id(1)
                .banDate(LocalDate.parse("2014-10-12"))
                .warn(15)
                .build();

        var canBeActive = InactiveUser.builder()
                .id(2)
                .banDate(LocalDate.parse("2016-10-12"))
                .warn(0)
                .build();

        List<String> fails = new LinkedList<>();

//        when
        Stream.of(cannotBeActive, canBeActive)
                .map(Function1.liftTry(x -> x.activate(Clock.fixed(Instant.parse("2016-12-03T10:15:30Z"), ZoneId.systemDefault()))))
                .forEach(tryF -> tryF
                        .onSuccess(activeUserRepository::add)
                        .onFailure(exception -> fails.add(exception.getMessage())));

//        then
        assertThat(activeUserRepository.count(), is(1));
        assertTrue(activeUserRepository.existsAll(List.of(2)));

//        and
        assertThat(fails, hasSize(1));
        assertThat(fails.get(0), is("id = 1: warns has to be <= 10"));
    }
}
