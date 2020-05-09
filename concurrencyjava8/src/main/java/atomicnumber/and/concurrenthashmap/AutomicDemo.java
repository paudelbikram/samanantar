package atomicnumber.and.concurrenthashmap;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.LongBinaryOperator;
import java.util.stream.IntStream;

import static util.ConcurrentUtils.stop;

public class AutomicDemo
{
    public static void main(String[] args)
    {
        /*
        java.concurrent.atomic contains many useful
        classes to perform atomic operations. An operation is atomic
        when you can safely perform the operation in parallel
        on multiple threads without using the synchronized keyword.
        Internally atomic classes make heavy use of compare and swap (CAS).
        It is prefer to use atomic classes over locks.
         */
    }


    public void introToAtomic()
    {
        AtomicInteger atomicInt = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        IntStream.range(0, 1000)
                .forEach(i -> executor.submit(atomicInt::incrementAndGet));

        stop(executor);

        System.out.println(atomicInt.get());    // => 1000
    }


    public void introToUpdateAndGet()
    {
        AtomicInteger atomicInt = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        /*
        updateAndGet() accepts a lambda expression in order to perform
        arbitrary arithmetic operation upon the integer.
         */
        IntStream.range(0, 1000)
                .forEach(i -> {
                    Runnable task = () ->
                            atomicInt.updateAndGet(n -> n + 2);
                    executor.submit(task);
                });

        stop(executor);

        System.out.println(atomicInt.get());    // => 2000
    }


    public void introToAccumulateAndGet()
    {
        /*
        AccumulateAndGet() accepts another kind of lambda expression
        of type IntBinaryOperator
         */
        AtomicInteger atomicInt = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        IntStream.range(0, 1000)
                .forEach(i -> {
                    Runnable task = () ->
                            atomicInt.accumulateAndGet(i, (n, m) -> n + m);
                    executor.submit(task);
                });

        stop(executor);

        System.out.println(atomicInt.get());    // => 499500
    }


    public void introToLongAdded()
    {
        /*
        Instead of summing up a single result, this class maintains a set
        of variables internally to reduce contention over threads.
        The actual result can be retrieved by calling sum() or sumThenReset().
        This class is prefer over atomic int when updates from multiple threads
        are more common than reads.The drawback of LongAdder is higher memory
        consumption because of set of variables held in memory.
         */
        LongAdder adder = new LongAdder();
        ExecutorService executor = Executors.newFixedThreadPool(2);

        IntStream.range(0, 1000)
                .forEach(i -> executor.submit(adder::increment));

        stop(executor);

        System.out.println(adder.sumThenReset());   // => 1000
    }


    public void introToLongAccumulator()
    {
        /*
        LongAccumulator is more generalized version of LongAdded. It
        is builds around a lambda expression of type LongBinaryOperator.

         */
        LongBinaryOperator op = (x, y) -> 2 * x + y;
        LongAccumulator accumulator = new LongAccumulator(op, 1L);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        IntStream.range(0, 10)
                .forEach(i -> executor.submit(() -> accumulator.accumulate(i)));

        stop(executor);

        System.out.println(accumulator.getThenReset());     // => 2539
    }


    public void introToConcurrentMap()
    {
        ConcurrentMap<String, String> map = new ConcurrentHashMap<>();
        map.put("foo", "bar");
        map.put("han", "solo");
        map.put("r2", "d2");
        map.put("c3", "p0");
        map.forEach((key, value) -> System.out.printf("%s = %s\n", key, value));
        String value = map.putIfAbsent("c3", "p1");
        System.out.println(value);    // p0

        String value1 = map.getOrDefault("hi", "there");
        System.out.println(value1);    // there

        map.replaceAll((key, value3) -> "r2".equals(key) ? "d3" : value3);
        System.out.println(map.get("r2"));    // d3
        //there is also computeIfAbsent() and computeIfPresent()
        map.compute("foo", (key, value4) -> value4 + value4);
        System.out.println(map.get("foo"));   // barbar

        map.merge("foo", "boo", (oldVal, newVal) -> newVal + " was " + oldVal);
        System.out.println(map.get("foo"));   // boo was foo

        /*
        ConcurrentHashMap has been further enhanced with a couple of new methods
        to perform parallel operation upon the map.
        Just like parallel streams those methods use a special ForkJoinPool available via
        ForkJoinPool.commonPool() in Java 8. This pool uses a preset parallelism
        which depends on the number of available cores.
        Four CPU cores machine results in a parallelism of three.
        This value can be decreased or increased by setting the following
        JVM parameter.

        -Djava.util.concurrent.ForkJoinPool.common.parallelism=5

         */
        System.out.println(ForkJoinPool.getCommonPoolParallelism());  // 3
    }



    public void introToParallelism()
    {
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
        map.put("foo", "bar");
        map.put("han", "solo");
        map.put("r2", "d2");
        map.put("c3", "p0");
        /*
        JAVA 8 introduces three kinds of parallel operations:
        forEach, search, and reduce. Each of those operations are
        available in four forms acceptiong functions with keys, values,
        entries and key-value pair arguments.
        All of those above use a common first argument called parallelismThreshold.
        This threshold indicates the minimum collection size when the operation should
        be executed in parallel. Eg if you pass a threashold of 500 and the actual size of
        the map is 499, the operation will be performed sequentially on a single thread.
         ConcurrentHashMap is unordered.
         */

        map.forEach(1, (key, value) ->
                System.out.printf("key: %s; value: %s; thread: %s\n",
                        key, value, Thread.currentThread().getName()));

        /*
        If multiple entries of the map match the given search function the result
        may be non deterministic.
         */
        String result = map.search(1, (key, value) -> {
            System.out.println(Thread.currentThread().getName());
            if ("foo".equals(key)) {
                return value;
            }
            return null;
        });
        System.out.println("Result: " + result);


        String result1 = map.searchValues(1, value5 -> {
            System.out.println(Thread.currentThread().getName());
            if (value5.length() > 3) {
                return value5;
            }
            return null;
        });

        System.out.println("Result: " + result1);

    }


    public void introToReduce()
    {
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
        map.put("foo", "bar");
        map.put("han", "solo");
        map.put("r2", "d2");
        map.put("c3", "p0");
        String result = map.reduce(1,
                (key, value) -> {
                    System.out.println("Transform: " + Thread.currentThread().getName());
                    return key + "=" + value;
                },
                (s1, s2) -> {
                    System.out.println("Reduce: " + Thread.currentThread().getName());
                    return s1 + ", " + s2;
                });

        System.out.println("Result: " + result);
    }





}


