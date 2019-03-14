package services;

import beans.BusStop;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public abstract class APIRequester {

    public abstract JSONArray requestRoute(List<BusStop> route) throws Exception;

    public abstract JSONObject requestWalkingRoute(double originLat, double originLong, double destinationLat, double destinationLong) throws Exception;

    /*
     * Convert a series of waypoint coordinates into a single polyline string.
     */
    protected String getWaypointsPolyline(List<BusStop> stopsInItinerary) {
        double previousLat = 0;
        double previousLong = 0;
        StringBuilder poliLyneString = new StringBuilder();
        for (int i = 0; i < stopsInItinerary.size(); i++) {
            double lat = stopsInItinerary.get(i).getLatitude();
            double longi = stopsInItinerary.get(i).getLongitude();
            double latChange = lat - previousLat;
            double longChange = longi - previousLong;

            poliLyneString.append(polylineEncode(latChange));
            poliLyneString.append(polylineEncode(longChange));

            previousLat = lat;
            previousLong = longi;
        }

        return poliLyneString.toString();
    }

    //Encode a double value to the Google's Encoded Polyline Algorithm Format
    protected String polylineEncode(double value) {
        //Take the decimal value and multiply it by 1e5
        int round = (int) Math.round(value * 1e5);

        if (round == 0) {
            return "?";
        }

        //Convert the decimal value to binary. Note that a negative value must be calculated using its two's complement.
        StringBuilder binaryString = new StringBuilder(Integer.toBinaryString(round));
        if (value < 0) {
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

        /*
         * Break the binary value out into 5-bit chunks (starting from the right hand side)
         * and place the 5-bit chunks into reverse order
         */
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
        for (int n = 0; n < chunk2.length(); n++) {
            if (chunk2.charAt(n) == '1' || chunk3.charAt(n) == '1' || chunk4.charAt(n) == '1' || chunk5.charAt(n) == '1' || chunk6.charAt(n) == '1') {
                chunk1.insert(0, '1');
                break;
            }
        }
        if (chunk1.length() == 5) {
            chunk1.insert(0, '0');
        }
        for (int n = 0; n < chunk3.length(); n++) {
            if (chunk3.charAt(n) == '1' || chunk4.charAt(n) == '1' || chunk5.charAt(n) == '1' || chunk6.charAt(n) == '1') {
                chunk2.insert(0, '1');
                break;
            }
        }
        if (chunk2.length() == 5) {
            chunk2.insert(0, '0');
        }
        for (int n = 0; n < chunk4.length(); n++) {
            if (chunk4.charAt(n) == '1' || chunk5.charAt(n) == '1' || chunk6.charAt(n) == '1') {
                chunk3.insert(0, '1');
                break;
            }
        }
        if (chunk3.length() == 5) {
            chunk3.insert(0, '0');
        }
        for (int n = 0; n < chunk5.length(); n++) {
            if (chunk5.charAt(n) == '1' || chunk6.charAt(n) == '1') {
                chunk4.insert(0, '1');
                break;
            }
        }
        if (chunk4.length() == 5) {
            chunk4.insert(0, '0');
        }
        for (int n = 0; n < chunk6.length(); n++) {
            if (chunk6.charAt(n) == '1') {
                chunk5.insert(0, '1');
                break;
            }
        }
        if (chunk5.length() == 5) {
            chunk5.insert(0, '0');
        }
        chunk6.insert(0, '0');

        /*
         * Convert each value to decimal
         * and add 63 to each value
         */
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
}
