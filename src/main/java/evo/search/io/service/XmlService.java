package evo.search.io.service;

import evo.search.Main;
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

/**
 * Utility service for xml-based transformation.
 */
@Slf4j
public class XmlService {

    /**
     * Hidden constructor
     */
    private XmlService() {
    }

    /**
     * Read {@code <property>} elements from a parent and consume their "name" and "value" attributes.
     *
     * @param parent   parent element containing the property elements
     * @param consumer consumer of the "name" and "value" attributes values
     */
    public static void readProperties(final Element parent, final BiConsumer<String, String> consumer) {
        if (parent == null) {
            return;
        }
        forEach("property", parent, property -> readProperty(property, consumer));
    }

    /**
     * Get a {@code <property>} element and consume its "name" and "value" attributes.
     *
     * @param property property element with name and value attributes
     * @param consumer consumer of the "name" and "value" attributes values
     */
    public static void readProperty(final Element property, final BiConsumer<String, String> consumer) {
        final Attribute nameAttribute = property.attribute("name");
        final Attribute valueAttribute = property.attribute("value");
        if (nameAttribute != null && valueAttribute != null) {
            final String value = valueAttribute.getValue();
            consumer.accept(nameAttribute.getValue(), value.equals("null") ? null : value);
        }
    }

    /**
     * Create a {@code <property>} element with "name", "value" and "type" attributes.
     * The type is inferred through the value.
     *
     * @param name  name of the property
     * @param value value of the property
     * @param <T>   type of the property's value
     * @return property element with "name", "value" and "type" attributes
     * @see #writeProperty(String, Object, String)
     */
    public static <T> Element writeProperty(final String name, final T value) {
        final String type = value == null ? null : value.getClass().getSimpleName();
        return writeProperty(name, value == null ? "null" : value, type);
    }

    /**
     * Create a {@code <property>} element with "name", "value" and "type" attributes.
     *
     * @param name  name of the property
     * @param value value of the property
     * @param type  type name of the property's value
     * @param <T>   type of the property's value
     * @return property element with "name", "value" and "type" attributes
     */
    public static <T> Element writeProperty(@NonNull final String name, @NonNull final T value, final String type) {
        final Element propertyElement = new DefaultElement("property")
                .addAttribute("name", name)
                .addAttribute("value", String.valueOf(value));

        if (type == null || type.isEmpty()) {
            return propertyElement;
        }

        return propertyElement.addAttribute("type", type);
    }

    /**
     * Iterate a parent element's children by their name and consume these elements.
     *
     * @param name   name of the children elements
     * @param parent parent element
     * @param action consumer of the children
     */
    public static void forEach(final String name, final Element parent, final Consumer<Element> action) {
        final Iterator<?> elementIterator = Objects.requireNonNull(parent).elementIterator(name);
        if (elementIterator != null) {
            elementIterator.forEachRemaining(o -> {
                if (o instanceof Element) action.accept((Element) o);
            });
        }
    }

    /**
     * Create the {@link Document} for the global project register.
     *
     * @param projects list of project index entries
     * @return document containing the serialized index entries
     */
    public static Document writeRegister(final List<IndexEntry> projects) {
        final Document configDocument = DocumentHelper.createDocument();
        final Element root = configDocument.addElement("register")
                .addAttribute("version", Main.VERSION);

        final Element projectsNode = root.addElement("entries");
        appendElementList(projectsNode, projects, IndexEntry::createElement);
        return configDocument;
    }

    /**
     * Parse the {@link Document} for the global project register.
     *
     * @param document document to parse
     * @return list of project index entries
     */
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

    /**
     * Iterate a parent element's children by their name and map these elements to their values.
     *
     * @param name   name of the children elements
     * @param parent parent element
     * @param action mapper of the children
     * @param <T>    type of the element's value
     * @return list of the parsed elements values
     */
    public static <T> List<T> readElementList(final String name, final Element parent, final Function<Element, T> action) {
        final ArrayList<T> list = new ArrayList<>();
        forEach(name, parent, element -> list.add(action.apply(element)));
        return list;
    }

    /**
     * Append a list of mapped values to a parent element.
     *
     * @param parent parent element to append to
     * @param list   list of value to map and append
     * @param action mapper function to create elements from the given values
     * @param <T>    type of the values
     */
    public static <T> void appendElementList(final Element parent, final List<T> list, final Function<T, Element> action) {
        list.stream().map(action).forEach(parent::add);
    }

    /**
     * Create a simple element with name and content.
     *
     * @param name    name of the simple element
     * @param content content of the simple element to stringify
     * @param <T>     type of the content value
     * @return element with name and string content
     */
    public static <T> Element simpleElement(final String name, final T content) {
        return new DefaultElement(name).addText(String.valueOf(content));
    }

}
