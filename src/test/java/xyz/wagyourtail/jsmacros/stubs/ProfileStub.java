package xyz.wagyourtail.jsmacros.stubs;

import org.slf4j.Logger;
import xyz.wagyourtail.jsmacros.core.Core;
import xyz.wagyourtail.jsmacros.core.config.BaseProfile;

import java.util.concurrent.LinkedBlockingQueue;

public class ProfileStub extends BaseProfile {

    static Thread th;
    static LinkedBlockingQueue<Runnable> runnables = new LinkedBlockingQueue<>();

    static {
        th = new Thread(() -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            while (true) {
                try {
                    runnables.take().run();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        th.setDaemon(true);
        th.start();
    }

    public ProfileStub(Core<?, ?> runner, Logger logger) {
        super(runner, logger);
        joinedThreadStack.add(th);
    }

    @Override
    public void logError(Throwable ex) {
        LOGGER.error("", ex);
    }

    @Override
    public boolean checkJoinedThreadStack() {
        return joinedThreadStack.contains(Thread.currentThread());
    }

}
