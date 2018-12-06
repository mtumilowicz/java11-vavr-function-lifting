import java.util.Collection;
import java.util.HashMap;

/**
 * Created by mtumilowicz on 2018-12-06.
 */
class ActiveUserRepository {
    private final HashMap<Integer, ActiveUser> storage = new HashMap<>();

    void add(ActiveUser user) {
        storage.put(user.getId(), user);
    }

    void existsAll(Collection<Integer> ids) {
        storage.keySet().containsAll(ids);
    }
}
