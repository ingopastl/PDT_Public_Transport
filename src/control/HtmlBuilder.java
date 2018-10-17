package control;

import beans.BusLine;
import beans.ItineraryBusStop;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class HtmlBuilder {

    private static CSVReader reader = new CSVReader();

    public void build(BusLine bl, int direction) throws Exception {
        if (bl == null) {
            throw new NullPointerException();
        }

        List<ItineraryBusStop> stops = bl.getItineraries().get(direction).getStops();
        if (stops.size() == 0) {
            reader.readStopSequence("src\\data\\SpBusLineData\\itinerary\\stopSequence\\" + bl.getItineraries().get(direction).getItineraryId() + ".txt");
        }

        File htmlTemplateFile = new File("src\\web\\template.html");
        String htmlString = FileUtils.readFileToString(htmlTemplateFile, StandardCharsets.UTF_8);

        StringBuilder stations = new StringBuilder();
        for (int i = 0; i < stops.size(); i++) {
            stations.append("\n{lat: ");
            stations.append(stops.get(i).getBusStop().getLatitude());
            stations.append(", lng: ");
            stations.append(stops.get(i).getBusStop().getLongitude());
            stations.append(", name: '");
            stations.append(stops.get(i).getBusStop().getId());
            stations.append("'},");
        }

        htmlString = htmlString.replace("$stations", stations.toString());
        File newHtmlFile = new File("src\\web\\displayRoute.html");
        FileUtils.writeStringToFile(newHtmlFile, htmlString, StandardCharsets.UTF_8);
    }
}
