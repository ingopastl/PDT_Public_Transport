package repositories;

import beans.BusLine;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BusLineRepository {
    private List<BusLine> list;
    private static BusLineRepository instance;

    private BusLineRepository() {
        list = new ArrayList<BusLine>();
    }

    public static BusLineRepository getInstance() {
        if (instance == null) {
            instance = new BusLineRepository();
        }
        return instance;
    }

    public void readCSV(String filePath) throws IOException {
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

            BusLine bl = new BusLine(busLineID.toString(), busLineShortName.toString(), busLineLongName.toString());
            addBusLine(bl);
        }
    }

    public void addBusLine(BusLine line) throws NullPointerException {
        if (line != null) {
            list.add(line);
        } else {
            throw new NullPointerException();
        }
    }

    public BusLine getByIndex(int i) {
        if (i < this.list.size() && i >= 0) {
            return this.list.get(i);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public BusLine getByID(String id) throws Exception {
        BusLine bl = null;
        for (int i = 0; i < this.list.size(); i++) {
            if (id.equals(this.list.get(i).getId())) {
                bl = this.list.get(i);
                break;
            }
        }

        return bl;
    }

    public void printList() {
        for (int i = 0; i < this.list.size(); i++) {
            System.out.print(this.list.get(i) + "\n");
        }
    }

    public int getListSize() {
        return list.size();
    }
}
