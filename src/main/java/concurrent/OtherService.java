package concurrent;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;

import java.util.Random;

public class OtherService {
    public static int DEFAULT_TIMEOUT = 3000; //默认超时时间 3s
    public static int DEFAULT_DELAY = 300; //默认延迟300ms
    public static int DEFAULT_DELAY_UPPER = 5000; //默认延迟上限 10s
    public static boolean IS_RANDOM_DELAY = Boolean.FALSE; //延迟随机

    @Suspendable
    public boolean dispose() throws InterruptedException, SuspendExecution {
        boolean result = false;

        int delay = DEFAULT_DELAY;

        if (IS_RANDOM_DELAY) {
            Random random = new Random();
            delay = random.nextInt(DEFAULT_DELAY_UPPER);

        }

        if (delay > DEFAULT_TIMEOUT) {
            result = false;
        } else {
            result = true;
        }

        try {
            Strand.sleep(delay);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }

        return result;
    }
}
