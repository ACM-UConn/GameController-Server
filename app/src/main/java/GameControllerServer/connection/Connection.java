package GameControllerServer.connection;

import co.m1ke.basic.events.EventExecutor;
import co.m1ke.basic.events.EventManager;
import co.m1ke.basic.events.interfaces.Event;
import co.m1ke.basic.events.listener.Listener;
import co.m1ke.basic.logger.Logger;
import co.m1ke.basic.utils.TimeUtil;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import javax.websocket.CloseReason;
import javax.websocket.Session;

import GameControllerServer.connection.event.ClientCommunicationEvent;
import GameControllerServer.connection.event.ClientConnectedEvent;
import GameControllerServer.connection.event.ClientDisconnectEvent;
import GameControllerServer.connection.event.HeartbeatExceptionEvent;
import GameControllerServer.connection.event.PollingExceptionEvent;
import com.google.common.collect.ImmutableMap;

/**
 * Websocket Handler for UConn ACM <a href="https://github.com/ACM-UConn/GameController-Server">Game Controller Project</a>
 *
 * @author Mike Medved
 * @date November 8th, 2020
 */
public class Connection extends Listener {

    private Server server;
    private ServerConnector connector;
    private EventManager eventManager;
    private Runnable heartbeat;
    private Logger logger;
    private int port;
    private boolean debug;
    private long heartbeatInterval;

    private ConcurrentMap<String, Session> sessions;
    private ScheduledFuture<?> heartbeatTask;
    private static EventExecutor executor;

    private final String SERVER_SESSION_ID = getRandomSessionId();
    private final ByteBuffer HEARTBEAT = ByteBuffer.wrap(SERVER_SESSION_ID.getBytes());

