package evo.search.io.service;

import evo.search.Main;
import evo.search.ga.DiscretePoint;
import evo.search.io.entities.IndexEntry;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class XmlService {

    private XmlService() {
    }


    public static void readProperties(Element parent, BiConsumer<String, String> consumer) {
        if (parent == null) {
            return;
        }
        forEach("property", parent, property -> {
            final Attribute nameAttribute = property.attribute("name");
            final Attribute valueAttribute = property.attribute("value");
            if (nameAttribute != null && valueAttribute != null) {
                String value = valueAttribute.getValue();
                consumer.accept(nameAttribute.getValue(), value.equals("null") ? null : value);
            }
        });
    }

    public static <T> Element writeProperty(String name, T value) {
        String type = value == null ? null : value.getClass().getSimpleName();
        return writeProperty(name, value == null ? "null" : value, type);
    }

    public static <T> Element writeProperty(@NonNull String name, @NonNull T value, String type) {
        Element propertyElement = new DefaultElement("property")
                .addAttribute("name", name)
                .addAttribute("value", String.valueOf(value));

        if (type == null || type.isEmpty()) {
            return propertyElement;
        }

        return propertyElement.addAttribute("type", type);
    }

    public static void forEach(String name, Element parent, Consumer<Element> action) {
        Iterator<Element> elementIterator = Objects.requireNonNull(parent).elementIterator(name);
        if (elementIterator != null) {
            elementIterator.forEachRemaining(action);
        }
    }

    public static Document writeRegister(List<IndexEntry> projects) {
        Document configDocument = DocumentHelper.createDocument();
        Element root = configDocument.addElement("register")
                .addAttribute("version", Main.VERSION);

        Element projectsNode = root.addElement("entries");
        appendElementList(projectsNode, projects, IndexEntry::createElement);
        return configDocument;
    }

    public static List<IndexEntry> readRegister(Document document) {
        Element rootElement = document.getRootElement();
        if (rootElement == null || !rootElement.getName().equals("register")) {
            log.info("Register document didn't have register as root. Continue with project register setup");
            return new ArrayList<>();
        }

        Element projects = rootElement.element("entries");
        if (projects == null) {
            return Collections.emptyList();
        }

        return readElementList("entry", projects, IndexEntry::parse).stream()
                .peek(project -> {
                    if (project == null) {
                        log.info("A project is broken. It will be deleted.");
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static <T> List<T> readElementList(String name, Element parent, Function<Element, T> action) {
        ArrayList<T> list = new ArrayList<>();
        forEach(name, parent, element -> list.add(action.apply(element)));
        return list;
    }

    public static <T> void appendElementList(Element parent, List<T> list, Function<T, Element> action) {
        list.stream().map(action).forEach(parent::add);
    }

    public static Element writePoint(String name, DiscretePoint point) {
        return new DefaultElement(name)
                .addAttribute("position", String.valueOf(point.getPosition()))
                .addAttribute("distance", String.valueOf(point.getDistance()));
    }

    public static <T> Element simpleElement(String name, T content) {
        return new DefaultElement(name).addText(String.valueOf(content));
    }
}
