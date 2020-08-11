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


    public static void readProperties(final Element parent, final BiConsumer<String, String> consumer) {
        if (parent == null) {
            return;
        }
        forEach("property", parent, property -> {
            final Attribute nameAttribute = property.attribute("name");
            final Attribute valueAttribute = property.attribute("value");
            if (nameAttribute != null && valueAttribute != null) {
                final String value = valueAttribute.getValue();
                consumer.accept(nameAttribute.getValue(), value.equals("null") ? null : value);
            }
        });
    }

    public static <T> Element writeProperty(final String name, final T value) {
        final String type = value == null ? null : value.getClass().getSimpleName();
        return writeProperty(name, value == null ? "null" : value, type);
    }

    public static <T> Element writeProperty(@NonNull final String name, @NonNull final T value, final String type) {
        final Element propertyElement = new DefaultElement("property")
                .addAttribute("name", name)
                .addAttribute("value", String.valueOf(value));

        if (type == null || type.isEmpty()) {
            return propertyElement;
        }

        return propertyElement.addAttribute("type", type);
    }

    public static void forEach(final String name, final Element parent, final Consumer<Element> action) {
        final Iterator<Element> elementIterator = Objects.requireNonNull(parent).elementIterator(name);
        if (elementIterator != null) {
            elementIterator.forEachRemaining(action);
        }
    }

    public static Document writeRegister(final List<IndexEntry> projects) {
        final Document configDocument = DocumentHelper.createDocument();
        final Element root = configDocument.addElement("register")
                .addAttribute("version", Main.VERSION);

        final Element projectsNode = root.addElement("entries");
        appendElementList(projectsNode, projects, IndexEntry::createElement);
        return configDocument;
    }

    public static List<IndexEntry> readRegister(final Document document) {
        final Element rootElement = document.getRootElement();
        if (rootElement == null || !rootElement.getName().equals("register")) {
            log.info("Register document didn't have register as root. Continue with project register setup");
            return new ArrayList<>();
        }

        final Element projects = rootElement.element("entries");
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

    public static <T> List<T> readElementList(final String name, final Element parent, final Function<Element, T> action) {
        final ArrayList<T> list = new ArrayList<>();
        forEach(name, parent, element -> list.add(action.apply(element)));
        return list;
    }

    public static <T> void appendElementList(final Element parent, final List<T> list, final Function<T, Element> action) {
        list.stream().map(action).forEach(parent::add);
    }

    public static Element writePoint(final String name, final DiscretePoint point) {
        return new DefaultElement(name)
                .addAttribute("amount", String.valueOf(point.getPositions()))
                .addAttribute("position", String.valueOf(point.getPosition()))
                .addAttribute("distance", String.valueOf(point.getDistance()));
    }

    public static <T> Element simpleElement(final String name, final T content) {
        return new DefaultElement(name).addText(String.valueOf(content));
    }
}
