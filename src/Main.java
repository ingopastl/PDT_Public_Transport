import beans.BusLine;
import beans.BusStop;
import control.*;
import repositories.BusLineRepository;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
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

public class Main extends Application {

    private static CSVReader reader = new CSVReader();
    private static GoogleRouteAPIRequester apiRequester = new GoogleRouteAPIRequester();
    private static PDT pdt = new PDT();

    public static void main(String[] args) {

        try{
            reader.readBusStops("src\\data\\SpBusLineData\\stops.txt");
            reader.readBusLines("src\\data\\SpBusLineData\\routes.txt");
            reader.readItineraries("src\\data\\SpBusLineData\\itinerary\\itineraries.txt");

            BusLineRepository busLineRep = BusLineRepository.getInstance();
            BusLine bl = busLineRep.getByID("423032");

            double[] randomLocation = pdt.randomLocation(bl.getItineraries().get(0));
            BusStop closestStop = pdt.findNearestStop(randomLocation, bl.getItineraries().get(0));

            System.out.print("Localização: Lat = " + randomLocation[0] + ", Long = " + randomLocation[1] + "\n");
            System.out.print("Parada mais próxima: Lat = " + closestStop.getLatitude() + ", Long = " + closestStop.getLongitude() + "\n");

            System.out.print("Distância de caminhada: " + apiRequester.walkingDistance(randomLocation[0], randomLocation[1], closestStop.getLatitude(), closestStop.getLongitude()) + " metros");

        } catch (Exception e) {
            e.printStackTrace();
        }

        //Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        WebView myWebView = new WebView();
        WebEngine engine = myWebView.getEngine();
        TextField busLineInput = new TextField();
        Button loadFileBtn = new Button("Display");
        ObservableList<String> directionsOptions = FXCollections.observableArrayList("Outward", "Return");
        ComboBox directionOptions = new ComboBox(directionsOptions);


        loadFileBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    HtmlBuilder html = new HtmlBuilder();
                    GoogleRouteAPIRequester apiRequester = new GoogleRouteAPIRequester();
                    String busLineString = busLineInput.getText();
                    String dOption = (String) directionOptions.getValue();

                    BusLineRepository busLineRep = BusLineRepository.getInstance();
                    BusLine bl = busLineRep.getByID(busLineString);
                    if (dOption.equals("Outward")) {
                        html.build(bl, 0);
                    } else {
                        html.build(bl, 1);
                    }

                    File file = new File("src\\web\\displayRoute.html");
                    engine.load(file.toURI().toURL().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        VBox root = new VBox();
        root.getChildren().addAll(myWebView, busLineInput, directionOptions, loadFileBtn);
        stage.setTitle("IC 2018");
        Scene scene = new Scene(root, 800, 500);
        stage.setScene(scene);
        stage.show();
    }
}