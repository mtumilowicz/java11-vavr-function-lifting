# java11-vavr-function-lifting
Examples of vavr function lifting.

_Reference_: https://www.vavr.io/vavr-docs/#_lifting

# preface
A partial function from `X` to `Y` is a function `f: X′ → Y`, 
for some `X′ c X`.

We lift function `f` to `f′: X -> Y` in such a manner:
* `f′ == f` on `X′`
* `f′(x) = Option.none()` for `x e X\X′`

# vavr
In vavr we have two approaches to lifting:
* lifting function to another function (described above)
    ```
    Function2<Integer, Integer, Integer> divide = (a, b) -> a / b;
    
    Function2<Integer, Integer, Option<Integer>> lifted = Function2.lift(divide);
    ```
* lifting function to Try
    ```
    Function2<Integer, Integer, Integer> divide = (a, b) -> a / b;
    
    Function2<Integer, Integer, Try<Integer>> lifted = Function2.liftTry(divide);
    ```

# project description
1. suppose we have:
    * `BlockedUser` with a method to activate it:
        ```
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
        ```
    * simple `ActiveUser`
        ```
        @Value
        @Builder
        class ActiveUser {
            int id;
        }
        ```
1. suppose we cannot modify class `BlockedUser`
1. we have `Stream` of `BlockedUsers` and 
we want to activate them (if possible) and save 
to the database
    * `activate` throws exceptions, so we `lift` that 
    function (`FunctionLiftingTest - lift()`):
        ```
        //        given
                ActiveUserRepository activeUserRepository = new ActiveUserRepository();
        
                var cannotBeActive = BlockedUser.builder()
                        .id(1)
                        .banDate(LocalDate.parse("2014-10-12"))
                        .warn(15)
                        .build();
        
                var canBeActive = BlockedUser.builder()
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
        ```
1. we have `Stream` of `BlockedUsers` and 
   we want to activate them (if possible) and save 
   to the database or generate report of exceptions
   occurred during activation (`FunctionLiftingTest - liftTry()`)
   ```
   //        given
           ActiveUserRepository activeUserRepository = new ActiveUserRepository();
   
           var cannotBeActive = BlockedUser.builder()
                   .id(1)
                   .banDate(LocalDate.parse("2014-10-12"))
                   .warn(15)
                   .build();
   
           var canBeActive = BlockedUser.builder()
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
   ```
   