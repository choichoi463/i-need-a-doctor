package org.example.utils.listener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class Sensor {
    private final AtomicBoolean value = new AtomicBoolean(false);
    private final List<Consumer<Boolean>> listeners = new CopyOnWriteArrayList<>();

    public void addListener(Consumer<Boolean> listener) {
        listeners.add(listener);
    }

    // "Pull": anyone can read the current value at any time
    public boolean getValue() {
        return value.get();
    }

    // "Push": update the value and notify listeners only if it changed
    public void setValue(boolean newValue) {
        boolean oldValue = value.getAndSet(newValue);
        if (oldValue != newValue) {
            for (Consumer<Boolean> l : listeners) {
                l.accept(newValue);
            }
        }
    }
}
