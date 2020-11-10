package GameControllerServer.connection;

import co.m1ke.basic.events.EventExecutor;

import java.util.concurrent.CountDownLatch;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import GameControllerServer.connection.event.ClientCommunicationEvent;
import GameControllerServer.connection.event.ClientConnectedEvent;
import GameControllerServer.connection.event.ClientDisconnectEvent;
import GameControllerServer.connection.event.PollingExceptionEvent;

@ClientEndpoint
@ServerEndpoint(value = "/events/")
public class ConnectionEndpoint {

    private EventExecutor executor;
    private CountDownLatch latch;

    public ConnectionEndpoint() {
        this.executor = Connection.getExecutor();
        this.latch = new CountDownLatch(1);
    }

    @OnOpen
    public void onConnect(Session session) {
        executor.emit(new ClientConnectedEvent(session, System.currentTimeMillis()));
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        executor.emit(new ClientCommunicationEvent(session, message, System.currentTimeMillis()));
    }

    @OnClose
    public void onDisconnect(CloseReason cause) {
        executor.emit(new ClientDisconnectEvent(cause, System.currentTimeMillis()));
        latch.countDown();
    }

    @OnError
    public void onError(Throwable cause) {
        executor.emit(new PollingExceptionEvent(cause, System.currentTimeMillis()));
    }

}