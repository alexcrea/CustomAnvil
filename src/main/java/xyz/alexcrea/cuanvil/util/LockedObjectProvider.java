package xyz.alexcrea.cuanvil.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

@NotNullByDefault
public class LockedObjectProvider<T> {

    private final Supplier<T> provider;
    private final ReentrantReadWriteLock lock;

    public final LockedRead<T> read = new LockedRead<>(this);
    public final LockedWrite<T> write = new LockedWrite<>(this);

    public LockedObjectProvider(Supplier<T> provider, ReentrantReadWriteLock lock) {
        this.provider = provider;
        this.lock = lock;
    }

    public static class LockedRead<T> implements AutoCloseable {

        private final LockedObjectProvider<T> parent;

        public LockedRead(LockedObjectProvider<T> parent) {
            this.parent = parent;
        }

        public T get() {
            this.parent.lock.readLock().lock();
            return parent.unsafe();
        }

        @Override
        public void close() {
            parent.lock.readLock().unlock();
        }
    }

    public static class LockedWrite<T> implements AutoCloseable {

        private final LockedObjectProvider<T> parent;

        public LockedWrite(LockedObjectProvider<T> parent) {
            this.parent = parent;
        }

        public T get() {
            this.parent.lock.writeLock().lock();
            return parent.unsafe();
        }

        @Override
        public void close() {
            parent.lock.writeLock().unlock();
        }
    }

    private T unsafe() {
        return provider.get();
    }

}
