import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;

public class App extends Application {
    public static void main(String[] args) {
        launch(args);
    }
    public void start(Stage primaryStage) throws Exception {
        VBox root = new FXMLLoader(getClass().getResource("/resources/App.fxml")).load();
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/icons/icon.png")));
        primaryStage.setTitle("File Explorer");
        Scene scene = new Scene(root, 1040, 520);
        scene.getStylesheets().add((getClass().getResource("/resources/styles.css").toExternalForm()));
        primaryStage.setScene(scene);
        primaryStage.show();
    }
} 
