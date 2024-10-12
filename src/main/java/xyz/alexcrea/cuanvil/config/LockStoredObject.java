package xyz.alexcrea.cuanvil.config;

import java.util.concurrent.locks.StampedLock;

/**
 * Bad implementation to acquire an object in a thread safe way
 * Made for read heavy case
 * @param <T> The type of object to store
 */
public class LockStoredObject<T> {

    private T stored;
    protected final StampedLock lock;

    LockStoredObject(T toStore){
        this.stored = toStore;
        this.lock = new StampedLock();
    }

    public T get(){
        long stamp = this.lock.tryOptimisticRead();
        T stored = this.stored;
        if (this.lock.validate(stamp))
            return stored;

        stamp = this.lock.readLock();
        try {stored = this.stored;}
        finally {this.lock.unlockRead(stamp);}
        return stored;
    }

    private long writeStamp;
    public final T acquiredWrite(){
        writeStamp = lock.writeLock();
        return stored;
    }

    public final void releaseWrite(){
        lock.unlockWrite(this.writeStamp);
    }

    public void isWriteLocked(){
        if(!lock.isWriteLocked()){
            throw new IllegalStateException("Lock is not write locked");
        }
    }

    public final void setStored(T toStore){
        isWriteLocked();
        this.stored = toStore;
    }

    public T getWhileWrite(){
        isWriteLocked();
        return stored;
    }

    public T unsafeGet(){
        return stored;
    }

}
