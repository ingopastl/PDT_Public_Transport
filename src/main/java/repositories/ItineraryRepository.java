package repositories;

import beans.BusLine;
import beans.Itinerary;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ItineraryRepository {
    private List<Itinerary> list;
    private static ItineraryRepository instance;

    private ItineraryRepository() {
        list = new ArrayList<Itinerary>();
    }

    public static ItineraryRepository getInstance() {
        if (instance == null) {
            instance = new ItineraryRepository();
        }
        return instance;
    }

    public void readCSV(String filePath) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.ISO_8859_1));

        String currentLine = br.readLine(); //Pega o cabeçalho
        int count = 0;
        for (int i = 0; i < currentLine.length(); i++) {
            if (currentLine.charAt(i) == ',') {
                count++;
            }
        }
        if (count != 7) {
            System.out.print("Cabeçalho errado");
            System.exit(1);
        }

        while ((currentLine = br.readLine()) != null) {
            StringBuilder busLineID = new StringBuilder(), serviceId = new StringBuilder(), itineraryId = new StringBuilder(), itineraryHeadsign = new StringBuilder();

            int field = 1;
            for (int i = 0; i < currentLine.length(); i++) {
                char currentChar = currentLine.charAt(i);
                if (currentChar != ',') {
                    if (currentChar != '\"') {
                        if (field == 1) {
                            busLineID.append(currentChar);
                        } else if (field == 2) {
                            serviceId.append(currentChar);
                        } else if (field == 3) {
                            itineraryId.append(currentChar);
                        } else if (field == 4) {
                            itineraryHeadsign.append(currentChar);
                        }
                    }
                } else {
                    field++;
                }
            }

            BusLineRepository busLineRep = BusLineRepository.getInstance();
            BusLine bl = busLineRep.getByID(busLineID.toString());
            if (bl == null) {
                throw new NullPointerException();
            }

            Itinerary it = new Itinerary(bl, serviceId.toString().charAt(0), itineraryId.toString(), itineraryHeadsign.toString());
            bl.addItinerary(it);
            addItinerary(it);
        }
        br.close();
    }

    public void addItinerary(Itinerary it) throws NullPointerException {
        if (it != null) {
            list.add(it);
        } else {
            throw new NullPointerException();
        }
    }

    public Itinerary getById(String id) {
        for (int i = 0; i < this.list.size(); i++) {
            if (this.list.get(i).getItineraryId().equals(id)) {
                return this.list.get(i);
            }
        }
        return null;
    }

    public void printList() {
        for (int i = 0; i < this.list.size(); i++) {
            System.out.print(this.list.get(i) + "\n");
        }
    }
}
