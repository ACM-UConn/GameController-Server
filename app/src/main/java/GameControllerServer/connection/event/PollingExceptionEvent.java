package GameControllerServer.connection.event;

import co.m1ke.basic.events.interfaces.Event;

import java.lang.annotation.Annotation;

public class PollingExceptionEvent implements Event {

    private Throwable cause;
    private long time;

    public PollingExceptionEvent(Throwable cause, long time) {
        this.cause = cause;
        this.time = time;
    }

    @Override
    public int priority() {
        return 50;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return null;
    }

    public Throwable getThrowable() {
        return cause;
    }

    public Exception asException() {
        return new Exception(cause);
    }

    public long getTime() {
        return time;
    }
}