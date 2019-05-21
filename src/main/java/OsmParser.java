import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;

public class OsmParser {
    public static void main(String[] args) throws Exception {
        File file = new File("planet_-46.641152988710514,-23.792320539855742_-46.53850843470361,-23.567520185337502.osm");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document doc = builder.parse(file);
        doc.getDocumentElement().normalize();

        System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
        NodeList nList = doc.getElementsByTagName("node");
        NodeList wList = doc.getElementsByTagName("way");

    }
}