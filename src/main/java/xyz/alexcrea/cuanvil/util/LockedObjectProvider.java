package xyz.alexcrea.cuanvil.util;

import io.delilaheve.CustomAnvil;
import org.jetbrains.annotations.NotNullByDefault;
import xyz.alexcrea.cuanvil.dependency.util.PlatformUtil;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import java.util.logging.Level;

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

        return !"false".equalsIgnoreCase(status);
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
            if(!APPLY_LOCK()) return;

            if(this.parent.lock.getReadHoldCount() > 0 && !this.parent.lock.isWriteLockedByCurrentThread()) {
                CustomAnvil.logError(
                        "Error while acquiring write lock: read lock already acquired... for configuration " + parent.unsafe().getClass(),
                        null,
                        true,
                        Level.SEVERE
                );
                return;
            }

            this.parent.lock.writeLock().lock();
        }

        @Override
        public void close() {
            if(!APPLY_LOCK()) return;
            // can be skipped if there was an issue
            if(!this.parent.lock.isWriteLockedByCurrentThread()) return;

            parent.lock.writeLock().unlock();
        }
    }

    private T unsafe() {
        return provider.get();
    }

}
