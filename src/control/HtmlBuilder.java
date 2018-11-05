package control;

import beans.BusLine;
import beans.BusStop;
import beans.Itinerary;
import beans.ItineraryBusStop;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HtmlBuilder {

    private static CSVReader reader = new CSVReader();

    public void build(BusLine bl, int direction) throws Exception {
        if (bl == null) {
            throw new NullPointerException();
        }

        List<ItineraryBusStop> stops = bl.getItineraries().get(direction).getStops();

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

    public void crateMapSimulation(BusLine bl, int direction) throws Exception {
        if (bl == null) {
            throw new NullPointerException();
        }

        GoogleRouteAPIRequester googleRouteAPIRequester = new GoogleRouteAPIRequester();
        PDT pdt = new PDT();

        Itinerary itinerary = bl.getItineraries().get(direction);
        BusStop departure = itinerary.getStops().get(0).getBusStop();
        double[] p1 = pdt.randomLocation(itinerary);
        double[] p2 = pdt.randomLocation(itinerary);
        BusStop bs1 = pdt.findNearestStop(p1, itinerary);
        BusStop bs2 = pdt.findNearestStop(p2, itinerary);
        double euclidian1 = Math.sqrt(Math.pow(departure.getLatitude() - bs1.getLatitude(), 2) + Math.pow(departure.getLongitude() - bs1.getLongitude(), 2));
        double euclidian2 = Math.sqrt(Math.pow(departure.getLatitude() - bs2.getLatitude(), 2) + Math.pow(departure.getLongitude() - bs2.getLongitude(), 2));

        BusStop startStop, endStop;
        double[] start, end;
        JSONObject startWalkJson, endWalkJson;
        if (euclidian1 < euclidian2) {
            startStop = bs1;
            start = p1;
            endStop = bs2;
            end = p2;

            startWalkJson = googleRouteAPIRequester.walkingRoute(p1[0], p1[1], startStop.getLatitude(), startStop.getLongitude());
            endWalkJson = googleRouteAPIRequester.walkingRoute(endStop.getLatitude(), endStop.getLongitude(), p2[0], p2[1]);
        } else {
            startStop = bs2;
            start = p2;
            endStop = bs1;
            end = p1;

            startWalkJson = googleRouteAPIRequester.walkingRoute(p2[0], p2[1], startStop.getLatitude(), startStop.getLongitude());
            endWalkJson = googleRouteAPIRequester.walkingRoute(endStop.getLatitude(), endStop.getLongitude(), p1[0], p1[1]);
        }

        double startWalkDuration = (int) startWalkJson.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").get("value");
        double startWalkDistance = (int) startWalkJson.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").get("value");
        double endWalkDuration = (int) endWalkJson.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").get("value");
        double endWalkDistance = (int) endWalkJson.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").get("value");

        List<ItineraryBusStop> stops = itinerary.getStops();
        List<BusStop> route = new ArrayList<BusStop>();
        int i = 0;
        while (!itinerary.getStops().get(i).getBusStop().equals(startStop)) {
            i++;
        }
        while (!itinerary.getStops().get(i).getBusStop().equals(endStop)) {
            route.add(itinerary.getStops().get(i).getBusStop());
            i++;
        }
        route.add(itinerary.getStops().get(i).getBusStop());

        JSONArray jsonArray = new GoogleRouteAPIRequester().requestRoute(route);
        double totalTravelDistance = 0;
        double totalTravelTime = 0;
        for (i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            JSONArray routes = jsonObject.getJSONArray("routes");
            JSONArray legs = routes.getJSONObject(0).getJSONArray("legs");

            for (int j = 0; j < legs.length(); j++) {
                int duration = (int) legs.getJSONObject(j).getJSONObject("duration").get("value");
                int distance = (int) legs.getJSONObject(j).getJSONObject("distance").get("value");
                totalTravelTime += duration;
                totalTravelDistance += distance;
            }
        }

        System.out.print("Tempo andando até a parada de origem:\n" + startWalkDuration/60 + " minutos\n\n");
        System.out.print("Distância andada até parada de origem:\n" + startWalkDistance + " metros\n\n");
        System.out.print("Tempo percorrido no ônibus:\n" + totalTravelTime/60 + " minutos\n\n");
        System.out.print("Distância percorrida no ônibus:\n" + totalTravelDistance + " metros\n\n");
        System.out.print("Tempo andando até o destino:\n" + endWalkDuration/60 + " minutos\n\n");
        System.out.print("Distância andada até o destino:\n" + endWalkDistance + " metros\n\n");
        System.out.print("Origem: " + start[0] + ", " + start[1] + "\n");
        System.out.print("Destino: " + end[0] + ", " + end[1] + "\n");

        File htmlTemplateFile = new File("src\\web\\simulationTemplate.html");
        String htmlString = FileUtils.readFileToString(htmlTemplateFile, StandardCharsets.UTF_8);

        StringBuilder stationsString = new StringBuilder();
        for (i = 0; i < stops.size(); i++) {
            stationsString.append("\n{lat: ");
            stationsString.append(stops.get(i).getBusStop().getLatitude());
            stationsString.append(", lng: ");
            stationsString.append(stops.get(i).getBusStop().getLongitude());
            stationsString.append(", name: '");
            stationsString.append(stops.get(i).getBusStop().getId());
            stationsString.append("'},");
        }

        StringBuilder routeString = new StringBuilder();
        for (i = 0; i < route.size(); i++) {
            routeString.append("\n{lat: ");
            routeString.append(route.get(i).getLatitude());
            routeString.append(", lng: ");
            routeString.append(route.get(i).getLongitude());
            routeString.append(", name: '");
            routeString.append(route.get(i).getId());
            routeString.append("'},");
        }

        String startOrigin = start[0] + ", " + start[1];
        String startDest = startStop.getLatitude() + ", " + startStop.getLongitude();
        String endOrigin = end[0] + ", " + end[1];
        String endDest = endStop.getLatitude() + ", " + endStop.getLongitude();

        htmlString = htmlString.replace("$stations", stationsString.toString());
        htmlString = htmlString.replace("$route", routeString.toString());

        htmlString = htmlString.replace("$startOrigin", startOrigin);
        htmlString = htmlString.replace("$startDest", startDest);
        htmlString = htmlString.replace("$endOrigin", endOrigin);
        htmlString = htmlString.replace("$endDest", endDest);

        File newHtmlFile = new File("src\\web\\simulation.html");
        FileUtils.writeStringToFile(newHtmlFile, htmlString, StandardCharsets.UTF_8);
    }
}
