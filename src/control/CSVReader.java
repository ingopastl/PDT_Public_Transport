package control;

import beans.*;
import repositories.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
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
            if (currentLine.charAt(i) == ';') {
                count++;
            }
        }
        if (count != 5) {
            System.out.print("Cabeçalho errado");
            System.exit(1);
        }

        //Instâncias dos repositórios
        CityRepository cityRep = CityRepository.getInstance();
        NeighborhoodRepository neighRep = NeighborhoodRepository.getInstance();
        StreetRepository streetRep = StreetRepository.getInstance();
        BusStopRepository stopRep = BusStopRepository.getInstance();

        while ((currentLine = br.readLine()) != null) {
            StringBuilder stopId = new StringBuilder(), streetName = new StringBuilder(), neighborhoodName = new StringBuilder(),
                    cityName = new StringBuilder(), latitudeString = new StringBuilder(), longitudeString = new StringBuilder();

            char latLastChar = ' ';
            char longeLasChar = ' ';

            int field = 1;
            for (int i = 0; i < currentLine.length(); i++) {
                if (currentLine.charAt(i) != ';') {
                    if (field == 1) {
                        stopId.append(currentLine.charAt(i));
                    } else if(field == 2) {
                        streetName.append(currentLine.charAt(i));
                    } else if(field == 3) {
                        neighborhoodName.append(currentLine.charAt(i));
                    } else if(field == 4) {
                        cityName.append(currentLine.charAt(i));
                    } else if(field == 5) {
                        char latCurrentChar = currentLine.charAt(i);
                        if ((latLastChar != ' ' || latCurrentChar != ' ') && (latLastChar != '-' || latCurrentChar != ' ')) {
                            latitudeString.append(latCurrentChar);
                        }
                        latLastChar = latCurrentChar;
                    } else if(field == 6) {
                        char longeCurrentChar = currentLine.charAt(i);
                        if ((longeLasChar != ' ' || longeCurrentChar != ' ') && (longeLasChar != '-' || longeCurrentChar != ' ')) {
                            longitudeString.append(longeCurrentChar);
                        }
                        longeLasChar = longeCurrentChar;
                    }
                } else {
                    field++;
                }
            }

            double latitude = turnCoordinateInDegrees(latitudeString.toString());
            double longitude = turnCoordinateInDegrees(longitudeString.toString());

            //Se não existir uma cidade com este nome cadastrada, um novo objeto City é criado e adicionado ao repositório.
            //Caso contrário, a referência para o objeto City contendo aquele nome é retornada.
            City c = cityRep.getOrCreate(cityName.toString());
            //O mesmo ocorre com a neighborhood.
            Neighborhood n = neighRep.getOrCreate(neighborhoodName.toString(), c);
            c.addNeighborhood(n);
            //Acontece com street o mesmo que com Neighborhood e City
            Street s = streetRep.getOrCreate(streetName.toString(), c, n);
            s.addNeighborhood(n);
            n.addStreet(s);
            BusStop stop = new BusStop(stopId.toString(), s, n, c, latitude, longitude);
            stopRep.addBusStop(stop);
            s.addBusStop(stop);
        }
    }

    public void readBusLines(String filePath) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.ISO_8859_1));

        String currentLine = br.readLine(); //Pega o cabeçalho
        int count = 0;
        for (int i = 0; i < currentLine.length(); i++) {
            if (currentLine.charAt(i) == ';') {
                count++;
            }
        }
        if (count != 9) {
            System.out.print("Cabeçalho errado");
            System.exit(1);
        }

        //Instâncias dos repositórios
        BusLineRepository busLineReb = BusLineRepository.getInstance();
        BusStopRepository busStopRep = BusStopRepository.getInstance();
        NeighborhoodRepository neighRep = NeighborhoodRepository.getInstance();
        StreetRepository streetRep = StreetRepository.getInstance();

        while ((currentLine = br.readLine()) != null) {
            StringBuilder busLineID = new StringBuilder(), busLineName = new StringBuilder(), itineraryName = new StringBuilder(), stopID = new StringBuilder(),
                    latitudeString = new StringBuilder(), longitudeString = new StringBuilder(), neighborhoodName = new StringBuilder(),
                    streetName = new StringBuilder(), trrOrder = new StringBuilder(), rrtOrder = new StringBuilder();

            char latLastChar = ' ';
            char longeLasChar = ' ';

            int field = 1;
            for (int i = 0; i < currentLine.length(); i++) {
                if (currentLine.charAt(i) != ';') {
                    if (field == 1) {
                        busLineID.append(currentLine.charAt(i));
                    } else if(field == 2) {
                        busLineName.append(currentLine.charAt(i));
                    } else if(field == 3) {
                        itineraryName.append(currentLine.charAt(i));
                    } else if(field == 4) {
                        stopID.append(currentLine.charAt(i));
                    } else if(field == 5) {
                        streetName.append(currentLine.charAt(i));
                    } else if(field == 6) {
                        neighborhoodName.append(currentLine.charAt(i));
                    } else if(field == 7) {
                        char latCurrentChar = currentLine.charAt(i);
                        if ((latLastChar != ' ' || latCurrentChar != ' ') && (latLastChar != '-' || latCurrentChar != ' ')) {
                            latitudeString.append(latCurrentChar);
                        }
                        latLastChar = latCurrentChar;
                    } else if(field == 8) {
                        char longeCurrentChar = currentLine.charAt(i);
                        if ((longeLasChar != ' ' || longeCurrentChar != ' ') && (longeLasChar != '-' || longeCurrentChar != ' ')) {
                            longitudeString.append(longeCurrentChar);
                        }
                        longeLasChar = longeCurrentChar;
                    } else if(field == 9) {
                        trrOrder.append(currentLine.charAt(i));
                    } else if(field == 10) {
                        rrtOrder.append(currentLine.charAt(i));
                    }
                } else {
                    field++;
                }
            }

            if (field != 10) {
                System.out.print("Erro na formatação do arquivo");
                System.exit(1);
            }

            double latitude = turnCoordinateInDegrees(latitudeString.toString());
            double longitude = turnCoordinateInDegrees(longitudeString.toString());

            //Se não existir um bairro com este nome cadastrada, um novo objeto Neighborhood é criado e adicionado ao repositório.
            //Caso contrário, a referência para o objeto Neighborhood contendo aquele nome é retornada.
            Neighborhood n = neighRep.getOrCreate(neighborhoodName.toString(), null);
            //Acontece com street o mesmo que com Neighborhood
            Street s = streetRep.getOrCreate(streetName.toString(), null, n);
            s.addNeighborhood(n);
            n.addStreet(s);
            //Acontece com street o mesmo que com BusStop
            BusStop stop = busStopRep.getOrCreate(stopID.toString(), s, n, null, latitude, longitude);
            s.addBusStop(stop);
            //Acontece com street o mesmo que com BusLine
            BusLine bl = busLineReb.getOrCreate(busLineID.toString(), busLineName.toString());
            bl.addStopToLine(stop, itineraryName.toString(), Integer.parseInt(trrOrder.toString()), Integer.parseInt(rrtOrder.toString()));
        }
    }
}
