package indi.yume.tools.fragmentmanager;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import lombok.experimental.UtilityClass;

/**
 * Created by yume on 16-8-30.
 */
@UtilityClass
public class ThrottleUtil {
    private static final AtomicLong throttleTime = new AtomicLong(0);
    private static final AtomicLong lastEventTime = new AtomicLong(0);

    static boolean checkEvent() {
        return checkEvent(true);
    }

    static boolean checkEvent(boolean saveCurrentTime) {
        long currentTime = System.currentTimeMillis();
        long throttle = throttleTime.get();

        if(throttle == 0)
            return true;

        if(currentTime - lastEventTime.get() > throttle) {
            if(saveCurrentTime)
                lastEventTime.set(System.currentTimeMillis());
            return true;
        }
        return false;
    }

    public static void setThrottleTime(long time) {
        throttleTime.set(Math.max(0, time));
    }

    public static void setThrottleTime(long time, TimeUnit unit) {
        setThrottleTime(unit.toMillis(time));
    }
}
