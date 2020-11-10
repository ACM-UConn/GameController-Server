package GameControllerServer.connection.event;

import co.m1ke.basic.events.interfaces.Event;

import java.lang.annotation.Annotation;
import javax.websocket.Session;

public class HeartbeatExceptionEvent implements Event {

    private Session session;
    private String sessionId;
    private long time;

    public HeartbeatExceptionEvent(Session session, String sessionId, long time) {
        this.session = session;
        this.sessionId = sessionId;
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

    public Session getSession() {
        return session;
    }

    public String getSessionId() {
        return sessionId;
    }

    public long getTime() {
        return time;
    }

}