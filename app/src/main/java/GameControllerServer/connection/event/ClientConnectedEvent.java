package GameControllerServer.connection.event;

import co.m1ke.basic.events.interfaces.Event;

import java.lang.annotation.Annotation;
import javax.websocket.Session;

public class ClientConnectedEvent implements Event {

    private Session session;
    private long time;

    public ClientConnectedEvent(Session session, long time) {
        this.session = session;
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

    public long getTime() {
        return time;
    }
}