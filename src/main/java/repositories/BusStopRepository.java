package repositories;

import beans.BusStop;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BusStopRepository {
    private List<BusStop> list;
    private static BusStopRepository instance;

    private BusStopRepository() {
        list = new ArrayList<BusStop>();
    }

    public static BusStopRepository getInstance() {
        if (instance == null) {
            instance = new BusStopRepository();
        }
        return instance;
    }

    public void readStopsCSV(String filePath) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.ISO_8859_1));

        String currentLine = br.readLine(); //Pega o cabeçalho.
        int count = 0;
        for (int i = 0; i < currentLine.length(); i++) {
            if (currentLine.charAt(i) == ',') {
                count++;
            }
        }
        if (count != 9) {
            System.out.print("Cabeçalho errado");
            System.exit(1);
        }

        while ((currentLine = br.readLine()) != null) {
            StringBuilder stopId = new StringBuilder(), streetName = new StringBuilder(), latitudeString = new StringBuilder(), longitudeString = new StringBuilder();

            int field = 1;
            int quoteCount = 0;
            for (int i = 0; i < currentLine.length(); i++) {
                char currentChar = currentLine.charAt(i);
                if (currentChar == ',') {
                    field++;
                } else {
                    if (field == 1) {
                        while(true) {
                            if (currentLine.charAt(i) == '\"') {
                                quoteCount++;
                                if (quoteCount >= 2) {
                                    quoteCount = 0;
                                    break;
                                }
                            } else {
                                stopId.append(currentLine.charAt(i));
                            }
                            ++i;
                        }
                    } else if (field == 3) {
                        while(true) {
                            if (currentLine.charAt(i) == '\"') {
                                quoteCount++;
                                if (quoteCount >= 2) {
                                    quoteCount = 0;
                                    break;
                                }
                            } else {
                                streetName.append(currentLine.charAt(i));
                            }
                            ++i;
                        }
                    } else if (field == 5) {
                        while(true) {
                            if (currentLine.charAt(i) == '\"') {
                                quoteCount++;
                                if (quoteCount >= 2) {
                                    quoteCount = 0;
                                    break;
                                }
                            } else {
                                latitudeString.append(currentLine.charAt(i));
                            }
                            ++i;
                        }
                    } else if (field == 6) {
                        while(true) {
                            if (currentLine.charAt(i) == '\"') {
                                quoteCount++;
                                if (quoteCount >= 2) {
                                    quoteCount = 0;
                                    break;
                                }
                            } else {
                                longitudeString.append(currentLine.charAt(i));
                            }
                            ++i;
                        }
                    }
                }
            }

            BusStop stop = new BusStop(stopId.toString(), Double.parseDouble(latitudeString.toString()), Double.parseDouble(longitudeString.toString()));
            addNode(stop);
        }
        br.close();
    }

    public void addNode(BusStop stop) throws NullPointerException {
        if (stop != null) {
            list.add(stop);
        } else {
            throw new NullPointerException();
        }
    }

    public void printList() {
        for (int i = 0; i < this.list.size(); i++) {
            System.out.print(this.list.get(i) + "\n");
        }
    }

    public BusStop getById(String id) {
        for (int i = 0; i < this.list.size(); i++) {
            if (this.list.get(i).getId().equals(id)) {
                return this.list.get(i);
            }
        }
        return null;
    }
}
