import control.*;
import repositories.*;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;

public class Main extends Application {

    public static void main(String[] args) {
        CSVReader reader = new CSVReader();
        try{
            reader.readBusLines("dataFiles\\CTMGrandeRecife_LinhaParadaTrajeto_dado.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Instâncias dos repositórios
        //CityRepository cityRep = CityRepository.getInstance();
        //NeighborhoodRepository neighRep = NeighborhoodRepository.getInstance();
        //StreetRepository streetRep = StreetRepository.getInstance();
        //BusStopRepository busStopRep = BusStopRepository.getInstance();
        //BusLineRepository busLineRep = BusLineRepository.getInstance();
        //BusStopInLineRepository busStopInLineRep = BusStopInLineRepository.getInstance();

        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        WebView myWebView = new WebView();
        WebEngine engine = myWebView.getEngine();

        Button loadFileBtn = new Button("Display bus line number 645");
        loadFileBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                try {
                BusLineRepository busLineRep = BusLineRepository.getInstance();
                HtmlBuilder html = new HtmlBuilder();
                html.build(busLineRep.getByID("645"));
                File file = new File("webFiles\\displayRoute.html");
                    engine.load(file.toURI().toURL().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        VBox root = new VBox();
        root.getChildren().addAll(myWebView, loadFileBtn);

        Scene scene = new Scene(root, 800, 500);
        stage.setScene(scene);

        stage.show();
    }
}