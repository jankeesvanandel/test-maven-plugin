package nl.jkva;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.maven.plugin.MojoExecutionException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class XmlReader {

    public static class ArtifactConfig {
        public final String groupId;
        public final String artifactId;
        public final String type;
        public final String version;

        public ArtifactConfig(String groupId, String artifactId, String type, String version) {
            this.groupId = groupId;

            this.artifactId = artifactId;
            this.type = type;
            this.version = version;
        }

    }

    public static List<ArtifactConfig> listAllArtifacts(File file) throws MojoExecutionException {
        try {
            String xmlString = Files.toString(file, Charsets.UTF_8);
            List<ArtifactConfig> config = parseXML(xmlString);
            return config;
        } catch (IOException e) {
            throw new MojoExecutionException("Error: ", e);
        } catch (XMLStreamException e) {
            throw new MojoExecutionException("Error: ", e);
        }
    }

    public static List<ArtifactConfig> parseXML(String xml)
            throws XMLStreamException, UnsupportedEncodingException {
        byte[] byteArray = xml.getBytes("UTF-8");
        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLEventReader reader = inputFactory.createXMLEventReader(inputStream);

        List<ArtifactConfig> config = new ArrayList<ArtifactConfig>();

        while (reader.hasNext()) {
            XMLEvent event = (XMLEvent) reader.next();

            if (event.isStartElement()) {
                StartElement element = event.asStartElement();

                String elementName = element.getName().getLocalPart();
                if (elementName.equals("artifact")) {
                    ArtifactConfig artifactConfig = new ArtifactConfig(
                            getAttribute(element, "groupId"),
                            getAttribute(element, "artifactId"),
                            getAttribute(element, "type"),
                            getAttribute(element, "version"));
                    config.add(artifactConfig);
                } else if (elementName.equals("delivery")) {
                    continue;
                } else {
                    throw new RuntimeException("Unknown element: " + element.getName());
                }
            }
        }

        return config;
    }

    private static String getAttribute(StartElement element, String groupId) {
        return element.getAttributeByName(QName.valueOf(groupId)).getValue();
    }
}
