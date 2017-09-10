package concurrent;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.SuspendableCallable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class MyService {
    public static int DEFAULT_DEPEND_COUNT = 3; // 默认依赖其他服务调用次数
    public static DISPOSE_TYPE disposeType = DISPOSE_TYPE.SEQUENCE;
    private int dependCount = DEFAULT_DEPEND_COUNT;

    public enum DISPOSE_TYPE {MULTI_THREAD, FORKJOIN, COROUTINE, SEQUENCE}

    public MyService() {
    }

    public MyService(int dependCount) {
        if (dependCount < 0) {
            this.dependCount = DEFAULT_DEPEND_COUNT;
        }
    }

    public boolean dispose() {
        switch (disposeType) {
            case MULTI_THREAD:
                return disposeByExecutor();
            case COROUTINE:
                return disposeByCoroutine();
            case FORKJOIN:
                return disposeByForkJoin();
            default:
                return disposeBySequence();
        }
    }

    private boolean disposeByForkJoin() {
        boolean result = false;

        List<Future<Boolean>> futures = new ArrayList<Future<Boolean>>(dependCount);
        //forkjoin 线程池 带work-stealing
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        final CountDownLatch countDownLatch = new CountDownLatch(dependCount);

        for (int i = 0; i < dependCount; i++) {
            futures.add(forkJoinPool.submit(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    OtherService otherService = new OtherService();
                    boolean result = otherService.dispose();
                    countDownLatch.countDown();
                    return result;
                }
            }));
        }

        try {
            countDownLatch.await();
            result = Boolean.TRUE;
            for (Future<Boolean> future : futures) {
                result &= future.get();
            }
        } catch (Exception e) {
            result = Boolean.FALSE;
        }

        forkJoinPool.shutdown();
        return result;
    }

    private boolean disposeByCoroutine() {
        boolean result = false;

        List<Fiber<Boolean>> fibers = new ArrayList<Fiber<Boolean>>();
        for (int i = 0; i < dependCount; i++) {
            fibers.add(new Fiber<Boolean>(new SuspendableCallable<Boolean>() {
                public Boolean run() throws SuspendExecution, InterruptedException {
                    OtherService otherService = new OtherService();
                    return otherService.dispose();
                }
            }).start());
        }

        result = Boolean.TRUE;
        try {
            for (Fiber<Boolean> fiber : fibers) {
                result &= fiber.get();
            }
        } catch (Exception e) {
            result = Boolean.FALSE;
        }

        return result;
    }

    private boolean disposeByExecutor() {
        boolean result = false;

        List<Future<Boolean>> futures = new ArrayList<Future<Boolean>>(dependCount);
        // 普通executor线程池 不带work-stealing
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        final CountDownLatch countDownLatch = new CountDownLatch(dependCount);

        for (int i = 0; i < dependCount; i++) {
            futures.add(executorService.submit(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    OtherService otherService = new OtherService();
                    boolean result = otherService.dispose();
                    countDownLatch.countDown();
                    return result;
                }
            }));
        }

        try {
            countDownLatch.await();
            result = Boolean.TRUE;
            for (Future<Boolean> future : futures) {
                result &= future.get();
            }
        } catch (Exception e) {
            result = Boolean.FALSE;
        }

        executorService.shutdown();

        return result;
    }

    private boolean disposeBySequence() {
        boolean result = false;

        OtherService otherService = new OtherService();

        try {
            for (int i = 0; i < dependCount; i++) {
                // 调用其他服务，300ms的延迟
                result = otherService.dispose();

                if (!result) {
                    break;
                }
            }
        } catch (Exception e) {
            result = Boolean.FALSE;
        }


        return result;
    }
}
