package evo.search.io.service;

import evo.search.Main;
import evo.search.io.entities.Project;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

@Slf4j
public class XmlService {

    private static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    private static DocumentBuilder builder;

    static {
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            log.error("Could not create the document builder", e);
        }
    }

    private XmlService() {
    }

    public static Document writeRegister(List<Project> projects) {
        Document configDocument = DocumentHelper.createDocument();
        Element root = configDocument.addElement("config")
                .addAttribute("version", Main.VERSION);

        Element projectsNode = root.addElement("projects");
        projects.forEach(project -> projectsNode.add(createElement(project)));
        return configDocument;
    }

    private static Element createElement(Project project) {
        return new DefaultElement("project")
                .addAttribute("name", project.getName())
                .addAttribute("version", project.getVersion())
                .addAttribute("path", project.getPath());
    }

    private static <T> Document write(T input, Function<T, Element> mapper) {
        return DocumentHelper.createDocument(mapper.apply(input));
    }

    public static Document write(Project project) {
        return write(project, XmlService::createElement);
    }

    public static List<Project> readRegister(Document document) {
        Element rootElement = document.getRootElement();
        if (rootElement == null || !rootElement.getName().equals("config")) {
            log.info("Register document didn't have config as root. Continue with project register setup");
            document = writeRegister(Collections.emptyList());
            rootElement = document.getRootElement();
        }

        Element projects = rootElement.element("projects");
        if (projects == null) {
            return Collections.emptyList();
        }

        ArrayList<Project> projectList = new ArrayList<>();
        for (Iterator<Element> it = projects.elementIterator("project"); it.hasNext(); ) {
            final Element element = it.next();
            final Attribute nameAttribute = element.attribute("name");
            final Attribute versionAttribute = element.attribute("version");
            final Attribute pathAttribute = element.attribute("path");

            if (nameAttribute == null || versionAttribute == null || pathAttribute == null) {
                log.debug("One registered project is empty.");
                continue;
            }
            projectList.add(new Project(nameAttribute.getValue(), versionAttribute.getValue(), pathAttribute.getValue()));
        }

        return projectList;
    }

    public static Document read(File file) {
        SAXReader reader = new SAXReader();
        Document document = DocumentHelper.createDocument();
        try {
            document = reader.read(file);
        } catch (DocumentException | MalformedURLException e) {
            log.error("Could not parse XML file: " + file.getPath(), e);
        }
        return document;
    }

    public static void write(File file, Document document) {
        try (FileWriter fileWriter = new FileWriter(file)) {
            document.write(fileWriter);
        } catch (IOException e) {
            log.error("Could not serialize document in XML file: " + file.getPath(), e);
        }
    }

    private static Iterator<Node> iterator(NodeList nodeList) {
        return new Iterator<>() {
            int currentIndex = -1;

            @Override
            public boolean hasNext() {
                return currentIndex < nodeList.getLength();
            }

            @Override
            public Node next() {
                return nodeList.item(++currentIndex);
            }
        };
    }
}
