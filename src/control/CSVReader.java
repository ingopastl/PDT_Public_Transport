package control;

import beans.*;
import repositories.*;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class CSVReader {

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

    public void readBusStops(String filePath) throws IOException {
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

        //StreetRepository streetRep = StreetRepository.getInstance();
        BusStopRepository stopRep = BusStopRepository.getInstance();

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

            /*
            * Street data is not necessary as of now.
            */
            //Street s = streetRep.getOrCreate(streetName.toString());
            BusStop stop = new BusStop(stopId.toString(), null, Double.parseDouble(latitudeString.toString()), Double.parseDouble(longitudeString.toString()));
            stopRep.addBusStop(stop);
            //s.addBusStop(stop);
        }
    }

    public void readBusLines(String filePath) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.ISO_8859_1));

        String currentLine = br.readLine(); //Pega o cabeçalho
        int count = 0;
        for (int i = 0; i < currentLine.length(); i++) {
            if (currentLine.charAt(i) == ',') {
                count++;
            }
        }
        if (count != 8) {
            System.out.print("Cabeçalho errado");
            System.exit(1);
        }

        //Instâncias dos repositórios
        BusLineRepository busLineReb = BusLineRepository.getInstance();

        while ((currentLine = br.readLine()) != null) {
            StringBuilder busLineID = new StringBuilder(), busLineShortName = new StringBuilder(), busLineLongName = new StringBuilder();

            int field = 1;
            for (int i = 0; i < currentLine.length(); i++) {
                char currentChar = currentLine.charAt(i);
                if (currentChar != ',') {
                    if (currentChar != '\"') {
                        if (field == 1) {
                            busLineID.append(currentChar);
                        } else if (field == 3) {
                            busLineShortName.append(currentChar);
                        } else if (field == 4) {
                            busLineLongName.append(currentChar);
                        }
                    }
                } else {
                    field++;
                }
            }

            //Acontece com street o mesmo que com BusLine
            BusLine bl = new BusLine(busLineID.toString(), busLineShortName.toString(), busLineLongName.toString());
            busLineReb.addBusLine(bl);
        }
    }

    public void readItineraries(String filePath) throws IOException {
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

        //Instâncias dos repositórios
        ItineraryRepository itRep = ItineraryRepository.getInstance();
        BusLineRepository lineRep = BusLineRepository.getInstance();

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

            BusLine bl = lineRep.getByID(busLineID.toString());
            if (bl == null) {
                throw new NullPointerException();
            }

            Itinerary it = new Itinerary(bl, serviceId.toString().charAt(0), itineraryId.toString(), itineraryHeadsign.toString());
            bl.addItinerary(it);

            itRep.addItinerary(it);
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

        //Instâncias dos repositórios
        ItineraryRepository itRep = ItineraryRepository.getInstance();
        BusStopRepository busStopRep = BusStopRepository.getInstance();
        ItineraryBusStopRepository itiBsRep = ItineraryBusStopRepository.getInstance();

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

            BusStop bs = busStopRep.getById(stopId.toString());
            Itinerary iti = itRep.getById(itineraryId.toString());
            ItineraryBusStop ibs = new ItineraryBusStop(bs, iti, Integer.parseInt(stopSequence.toString()));
            iti.addItineraryBusStop(ibs);
            itiBsRep.addItineraryBusStop(ibs);
        }
    }
}
