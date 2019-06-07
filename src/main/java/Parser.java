import beans.BusStop;
import beans.BusStopRelation;
import beans.Itinerary;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import org.w3c.dom.*;
import repositories.BusLineRepository;
import repositories.BusStopRepository;
import repositories.ItineraryRepository;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;

public class Parser {
    public static void main(String[] args) throws Exception {
        BusStopRepository busStopRepository = BusStopRepository.getInstance();
        BusLineRepository busLineRepository = BusLineRepository.getInstance();
        ItineraryRepository itineraryRepository = ItineraryRepository.getInstance();

        busStopRepository.readStopsCSV("src" + File.separatorChar + "main" + File.separatorChar
                + "resources" + File.separatorChar + "busData" + File.separatorChar + "stops.txt");
        busLineRepository.readCSV("src" + File.separatorChar + "main" + File.separatorChar
                + "resources" + File.separatorChar + "busData" + File.separatorChar + "routes.txt");
        itineraryRepository.readCSV("src" + File.separatorChar + "main" + File.separatorChar
                + "resources" + File.separatorChar + "busData" + File.separatorChar + "itineraries"
                + File.separatorChar + "itineraries.txt");

        //File file = new File("planet_-46.641152988710514,-23.792320539855742_-46.53850843470361,-23.567520185337502.osm");
        //extractToCSV(file, "style.xsl", "nodes.csv");

        createCSV();
    }

    private static void createCSV() throws Exception {
        FileReader filereader = new FileReader("nodes.csv");
        CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();
        List<String[]> csvData = csvReader.readAll();

        String csv = "nodes1.csv";
        CSVWriter csvWriter = new CSVWriter(new FileWriter(csv));
        String [] record = "lat,lon,region".split(",");
        csvWriter.writeNext(record);

        BusLineRepository busLineRepository = BusLineRepository.getInstance();
        Itinerary iti = busLineRepository.getByID("423032").getItineraries().get(0);
        List<BusStopRelation> stops = iti.getStops();

        File inputFile = new File("ways.xml");
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(inputFile);
        doc.getDocumentElement().normalize();

        NodeList wayList = doc.getElementsByTagName("way");

        for (int i = 0; i < wayList.getLength(); i++) {
            Node iWay = wayList.item(i);
            if (iWay.getNodeType() == Node.ELEMENT_NODE) {
                Element elementWay = (Element) iWay;
                System.out.println(elementWay.getAttribute("id"));

                NodeList ndList = elementWay.getElementsByTagName("nd");

                for (int j = 0; j < ndList.getLength(); j++) {
                    Element nd = (Element) ndList.item(j);

                    record = (nd.getAttribute("lat") + "," + nd.getAttribute("lon") + "," + elementWay.getAttribute("region")).split(",");
                    csvWriter.writeNext(record);
                }
            }
        }
        csvWriter.close();
    }

    private static void extractToCSV(File file, String style, String  outputFile) throws Exception {
        File stylesheet = new File(style);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document doc = builder.parse(file);

        StreamSource stylesource = new StreamSource(stylesheet);
        Transformer transformer = TransformerFactory.newInstance()
                .newTransformer(stylesource);
        Source source = new DOMSource(doc);
        Result outputTarget = new StreamResult(new File(outputFile));
        transformer.transform(source, outputTarget);
    }

