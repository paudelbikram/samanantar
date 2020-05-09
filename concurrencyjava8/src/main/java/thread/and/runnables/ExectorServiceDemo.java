package thread.and.runnables;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class ExectorServiceDemo
{
    public static void main (String [] args) throws ExecutionException, InterruptedException {

    callableAndFuture();
}


    public static void callableAndFuture() throws ExecutionException, InterruptedException {
        Callable<Integer> task = () -> {
            try {
                TimeUnit.SECONDS.sleep(1);
                return 123;
            }
            catch (InterruptedException e) {
                throw new IllegalStateException("task interrupted", e);
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(1);//can increase this number to make it configurable.
        Future<Integer> future = executor.submit(task);

        System.out.println("future done? " + future.isDone());//checks if future is done

        Integer result = future.get();//blocks the current thread and waits until the callable completes

        System.out.println("future done? " + future.isDone());
        System.out.print("result: " + result);
        /*
        Futures are tightly coupled to the underlying
        executor service. Keep in mind that every non-terminated future will throw exceptions
        if you shutdown the executor:

        executor.shutdownNow();
        future.get();
         */


    }


    public void introToExecutor()
    {
        ExecutorService executor = Executors.newSingleThreadExecutor(); //Executor with a thread pool of size 1
        executor.submit(()->{
            String threadName = Thread.currentThread().getName();
            System.out.println("Hello "+ threadName);
        });

        //Executors have to be stopped explicitly - otherwise they keep listening for new tasks.
        try {
            System.out.println("attempt to shutdown executor");
            executor.shutdown(); //waits for currently running tasks to finish while shutdownNow() interrupts all running tasks and
            // shut the executor down immediately.
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            System.err.println("tasks interrupted");
        }
        finally {
            if (!executor.isTerminated()) {
                System.err.println("cancel non-finished tasks");
            }
            executor.shutdownNow();
            System.out.println("shutdown finished");
        }
    }


    public void introToTimeout() throws InterruptedException, ExecutionException, TimeoutException {
        ExecutorService executor = Executors.newFixedThreadPool(1);

        Future<Integer> future = executor.submit(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
                return 123;
            }
            catch (InterruptedException e) {
                throw new IllegalStateException("task interrupted", e);
            }
        });

        /*
        The following line throws an exception because we are only waiting for one
        second when we need at least two seconds to return result.
         */

        future.get(1, TimeUnit.SECONDS);
    }


    /*
    Executors supports batch submitting of multiple callable at once via invokeAll().
    This method accepts a collection of callables and returns a list of futures.
     */

    public void introToInvokeAll() throws InterruptedException {
        ExecutorService executor = Executors.newWorkStealingPool();
        /*
        We first map each future to its return value and then print each value to console.
         */
        List<Callable<String>> callables = Arrays.asList(
                () -> "task1",
                () -> "task2",
                () -> "task3");

        executor.invokeAll(callables)
                .stream()
                .map(future -> {
                    try {
                        return future.get();
                    }
                    catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                })
                .forEach(System.out::println);
    }


    /*
    Another way of batch-submitting callables is the method invokeAny().
    This blocks until the first callable terminates and returns the result of
    that callable.
     */
    public void introToInvokeAny() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newWorkStealingPool();
        /*
        newWorkStealingPool is factory method part of JAVA 8 and returns an executor of type
        ForkJoinPool which works slightly different than normal executors. Instead of using a
        fixed size thread-pool ForJoinPools are created for a given parallelism size which per
        default is the number of available cores of the hosts CPU.
         */

        List<Callable<String>> callables = Arrays.asList(
                callable("task1", 2),
                callable("task2", 1),
                callable("task3", 3));

        String result = executor.invokeAny(callables);
        System.out.println(result);
    }


    private Callable<String> callable(String result, long sleepSeconds) {
        return () -> {
            TimeUnit.SECONDS.sleep(sleepSeconds);
            return result;
        };
    }


    public void introToScheduledExecutorService() throws InterruptedException {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        Runnable task = () -> System.out.println("Scheduling: " + System.nanoTime());
        /*
        Scheduling a task produces a specialize future of type ScheduledFuture which in addition to
        Future - provides the method getDelay() to retrieve remaining delay.
         */
        ScheduledFuture<?> future = executor.schedule(task, 3, TimeUnit.SECONDS);

        TimeUnit.MILLISECONDS.sleep(1337);

        long remainingDelay = future.getDelay(TimeUnit.MILLISECONDS);
        System.out.printf("Remaining Delay: %sms", remainingDelay);
    }

    public void introToPeriodicExecutorService()
    {
        /*
        In order to schedule tasks to be executed periodically, executors provide the two methods
        scheduleAtFixedRate() and scheduledWithDelay(). The fist method is capable of executing tasks
        with a fixed time rate. Eg every second.
        Additionally this method accepts an initial delay which describes the leading wait time
        before the task will be executed for the first time.
        scheduleAtFixedRate() does not take into account the actual duration of the task.
        So if you specify a period of one second but the task needs 2 seconds to be executed the
        the thread pool will working to capacity very soon.
        In that case, you should consider using scheduleWithFixedDelay() instead.
        The difference is that the wait time period applies between the end of a task
        and the start of the next task.
        */
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        Runnable task = () -> System.out.println("Scheduling: " + System.nanoTime());

        int initialDelay = 0;
        int period = 1;
        executor.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.SECONDS);

    }

    public void introToScheduleWithFixedDelay()
    {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        Runnable task = () -> {
            try {
                TimeUnit.SECONDS.sleep(2);
                System.out.println("Scheduling: " + System.nanoTime());
            }
            catch (InterruptedException e) {
                System.err.println("task interrupted");
            }
        };

        executor.scheduleWithFixedDelay(task, 0, 1, TimeUnit.SECONDS);
    }



}
