package control;

import beans.BusLine;
import beans.BusStopInLine;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.util.ArrayList;

public class HtmlBuilder {

    public void build(BusLine bl) throws Exception {
        File htmlTemplateFile = new File("webFiles\\template.html");
        String htmlString = FileUtils.readFileToString(htmlTemplateFile);

        ArrayList<BusStopInLine> ttrStations = bl.getSortedTtrStops();
        ttrStations.remove(ttrStations.size() - 1);
        ArrayList<BusStopInLine> rttStations = bl.getSortedRttStops();

        ArrayList<BusStopInLine> allStations = new ArrayList<BusStopInLine>();
        allStations.addAll(ttrStations);
        allStations.addAll(rttStations);

        StringBuilder stations = new StringBuilder();
        for (int i = 0; i < allStations.size(); i++) {
            stations.append("\n{lat: ");
            stations.append(allStations.get(i).getBusStop().getLatitude());
            stations.append(", lng: ");
            stations.append(allStations.get(i).getBusStop().getLongitude());
            stations.append(", name: '");
            stations.append(allStations.get(i).getBusStop().getId());
            stations.append("'},");
        }

        htmlString = htmlString.replace("$stations", stations.toString());
        File newHtmlFile = new File("webFiles\\displayRoute.html");
        FileUtils.writeStringToFile(newHtmlFile, htmlString);
    }
}