    public Connection(boolean debug, EventManager eventManager) {
        super("Discovery", eventManager);

        this.port = 7070;
        this.debug = debug;
        this.heartbeatInterval = 5L;
        this.server = new Server();
        this.connector = new ServerConnector(server);
        this.logger = new Logger("Discovery");
        this.eventManager = this.getManager();
        this.sessions = new ConcurrentHashMap<>();
        this.heartbeat = () -> this.sessions.forEach((id, session) -> {
            try {
                session.getAsyncRemote().sendPing(HEARTBEAT);
                logger.debug("Heartbeat signal sent to " + id, debug);
            } catch (IOException ex) {
                logger.except(ex, "Error sending heartbeat to Client " + id);
                executor.emit(new HeartbeatExceptionEvent(session, id, System.currentTimeMillis()));
            }
        });

        ScheduledExecutorService service = Executors.newScheduledThreadPool(4);
        this.heartbeatTask = service.scheduleAtFixedRate(heartbeat,
                heartbeatInterval, heartbeatInterval, TimeUnit.SECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                sessions.forEach((id, session) -> {
                    try {
                        session.getAsyncRemote().sendText("shutdown");
                        session.close(new CloseReason(CloseReason.CloseCodes.getCloseCode(1001),
                                "Service Shutdown"));
                    } catch (IOException ex) {
                        logger.except(ex, "Error sending shutdown signal to " + id);
                    }
                });

                sessions.clear();
                executor.unregisterAll();
                heartbeatTask.cancel(true);
                connector.close();
                server.stop();
            } catch (Exception ex) {
                logger.except(ex, "Error completing shutdown routine");
            }
        }));

        executor = eventManager.getEventExecutor();
        connector.setPort(port);
        server.addConnector(connector);
        executor.registerListener(this);

        ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextHandler.setContextPath("/");
        server.setHandler(contextHandler);

        logger.info("Deployment Settings:");
        logger.info(" - Debug Mode: " + debug);
        logger.info(" - WebSocket Port: " + port);
        logger.info(" - Heartbeat Interval: " + heartbeatInterval + " second" + (heartbeatInterval == 1 ? "" : "s"));

        try {
            WebSocketServerContainerInitializer.configure(contextHandler,
                    ((servletContext, ws) -> {
                        ws.setDefaultMaxTextMessageBufferSize(65535);
                        ws.addEndpoint(ConnectionEndpoint.class);
                    }));

            server.start();
        } catch (Throwable e) {
            logger.except(new Exception(e), "Exception while initializing WebSocket server");
        }
    }

    @Override
    public void init() {
        this.registerSelf();
    }

    @Event
    public void onConnect(ClientConnectedEvent event) {
        Session session = event.getSession();
        String sessionId = getRandomSessionId();
        sessions.put(sessionId, session);
        logger.info("Client with ID " + sessionId + " connected.", debug);
    }

    @Event
    public void onDisconnect(ClientDisconnectEvent event) {
        CloseReason cause = event.getCause();
        Map.Entry<String, Session> dead = sessions
                .entrySet()
                .stream()
                .filter(v -> !v
                        .getValue()
                        .isOpen())
                .findFirst()
                .orElse(null);

        int opcode = cause.getCloseCode().getCode();
        String cleanCause = cleanDisconnectCause(cause);

        if (dead == null) {
            logger.warning("Client with unknown identifier disconnected due to [Opcode " + opcode + " - " + cleanCause + "]", debug);
            return;
        }

        this.removeIf(sessions, (id, session) -> id.equals(dead.getKey())
                && session.getId().equals(dead.getValue().getId()));

        logger.info("Client " + dead.getKey() + " disconnected due to [Opcode " + opcode + " - " + cleanCause + "]", debug);
    }

    @Event
    public void onMessage(ClientCommunicationEvent event) {
        Session session = event.getSession();
        String sessionId = getIdForSession(session);
        String message = event.getMessage();
        long time = event.getTime();

        logger.info(sessionId + " communicated \"" + message + "\" at " + TimeUtil.format(TimeUtil.getDateFormat(), time) + ".", debug);
    }

    @Event
    public void onHeartbeatException(HeartbeatExceptionEvent event) {}

    @Event
    public void onPollException(PollingExceptionEvent event) {
        logger.except(event.asException(), "Exception while polling sockets");
    }

    /**
     * Retrieves the websocket connection string for QR code
     * generation in {@link GameControllerServer.Interface}.
     * @return the websocket connection string
     */
    public String getConnectionUrl() {
        try {
            return "ws://" + InetAddress.getLocalHost().getHostAddress() + ":" + port + "/events/";
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns a mutable map of active websocket sessions.
     * @return a map of active sessions.
     */
    public Map<String, Session> getActiveSessions() {
        Map<String, Session> active = new HashMap<>();
        sessions
                .entrySet()
                .stream()
                .filter(ent -> ent
                        .getValue()
                        .isOpen())
                .forEach(session -> active.put(session.getKey(), session.getValue()));

        return active;
    }

    /**
     * Returns the internal ID for a given session object.
     * @param session the session object
     * @return the internal ID (if found)
     */
    public String getIdForSession(Session session) {
        Map<String, Session> sessions = getActiveSessions();
        Map.Entry<String, Session> entry = sessions
                .entrySet()
                .stream()
                .filter(s -> s
                        .getValue()
                        .getId()
                        .equals(session.getId()))
                .findFirst()
                .orElse(null);

        if (entry == null) {
            return null;
        }

        return entry.getKey();
    }

    /**
     * Returns an immutable map containing websocket
     * sessions information for connected clients.
     *
     * @return an immutable map of sessions.
     */
    public ImmutableMap<String, Session> getSessions() {
        return ImmutableMap.copyOf(sessions);
    }

    /**
     * Creates and returns a random session ID
     * using the first seven characters of a UUID.
     *
     * @return a random session ID.
     */
    public String getRandomSessionId() {
        return UUID
                .randomUUID()
                .toString()
                .substring(0, 6);
    }

    /**
     * Returns an instance of the {@link EventExecutor} for {@link Connection}.
     * @return an instance of {@link EventExecutor}.
     */
    public static EventExecutor getExecutor() {
        return executor;
    }

    /**
     * Removes all items that conform to
     * the supplied {@link BiPredicate} condition
     * in the provided {@link Map} compatible structure.
     *
     * @param map the map
     * @param predicate the predicate condition
     * @param <K> the key type of the map
     * @param <V> the value type of the map
     **/
    private <K, V> void removeIf(Map<K, V> map, BiPredicate<K, V> predicate) {
        List<Map.Entry<K, V>> toRemove = map
                .entrySet()
                .stream()
                .filter(ent -> predicate.test(ent.getKey(), ent.getValue()))
                .collect(Collectors.toList());

        toRemove.forEach(ent -> map.remove(ent.getKey(), ent.getValue()));
        toRemove.clear();
    }

    /**
     * Cleans the {@link CloseReason#getReasonPhrase()} resulting cause.
     * @param cause the {@link CloseReason} for a disconnection event.
     * @return the cleaned {@link CloseReason#getReasonPhrase()} string.
     */
    private String cleanDisconnectCause(CloseReason cause) {
        String str = cause.getReasonPhrase();
        if (str.contains("Exception: ")) {
            return str.split("Exception: ")[1];
        }

        return str;
    }

}
