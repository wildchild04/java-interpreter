package interpreter.environment;

import interpreter.monkey.objects.MonkeyObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class Environment {

    private Map<String, MonkeyObject> store = new HashMap<>();
    private Environment outer;

    public Environment() {
        this.outer = null;
    }

    public Environment(Environment outer) {
        this.outer = outer;
    }

    public Optional<MonkeyObject> get(String name) {

        MonkeyObject res = store.get(name);

        if (res == null && outer != null) {
            res = outer.get(name).orElse(null);
        }

        return Optional.ofNullable(res);
    }

    public Optional<MonkeyObject> set(String name, MonkeyObject object) {
        return Optional.ofNullable(store.put(name, object));
    }


}
