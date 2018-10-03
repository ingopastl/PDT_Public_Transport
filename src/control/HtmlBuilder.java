package control;

import beans.BusLine;
import beans.ItineraryBusStop;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.util.List;

public class HtmlBuilder {

    public void build(BusLine bl) throws Exception {
        File htmlTemplateFile = new File("src\\web\\template.html");
        String htmlString = FileUtils.readFileToString(htmlTemplateFile);

        List<ItineraryBusStop> stops = bl.getItineraries().get(0).getStops();

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
        FileUtils.writeStringToFile(newHtmlFile, htmlString);
    }
}
