package xyz.alexcrea.cuanvil.util;

import org.jetbrains.annotations.NotNullByDefault;
import xyz.alexcrea.cuanvil.dependency.util.PlatformUtil;

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

    private static boolean APPLY_LOCK() {
        var status = System.getenv("CUSTOMANVIL_LOCK_STATUS");
        if("false".equalsIgnoreCase(status))
            return false;

        if("true".equalsIgnoreCase(status))
            return true;

        return PlatformUtil.INSTANCE.isFolia();
    }

    public static class LockedRead<T> implements AutoCloseable {

        private final LockedObjectProvider<T> parent;

        public LockedRead(LockedObjectProvider<T> parent) {
            this.parent = parent;
        }

        public T get() {
            lock();
            return parent.unsafe();
        }

        public void lock() {
            if(APPLY_LOCK()) this.parent.lock.readLock().lock();
        }

        @Override
        public void close() {
            if(APPLY_LOCK()) parent.lock.readLock().unlock();
        }
    }

    public static class LockedWrite<T> implements AutoCloseable {

        private final LockedObjectProvider<T> parent;

        public LockedWrite(LockedObjectProvider<T> parent) {
            this.parent = parent;
        }

        public T get() {
            lock();
            return parent.unsafe();
        }

        public void lock() {
            if(APPLY_LOCK()) this.parent.lock.writeLock().lock();
        }

        @Override
        public void close() {
            if(APPLY_LOCK()) parent.lock.writeLock().unlock();
        }
    }

    private T unsafe() {
        return provider.get();
    }

}
