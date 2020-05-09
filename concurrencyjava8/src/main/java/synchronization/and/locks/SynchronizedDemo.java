package synchronization.and.locks;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static util.ConcurrentUtils.stop;


public class SynchronizedDemo
{
    int count = 0;


    public static void main(String [] args)
    {
        new SynchronizedDemo().problemDemo();
        new SynchronizedDemo().solutionDemo();
    }

    private void increment() {
        count = count + 1;
    }


    private synchronized void incrementSync() {
        count = count + 1;
    }
    /*
    The synchronized keyword is also available as a block statement.
    void incrementSync() {
        synchronized (this) {
            count = count + 1;
        }
    }

    Internally Java uses a so called monitor also known as monitor lock or intrinsic lock
    in order to manage synchronization. This monitor is bound to an object eg. when using
    synchronized methods each method share the same monitor of the corresponding object.
    All implicit monitors implement the reentrant characteristics. Reentrant means that locks
    are bound to the current thread. A thread can safely acquire the same lock multiple times
    without running into deadlocks. (eg a synchronized method calls another synchronized method
    on the same object).
     */

    public void problemDemo()
    {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        IntStream.range(0, 10000)
                .forEach(i -> executor.submit(this::increment));

        stop(executor);

        System.out.println(count); //The reuslt always varies because of race condition
        /* For this to work properly
        1. read the current value
        2. increate this value by one
        3. write then new value to the variable
         */
    }


    public void solutionDemo()
    {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        IntStream.range(0, 10000)
                .forEach(i -> executor.submit(this::incrementSync));

        stop(executor);

        System.out.println(count);
    }





}
