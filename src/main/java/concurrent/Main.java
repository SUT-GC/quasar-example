package concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Main {
    private static ExecutorService executorPool;
    private static int poolSize = 300;
    private static int requestSize = 300;

    private static void init() {
        MyService.DEFAULT_DEPEND_COUNT = 5;
        MyService.disposeType = MyService.DISPOSE_TYPE.COROUTINE;

        OtherService.IS_RANDOM_DELAY = Boolean.FALSE;
        OtherService.DEFAULT_DELAY = 300000;
        OtherService.DEFAULT_TIMEOUT = 300000;
        OtherService.DEFAULT_DELAY_UPPER = 10000;

        poolSize = 10;
        requestSize = 5;
        executorPool = Executors.newFixedThreadPool(poolSize);
    }

    public static void main(String[] args) {
        init();
        int successCount = 0;
        int failCount = 0;
        Long startTime = System.currentTimeMillis();

        final CountDownLatch countDownLatch = new CountDownLatch(requestSize);
        final List<Future<Boolean>> results = new ArrayList<Future<Boolean>>();
        for (int i = 0; i < requestSize; i++) {
            results.add(executorPool.submit(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    boolean result = new MyService().dispose();
                    countDownLatch.countDown();
                    return result;
                }
            }));
        }
        try {
            countDownLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (Future<Boolean> future : results) {
            try {
                if (future.get()) {
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                failCount++;
            }
        }

        Long endTime = System.currentTimeMillis();

        System.out.println(String.format("请求完成, 耗时 %s ms, 请求成功%s次, 失败%s次", (endTime - startTime), successCount, failCount));

        finish();
    }

    private static void finish() {
        executorPool.shutdown();
    }
}
