package synchronization.and.locks;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.IntStream;

import static util.ConcurrentUtils.sleep;
import static util.ConcurrentUtils.stop;

public class LocksDemo
{
    /*
    ReentrantLock is a mutual exclusion lock with the same
    basic behavior as implicit monitors accessed via the
    synchronized keyword but with extended capabilities.

     */

    ReentrantLock lock = new ReentrantLock();
    int count = 0;

    void increment()
    {
        lock.lock();
        try
        {
            count++;
        }
        //it is important to wrap it in try/finally block to ensure unlocking in case of exceptions.
        finally {
            lock.unlock();
        }
    }


    public static void main(String [] args)
    {

    }


    public void reentrantlockDemo()
    {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        ReentrantLock lock = new ReentrantLock();

        executor.submit(() -> {
            lock.lock();
            try {
                sleep(1);
            } finally {
                lock.unlock();
            }
        });

        executor.submit(() -> {
            System.out.println("Locked: " + lock.isLocked());
            System.out.println("Held by me: " + lock.isHeldByCurrentThread());
            boolean locked = lock.tryLock();//tryLock() is an alternative to lock() tries to acquire the lock without pausing the current thread.
            System.out.println("Lock acquired: " + locked);
        });

        stop(executor);
    }


    public void readWriteLockDemo()
    {
        /*
        The interface ReadWriteLock specifies another type of lock maintaining a pair of locks for read
        and write access. The idea behind read-write locks is that it's usually safe to read mutable
        variables concurrenlty as long as nobody is writing to this variable. So, the read lock can be held
        simultaneously by multiple threads as long as no threads hold the write lock. This can improve
        performance and thoughput in case that reads are more frequent than writes.
         */
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Map<String, String> map = new HashMap<>();
        ReadWriteLock lock = new ReentrantReadWriteLock();

        executor.submit(() -> {
            lock.writeLock().lock();
            try {
                sleep(1);
                map.put("foo", "bar");
            } finally {
                lock.writeLock().unlock();
            }
        });

        Runnable readTask = () -> {
            lock.readLock().lock();
            try {
                System.out.println(map.get("foo"));
                sleep(1);
            } finally {
                lock.readLock().unlock();
            }
        };

        executor.submit(readTask);
        executor.submit(readTask);

        stop(executor);
    }


    public void introToStampedLock()
    {
        /*
        JAVA 8 ships with a new kind of lock called StampedLock which also support read
        and write locks. StampedLock return a stamp represented by a long value. You can use
        stamps to either release a lock or to check if lock is still valid. Stamped also
        support another lock called optimistic locking.
         */
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Map<String, String> map = new HashMap<>();
        StampedLock lock = new StampedLock();

        executor.submit(() -> {
            long stamp = lock.writeLock();
            try {
                sleep(1);
                map.put("foo", "bar");
            } finally {
                lock.unlockWrite(stamp);
            }
        });

        Runnable readTask = () -> {
            long stamp = lock.readLock();
            try {
                System.out.println(map.get("foo"));
                sleep(1);
            } finally {
                lock.unlockRead(stamp);
            }
        };

        executor.submit(readTask);
        executor.submit(readTask);

        stop(executor);
        /*
        Stamped lock don't implement reentrant characteristics.
        Each call to lock returns a new stamp and blocks if no lock
        is available even if the same thread already holds a lock.
        So, you have to pay particular attention not ot run into deadlocks.
         */
    }


    public void introToOptimisticLock()
    {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        StampedLock lock = new StampedLock();

        executor.submit(() -> {
            long stamp = lock.tryOptimisticRead();
            try {
                System.out.println("Optimistic Lock Valid: " + lock.validate(stamp));
                sleep(1);
                System.out.println("Optimistic Lock Valid: " + lock.validate(stamp));
                sleep(2);
                System.out.println("Optimistic Lock Valid: " + lock.validate(stamp));
            } finally {
                lock.unlock(stamp);
            }
        });

        executor.submit(() -> {
            long stamp = lock.writeLock();
            try {
                System.out.println("Write Lock acquired");
                sleep(2);
            } finally {
                lock.unlock(stamp);
                System.out.println("Write done");
            }
        });

        stop(executor);
        /*
        An optimistic read lock is acquired by calling tryOptimisticRead()
        which always returns a stamp without blocking the current thread, no matter
        if the lock is actually available.
         */
    }


    public void introToTryConvertToWriteLock()
    {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        StampedLock lock = new StampedLock();

        executor.submit(() -> {
            long stamp = lock.readLock();
            try {
                if (count == 0) {
                    stamp = lock.tryConvertToWriteLock(stamp);
                    if (stamp == 0L) {
                        System.out.println("Could not convert to write lock");
                        stamp = lock.writeLock();
                    }
                    count = 23;
                }
                System.out.println(count);
            } finally {
                lock.unlock(stamp);
            }
        });

        stop(executor);
    }



    public void introToSemaphores()
    {
        /*
        Semaphores is useful where you have to limit the amount concurrent access
        to certain parts of your application.

        The executor can potentially run 10 tasks concurrently but we use a semaphore
        of size 5, thus limiting concurrent access to 5. It's important to use a try/finally
        block to properly release the semaphore even in case of exceptions.
         */
        ExecutorService executor = Executors.newFixedThreadPool(10);

        Semaphore semaphore = new Semaphore(5);

        Runnable longRunningTask = () -> {
            boolean permit = false;
            try {
                permit = semaphore.tryAcquire(1, TimeUnit.SECONDS);
                if (permit) {
                    System.out.println("Semaphore acquired");
                    sleep(5);
                } else {
                    System.out.println("Could not acquire semaphore");
                }
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            } finally {
                if (permit) {
                    semaphore.release();
                }
            }
        };

        IntStream.range(0, 10)
                .forEach(i -> executor.submit(longRunningTask));

        stop(executor);
    }

}
