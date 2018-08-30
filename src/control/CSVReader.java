package control;

import beans.*;
import repositories.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CSVReader {

    public double turnCoordinateInDegrees(String coordinate) {
        StringBuilder degrees = new StringBuilder(""), minutes = new StringBuilder(""), seconds = new StringBuilder("");
        int field = 1;
        for (int i = 0; i < coordinate.length(); i++) {
            if (coordinate.charAt(i) != ' ') {
                if (field == 1) {
                    degrees.append(coordinate.charAt(i));
                } else if (field == 2) {
                    minutes.append(coordinate.charAt(i));
                } else if (field == 3) {
                    seconds.append(coordinate.charAt(i));
                }
            } else {
                ++field;
            }
        }

        double d = 0, m = 0, s = 0;
        if (!degrees.toString().equals("")) {
            d = Double.parseDouble(degrees.toString());
        } else {
            return d;
        }
        if (!minutes.toString().equals("")) {
            m = Double.parseDouble(minutes.toString());
        }
        if (!seconds.toString().equals("")) {
            s = Double.parseDouble(seconds.toString());
        }

        if (d < 0) {
            return d + ((-m)/60) + ((-s)/3600);
        } else {
            return d + (m/60) + (s/3600);
        }
    }

    public void readBusStops(String filePath) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.ISO_8859_1));

        //Instâncias dos repositórios
        CityRepository cityRep = CityRepository.getInstance();
        NeighborhoodRepository neighRep = NeighborhoodRepository.getInstance();
        StreetRepository streetRep = StreetRepository.getInstance();
        BusStopRepository stopRep = BusStopRepository.getInstance();

        String currentLine = br.readLine(); //Pula a primeira linha
        while ((currentLine = br.readLine()) != null) {
            String stopId = "", streetName = "", neighborhoodName = "", cityName = "", latitudeString = "", longitudeString = "";

            char latLastChar = ' ';
            char longeLasChar = ' ';

            int field = 1;
            for (int i = 0; i < currentLine.length(); i++) {
                if (currentLine.charAt(i) != ';') {
                    if (field == 1) {
                        stopId = stopId + currentLine.charAt(i);
                    } else if(field == 2) {
                        streetName = streetName + currentLine.charAt(i);
                    } else if(field == 3) {
                        neighborhoodName = neighborhoodName + currentLine.charAt(i);
                    } else if(field == 4) {
                        cityName = cityName + currentLine.charAt(i);
                    } else if(field == 5) {
                        char latCurrentChar = currentLine.charAt(i);
                        if ((latLastChar != ' ' || latCurrentChar != ' ') && (latLastChar != '-' || latCurrentChar != ' ')) {
                            latitudeString = latitudeString + latCurrentChar;
                        }
                        latLastChar = latCurrentChar;
                    } else if(field == 6) {
                        char longeCurrentChar = currentLine.charAt(i);
                        if ((longeLasChar != ' ' || longeCurrentChar != ' ') && (longeLasChar != '-' || longeCurrentChar != ' ')) {
                            longitudeString = longitudeString + longeCurrentChar;
                        }
                        longeLasChar = longeCurrentChar;
                    }
                } else {
                    field++;
                }
            }

            double latitude = turnCoordinateInDegrees(latitudeString);
            double longitude = turnCoordinateInDegrees(longitudeString);

            City c = cityRep.getOrCreate(cityName);
            Neighborhood n = neighRep.getOrCreate(neighborhoodName, c);
            c.addNeighborhood(n);
            Street s = streetRep.getOrCreate(streetName, c, n);
            s.addNeighborhood(n);
            n.addStreet(s);
            BusStop stop = new BusStop(stopId, s, n, c, latitude, longitude);
            stopRep.addBusStop(stop);
            s.addBusStop(stop);

        }
    }

    public void readBusLines(String filePath) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.ISO_8859_1));

        //Instâncias dos repositórios
        BusLineRepository lineReb = BusLineRepository.getInstance();
        BusStopRepository stopRep = BusStopRepository.getInstance();
        BusStopInLineRepository slRep = BusStopInLineRepository.getInstance();
        NeighborhoodRepository neighRep = NeighborhoodRepository.getInstance();
        StreetRepository streetRep = StreetRepository.getInstance();

        String currentLine = br.readLine(); //Pula a primeira linha
        while ((currentLine = br.readLine()) != null) {
            String lineID = "", lineName = "", itineraryName = "", stopID = "", latitudeString = "", longitudeString = "", neighborhoodName = "", streetName = "", direction = "", directionID = "", trrOrder = "", rrtOrder = "";

            char latLastChar = ' ';
            char longeLasChar = ' ';

            int field = 1;
            for (int i = 0; i < currentLine.length(); i++) {
                if (currentLine.charAt(i) != ';') {
                    if (field == 1) {
                        lineID = lineID + currentLine.charAt(i);
                    } else if(field == 2) {
                        lineName = lineName + currentLine.charAt(i);
                    } else if(field == 3) {
                        itineraryName = itineraryName + currentLine.charAt(i);
                    } else if(field == 7) {
                        stopID = stopID + currentLine.charAt(i);
                    } else if(field == 8) {
                        streetName = streetName + currentLine.charAt(i);
                    } else if(field == 9) {
                        neighborhoodName = neighborhoodName + currentLine.charAt(i);
                    } else if(field == 10) {
                        char latCurrentChar = currentLine.charAt(i);
                        if ((latLastChar != ' ' || latCurrentChar != ' ') && (latLastChar != '-' || latCurrentChar != ' ')) {
                            latitudeString = latitudeString + latCurrentChar;
                        }
                        latLastChar = latCurrentChar;
                    } else if(field == 11) {
                        char longeCurrentChar = currentLine.charAt(i);
                        if ((longeLasChar != ' ' || longeCurrentChar != ' ') && (longeLasChar != '-' || longeCurrentChar != ' ')) {
                            longitudeString = longitudeString + longeCurrentChar;
                        }
                        longeLasChar = longeCurrentChar;
                    } else if(field == 12) {
                        directionID = directionID + currentLine.charAt(i);
                    } else if(field == 13) {
                        direction = direction + currentLine.charAt(i);
                    } else if(field == 14) {
                        trrOrder = trrOrder + currentLine.charAt(i);
                    } else if(field == 15) {
                        rrtOrder = rrtOrder + currentLine.charAt(i);
                    }
                } else {
                    field++;
                }
            }

            double latitude = turnCoordinateInDegrees(latitudeString);
            double longitude = turnCoordinateInDegrees(longitudeString);

            if (!stopRep.has(latitude, longitude)){
                return;
            }
        }
    }
}
