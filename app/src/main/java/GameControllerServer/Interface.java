package GameControllerServer;

import co.m1ke.basic.events.EventManager;
import co.m1ke.basic.events.interfaces.Event;
import co.m1ke.basic.events.interfaces.EventListener;
import co.m1ke.basic.logger.Logger;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;

import GameControllerServer.connection.Connection;
import GameControllerServer.connection.event.ClientCommunicationEvent;
import GameControllerServer.connection.event.ClientConnectedEvent;
import GameControllerServer.connection.event.ClientDisconnectEvent;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * JavaFX Interface Management for UConn ACM <a href="https://github.com/ACM-UConn/GameController-Server">Game Controller Project</a>
 * @author Benny Chen & Mike Medved
 * @date December 7th, 2020
 */
public class Interface extends Application implements EventListener {

    private Stage home;
    private Scene initialScreen;
    private Scene pendingScreen;
    private Scene controlScene;
    private Button nextScreen;

    private Logger logger;
    private EventManager eventManager;
    private Connection connection;
    private boolean pendingConnection;

    private final Object LOCK = new Object();

    //Launches Interface
    public static void main(String[] args) {
        launch(args);
    }

    //Main
    @Override
    public void start(Stage primaryStage) {
        this.home = primaryStage;

        // Register Communications Service
        this.logger = new Logger("GameControllerServer");
        this.eventManager = new EventManager(false);

        CompletableFuture<Connection> connectionFuture = CompletableFuture.supplyAsync(() -> new Connection(false, eventManager));
        this.connection = connectionFuture.join();
        this.pendingConnection = true;

        // Register Listeners
        eventManager.getEventExecutor().registerListener(this);

        BufferedImage qrCode = getQrCode();
        if (qrCode == null) {
            // TODO: Display error on interface that it failed to generate a QR Code.
            return;
        }

        //Interface
        ImageView qrView = new ImageView(SwingFXUtils.toFXImage(qrCode, null));
        ImageView sanik = new ImageView(new Image("https://i.kym-cdn.com/photos/images/newsfeed/000/472/021/b85.gif"));

        //Button Click
        nextScreen = new Button("Next Page");
        nextScreen.setOnAction(e -> home.setScene(pendingScreen));

        //QR Code Screen
        VBox Vlayout = new VBox(20);
        Vlayout.getChildren().addAll(qrView,nextScreen);
        initialScreen = new Scene(Vlayout,800,800);

        //Next Screen
        StackPane layout = new StackPane();
        layout.getChildren().add(sanik);
        pendingScreen = new Scene(layout, 800, 800);
        boolean goToNextPage = false;

        //Control Screen
        StackPane mainPage = new StackPane();
        controlScene = new Scene(mainPage, 800, 800);
        home.setScene(controlScene);

        home.setTitle("GameController");
        home.setScene(initialScreen);
        home.show();
    }

    @Event
    public void onConnect(ClientConnectedEvent event) {
        if (!pendingConnection) {
            return;
        }

        home.setScene(controlScene);
        home.show();
    }

    @Event
    public void onDisconnect(ClientDisconnectEvent event) {
        // TODO: Handle client disconnection
    }

    @Event
    public void onMessage(ClientCommunicationEvent event) {
        // TODO: Display keystrokes or something on interface if you want
    }

    /**
     * Generates a QR Code using the supplied
     * connection data from {@link Connection}.
     * @return a {@link BufferedImage} containing the QR Code
     */
    private BufferedImage getQrCode() {
        try {
            String URL = connection.getConnectionUrl();
            QRCodeWriter writer = new QRCodeWriter();
            int width = 300;
            int height = 300;

            BitMatrix byteMatrix = writer.encode(URL, BarcodeFormat.QR_CODE, width, height);
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();

            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
            graphics.setColor(Color.BLACK);

            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (byteMatrix.get(i, j)) {
                        graphics.fillRect(i, j, 1, 1);
                    }
                }
            }

            return image;
        } catch (WriterException ex) {
            logger.except(ex, "Failed to write QR Code");
            return null;
        }
    }

}