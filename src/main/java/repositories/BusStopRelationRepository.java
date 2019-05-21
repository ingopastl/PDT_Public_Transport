package repositories;

import beans.BusStopRelation;
import beans.BusStop;
import beans.Itinerary;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BusStopRelationRepository {
    private List<BusStopRelation> list;
    private static BusStopRelationRepository instance;

    private BusStopRelationRepository() {
        list = new ArrayList<BusStopRelation>();
    }

    public static BusStopRelationRepository getInstance() {
        if (instance == null) {
            instance = new BusStopRelationRepository();
        }
        return instance;
    }

    private double turnCoordinateInDegrees(String coordinate) {
        StringBuilder degrees = new StringBuilder(), minutes = new StringBuilder(), seconds = new StringBuilder();
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
        if (degrees.toString().length() > 0) {
            d = Double.parseDouble(degrees.toString());
        } else {
            return d;
        }
        if (minutes.toString().length() > 0) {
            m = Double.parseDouble(minutes.toString());
        }
        if (seconds.toString().length() > 0) {
            s = Double.parseDouble(seconds.toString());
        }

        if (d < 0) {
            return d + ((-m)/60) + ((-s)/3600);
        } else {
            return d + (m/60) + (s/3600);
        }
    }

    public void readStopSequence(String filePath) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.ISO_8859_1));

        String currentLine = br.readLine(); //Pega o cabeçalho
        int count = 0;
        for (int i = 0; i < currentLine.length(); i++) {
            if (currentLine.charAt(i) == ',') {
                count++;
            }
        }
        if (count != 5) {
            System.out.print("Cabeçalho errado");
            System.exit(1);
        }

        while ((currentLine = br.readLine()) != null) {
            StringBuilder itineraryId = new StringBuilder(), stopId = new StringBuilder(), stopSequence = new StringBuilder();

            int field = 1;
            for (int i = 0; i < currentLine.length(); i++) {
                char currentChar = currentLine.charAt(i);
                if (currentChar != ',') {
                    if (currentChar != '\"') {
                        if (field == 1) {
                            itineraryId.append(currentChar);
                        } else if (field == 4) {
                            stopId.append(currentChar);
                        } else if (field == 5) {
                            stopSequence.append(currentChar);
                        }
                    }
                } else {
                    field++;
                }
            }

            ItineraryRepository itRep = ItineraryRepository.getInstance();
            BusStopRepository busStopRep = BusStopRepository.getInstance();

            BusStop bs = busStopRep.getById(stopId.toString());
            Itinerary iti = itRep.getById(itineraryId.toString());
            BusStopRelation ibs = new BusStopRelation(bs, iti, Integer.parseInt(stopSequence.toString()));

            iti.addItineraryBusStop(ibs);
            addItineraryBusStop(ibs);
        }
        br.close();
    }

    public void addItineraryBusStop(BusStopRelation relation) throws NullPointerException {
        if (relation != null) {
            list.add(relation);
        } else {
            throw new NullPointerException();
        }
    }

    public void printList() {
        for (int i = 0; i < this.list.size(); i++) {
            System.out.print(this.list.get(i) + "\n");
        }
    }
}