    private static BusStopRelation findNearestStop(String nodeID, List<BusStopRelation> stops) throws Exception {
        FileReader filereader = new FileReader("nodes.csv");
        CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();
        List<String[]> allData = csvReader.readAll();
        for (String[] row : allData) {
            String id = row[0];
            if(id.equals(nodeID)) {
                double nodeLat = Double.parseDouble(row[1]);
                double nodeLon = Double.parseDouble(row[2]);

                BusStopRelation nearest = null;
                double nearestDistance = Double.MAX_VALUE;
                for (int i = 0; i < stops.size(); i++) {
                    BusStop s =  stops.get(i).getBusStop();
                    double distance = arithmeticDistance(nodeLat, s.getLatitude(), nodeLon, s.getLongitude());

                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                        nearest = stops.get(i);
                    }
                }

                return nearest;
            }
        }
        return null;
    }

    private static String[] findNdCoords(String ndID, List<String[]> csvData) throws Exception {
        for (String[] row : csvData) {
            String id = row[0];
            if(id.equals(ndID)) {
                return row;
            }
        }
        return null;
    }

    private static double arithmeticDistance(double x1, double x2, double y1, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    /*
    private static void classifyWays() throws Exception {
        FileReader filereader = new FileReader("nodes.csv");
        CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();
        List<String[]> csvData = csvReader.readAll();

        BusLineRepository busLineRepository = BusLineRepository.getInstance();
        Itinerary iti = busLineRepository.getByID("423032").getItineraries().get(0);
        List<BusStopRelation> stops = iti.getStops();

        File inputFile = new File("ways.xml");
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(inputFile);
        doc.getDocumentElement().normalize();

        NodeList wayList = doc.getElementsByTagName("way");

        for (int i = 0; i < wayList.getLength(); i++) {
            Node iWay = wayList.item(i);
            if (iWay.getNodeType() == Node.ELEMENT_NODE) {
                Element elementWay = (Element) iWay;
                System.out.println(elementWay.getAttribute("id"));

                NodeList ndList = elementWay.getElementsByTagName("nd");

                for (int j = 0; j < ndList.getLength(); j++) {
                    Element nd = (Element) ndList.item(j);
                    String ndID = nd.getAttribute("ref");
                    String[] ndInfo = findNdCoords(ndID, csvData);

                    Attr lat = doc.createAttribute("lat");
                    lat.setValue(ndInfo[1]);
                    nd.setAttributeNode(lat);

                    Attr lon = doc.createAttribute("lon");
                    lon.setValue(ndInfo[2]);
                    nd.setAttributeNode(lon);
                }
            }
        }

        // create the xml file
        //transform the DOM Object to an XML File
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        //transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        //transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC,"yes");
        //transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "10");
        DOMSource domSource = new DOMSource(doc);
        StreamResult streamResult = new StreamResult(new File("ways1.xml"));
        // If you use
        // StreamResult result = new StreamResult(System.out);
        // the output will be pushed to the standard output ...
        // You can use that for debugging
        transformer.transform(domSource, streamResult);
        //System.out.println("Done creating XML File");
    }
    */
    /*
    private static void extractWayXml() throws Exception {
        BusLineRepository busLineRepository = BusLineRepository.getInstance();
        Itinerary iti = busLineRepository.getByID("423032").getItineraries().get(0);
        List<BusStopRelation> stops = iti.getStops();

        File inputFile = new File("planet_-46.641152988710514,-23.792320539855742_-46.53850843470361,-23.567520185337502.osm");
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(inputFile);
        doc.getDocumentElement().normalize();
        NodeList wayList = doc.getElementsByTagName("way");
        NodeList nodeList = doc.getElementsByTagName("node");

        Document newDoc = docBuilder.newDocument();
        Element newDocRoot = newDoc.createElement("osm");
        newDoc.appendChild(newDocRoot);

        for (int i = 0; i < wayList.getLength(); i++) {
            Node iWay = wayList.item(i);
            if (iWay.getNodeType() == Node.ELEMENT_NODE) {
                Element elementWay = (Element) iWay;
                //System.out.print("ID : ");
                System.out.println(elementWay.getAttribute("id"));

                NodeList ndList = elementWay.getElementsByTagName("nd");
                NodeList tagList = elementWay.getElementsByTagName("tag");

                for (int j = 0; j < tagList.getLength(); j++) {
                    Node node1 = tagList.item(j);
                    if (node1.getNodeType() == node1.ELEMENT_NODE) {
                        Element tag = (Element) node1;
                        if (tag.getAttribute("k").equals("highway") && !(tag.getAttribute("v").equals("footway") || tag.getAttribute("v").equals("service"))) {

                            Element newWay = newDoc.createElement("way");

                            Attr id = newDoc.createAttribute("id");
                            id.setValue(elementWay.getAttribute("id"));
                            newWay.setAttributeNode(id);


                            for (int k = 0; k < ndList.getLength(); k++) {
                                Element node = (Element) ndList.item(k);

                                Element newNode = newDoc.createElement("nd");

                                Attr ref = newDoc.createAttribute("ref");
                                ref.setValue(node.getAttribute("ref"));
                                newNode.setAttributeNode(ref);

                                newWay.appendChild(newNode);
                            }

                            for (int l = 0; l < tagList.getLength(); l++) {
                                Element tag1 = (Element) tagList.item(l);

                                Element newTag = newDoc.createElement("tag");

                                Attr k = newDoc.createAttribute("k");
                                k.setValue(tag1.getAttribute("k"));
                                newTag.setAttributeNode(k);

                                Attr v = newDoc.createAttribute("v");
                                v.setValue(tag1.getAttribute("v"));
                                newTag.setAttributeNode(v);

                                newWay.appendChild(newTag);
                            }

                            newDocRoot.appendChild(newWay);
                        }
                    }
                }
            }
        }
        // create the xml file
        //transform the DOM Object to an XML File
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC,"yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "10");
        DOMSource domSource = new DOMSource(newDoc);
        StreamResult streamResult = new StreamResult(new File("ways.xml"));
        // If you use
        // StreamResult result = new StreamResult(System.out);
        // the output will be pushed to the standard output ...
        // You can use that for debugging
        transformer.transform(domSource, streamResult);
        //System.out.println("Done creating XML File");
    }

     */
}