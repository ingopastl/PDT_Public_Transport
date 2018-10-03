package control;

/*
TODO
This class is not really being used as of now.
 */
public class GoogleRouteAPIRequester {
    /*
    private static final String DEFAULTURL = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String APIKEY = "AIzaSyA4nuxucJdREFeUZ_8NtmgtX1tBf1ShurU";

    /*
    Encode a double value to the Google's Encoded Polyline Algorithm Format

    private String polylineEncode(double value) {
        //Take the decimal value and multiply it by 1e5
        int round = (int) Math.round(value * 1e5);
        //Convert the decimal value to binary. Note that a negative value must be calculated using its two's complement.
        StringBuilder binaryString = new StringBuilder(Integer.toBinaryString(round));
        if (round < 0) {
            while (binaryString.length() != 32) {
                binaryString.insert(0, '1');
            }
        } else {
            while (binaryString.length() != 32) {
                binaryString.insert(0, '0');
            }
        }

        //Left-shift the binary value one bit
        binaryString.deleteCharAt(0);
        binaryString.append('0');

        //If the original decimal value is negative, invert this encoding
        if (value < 0) {
            for (int i = 0; i < binaryString.length(); i++) {
                if (binaryString.charAt(i) == '1') {
                    binaryString.deleteCharAt(i);
                    binaryString.insert(i,'0');
                } else if (binaryString.charAt(i) == '0') {
                    binaryString.deleteCharAt(i);
                    binaryString.insert(i,'1');
                }
            }
        }

        //Break the binary value out into 5-bit chunks (starting from the right hand side)
        //and place the 5-bit chunks into reverse order
        StringBuilder chunk1 = new StringBuilder(), chunk2 = new StringBuilder(), chunk3 = new StringBuilder(), chunk4 = new StringBuilder(), chunk5 = new StringBuilder(), chunk6 = new StringBuilder();
        int chunk = 0;
        int count = 0;
        for (int i = binaryString.length() - 1; i >= 0; i--) {
            if (chunk == 0) {
                count++;
                chunk1.insert(0, binaryString.charAt(i));
                if (count == 5) {
                    chunk++;
                    count = 0;
                }
            } else if(chunk == 1) {
                count++;
                chunk2.insert(0, binaryString.charAt(i));
                if (count == 5) {
                    chunk++;
                    count = 0;
                }
            } else if(chunk == 2) {
                count++;
                chunk3.insert(0, binaryString.charAt(i));
                if (count == 5) {
                    chunk++;
                    count = 0;
                }
            } else if(chunk == 3) {
                count++;
                chunk4.insert(0, binaryString.charAt(i));
                if (count == 5) {
                    chunk++;
                    count = 0;
                }
            } else if(chunk == 4) {
                count++;
                chunk5.insert(0, binaryString.charAt(i));
                if (count == 5) {
                    chunk++;
                    count = 0;
                }
            } else if (chunk == 5) {
                count++;
                chunk6.insert(0, binaryString.charAt(i));
                if (count == 5) {
                    chunk++;
                    count = 0;
                }
            } else {
                break;
            }
        }

        //OR each value with 0x20 if another bit chunk follows
        if (chunk2.toString().equals("00000")) {
            chunk1.insert(0, '0');
        } else {
            chunk1.insert(0, '1');
        }
        if (chunk3.toString().equals("00000")) {
            chunk2.insert(0, '0');
        } else {
            chunk2.insert(0, '1');
        }
        if (chunk4.toString().equals("00000")) {
            chunk3.insert(0, '0');
        } else {
            chunk3.insert(0, '1');
        }
        if (chunk5.toString().equals("00000")) {
            chunk4.insert(0, '0');
        } else {
            chunk4.insert(0, '1');
        }
        if (chunk6.toString().equals("00000")) {
            chunk5.insert(0, '0');
        } else {
            chunk5.insert(0, '1');
        }
        chunk6.insert(0, '0');

        //Convert each value to decimal
        //and add 63 to each value
        int c1IntValue = 63 + Integer.parseInt(chunk1.toString(), 2);
        int c2IntValue = 63 + Integer.parseInt(chunk2.toString(), 2);
        int c3IntValue = 63 + Integer.parseInt(chunk3.toString(), 2);
        int c4IntValue = 63 + Integer.parseInt(chunk4.toString(), 2);
        int c5IntValue = 63 + Integer.parseInt(chunk5.toString(), 2);
        int c6IntValue = 63 + Integer.parseInt(chunk6.toString(), 2);

        //Convert each value to its ASCII equivalent
        int arr[] = {c1IntValue, c2IntValue, c3IntValue, c4IntValue, c5IntValue, c6IntValue};
        StringBuilder polyline = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] > 63) {
                polyline.append((char) arr[i]);
            }
        }
        return polyline.toString();
    }

    /*
    Convert a series of waypoint coordinates into a single polyline string.

    public String getWaypointsPolyline(ArrayList<ItineraryBusStop> stopsInLine) {
        double previousLat = 0;
        double previousLong = 0;
        StringBuilder poliLyneString = new StringBuilder();
        for (int i = 0; i < stopsInLine.size(); i++) {
            double latChange = stopsInLine.get(i).getBusStop().getLatitude() - previousLat;
            double longChange = stopsInLine.get(i).getBusStop().getLongitude() - previousLong;

            poliLyneString.append(polylineEncode(latChange));
            poliLyneString.append(polylineEncode(longChange));

            previousLat = stopsInLine.get(i).getBusStop().getLatitude();
            previousLong = stopsInLine.get(i).getBusStop().getLongitude();
        }

        return poliLyneString.toString();
    }

    /*
    TODO
    This method is supposerd to make a http request to the Google's Route API

    public void getRoute(BusLine bl) throws Exception {
        ArrayList<ItineraryBusStop> ttrOrder = bl.getSortedTtrStops();
        String originCoordinate = ttrOrder.get(0).getBusStop().getLatitude() + "," + ttrOrder.get(0).getBusStop().getLongitude();
        String destinationCoodinate = ttrOrder.get(ttrOrder.size() - 1).getBusStop().getLatitude() + "," + ttrOrder.get(ttrOrder.size() - 1).getBusStop().getLongitude();
        ttrOrder.remove(0);
        ttrOrder.remove(ttrOrder.size() - 1);

        while (ttrOrder.size() != 23) {
            ttrOrder.remove(ttrOrder.size() - 1);
        }

        String wayPoly = getWaypointsPolyline(ttrOrder);
        String url = DEFAULTURL + "origin=" + originCoordinate + "&destination=" + destinationCoodinate + "&waypoints=enc:" + wayPoly + ":" + "&key=" + APIKEY;
        System.out.print(url);

        //HttpClient client = HttpClientBuilder.create().build();
        //HttpPost postRequest = new HttpPost(url);
        //HttpResponse response = client.execute(postRequest);
    }
    */
}