package GameControllerServer.connection.event;

import co.m1ke.basic.events.interfaces.Event;

import java.lang.annotation.Annotation;
import javax.websocket.Session;

public class ClientCommunicationEvent implements Event {

    private Session session;
    private String message;
    private long time;

    public ClientCommunicationEvent(Session session, String message, long time) {
        this.session = session;
        this.message = message;
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

    public String getMessage() {
        return message;
    }

    public long getTime() {
        return time;
    }
}