package indi.yume.tools.fragmentmanager;

/**
 * Created by yume on 16-8-30.
 */

public class ThrottleException extends RuntimeException {
    public ThrottleException() {
        super("Call func is too fast, this event will be ignored.");
    }
}
