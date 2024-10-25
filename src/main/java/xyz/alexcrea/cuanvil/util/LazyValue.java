package xyz.alexcrea.cuanvil.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class LazyValue<T> {

    private final Supplier<T> valueSupplier;
    private T storedValue;

    public LazyValue(Supplier<T> valueSupplier) {
        this.valueSupplier = valueSupplier;
        this.storedValue = null;
    }

    @Nullable
    public T getStored(){
        return storedValue;
    }

    @NotNull
    public T get(){
        if (storedValue != null) return storedValue;

        synchronized(this) {
            if(storedValue == null) {
                storedValue = valueSupplier.get();
            }
        }

        return storedValue;
    }

}
