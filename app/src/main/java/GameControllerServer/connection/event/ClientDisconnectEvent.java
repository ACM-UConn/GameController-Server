package GameControllerServer.connection.event;

import co.m1ke.basic.events.interfaces.Event;

import java.lang.annotation.Annotation;
import javax.websocket.CloseReason;

public class ClientDisconnectEvent implements Event {

    private CloseReason cause;
    private long time;

    public ClientDisconnectEvent(CloseReason cause, long time) {
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

    public CloseReason getCause() {
        return cause;
    }

    public long getTime() {
        return time;
    }
}