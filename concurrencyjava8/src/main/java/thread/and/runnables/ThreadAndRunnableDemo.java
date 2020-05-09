package thread.and.runnables;

import java.util.concurrent.TimeUnit;

public class ThreadAndRunnableDemo
{
    public static void main(String[] args)
    {
        //Creating Runnable task
        Runnable task = () ->{
            String threadName = Thread.currentThread().getName();
            System.out.println("Hellow "+ threadName);
        };
        //Running Task in main thread without using Thread
        task.run();

        //Using Thread
        Thread thread = new Thread(task);
        //Starting Thread
        thread.start();

        System.out.println("DONE!");

        threadSleepDemo();
    }

    public static void threadSleepDemo()
    {
        Runnable runnable = () -> {
            try {
                String name = Thread.currentThread().getName();
                System.out.println("Foo " + name);
                TimeUnit.SECONDS.sleep(1); //Thread.sleep(1000);
                System.out.println("Bar " + name);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
    }

}
