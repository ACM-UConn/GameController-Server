package GameControllerServer;

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

import java.awt.*;
import java.awt.image.BufferedImage;


// Create interface for app. Follows same steps as App
// Act out these steps using other files based on what App is telling you:
// Start interface, initiate connection, open game screen, start playing.

public class Interface extends Application{

    Stage home;
    Scene scene1, scene2, scene3;

    Button nextScreen;
    //Launches Interface
    public static void main(String[] args) {
        launch(args);
    }

    //Main
    @Override
    public void start(Stage primaryStage) throws Exception {
        home = primaryStage;
        //QR Code generator


        String URL = "https://youtu.be/oHg5SJYRHA0";
        QRCodeWriter writer = new QRCodeWriter();
        int QRwidth = 300;
        int QRheight = 300;

        BufferedImage bufferedImage = null;

        try {
            BitMatrix byteMatrix = writer.encode(URL, BarcodeFormat.QR_CODE, QRwidth, QRheight);
            bufferedImage = new BufferedImage(QRwidth, QRheight, BufferedImage.TYPE_INT_RGB);
            bufferedImage.createGraphics();

            Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, QRwidth, QRheight);
            graphics.setColor(Color.BLACK);

            for (int i = 0; i < QRheight; i++) {
                for (int j = 0; j < QRwidth; j++) {
                    if (byteMatrix.get(i, j)) {
                        graphics.fillRect(i, j, 1, 1);
                    }
                }
            }

        } catch (WriterException ex) {
            ex.printStackTrace();
        }

        //Interface
        ImageView qrView = new ImageView();
        qrView.setImage(SwingFXUtils.toFXImage(bufferedImage, null));

        ImageView SANIK = new ImageView(new Image("https://i.kym-cdn.com/photos/images/newsfeed/000/472/021/b85.gif"));

        //Button Click

        nextScreen = new Button("Next Page");
        nextScreen.setOnAction(e -> home.setScene(scene2));

        //QR Code Screen

        VBox Vlayout = new VBox(20);
        Vlayout.getChildren().addAll(qrView,nextScreen);
        scene1 = new Scene(Vlayout,800,800);

        //Next Screen
        StackPane layout = new StackPane();
        layout.getChildren().add(SANIK);
        scene2 = new Scene(layout, 800, 800);
        boolean goToNextPage = false;

        //Control Screen
        StackPane mainPage = new StackPane();
        scene3 = new Scene(mainPage, 800, 800);
        home.setScene(scene3);

        home.setTitle("GameController");
        home.setScene(scene1);
        home.show();

    }

}