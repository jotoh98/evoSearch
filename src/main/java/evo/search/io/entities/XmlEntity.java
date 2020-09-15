package evo.search.io.entities;

import org.dom4j.Element;

/**
 * Entity interface for objects which can be parsed from and converted to xml.
 *
 * @param <T> return type of the parsed {@link Element}
 */
public interface XmlEntity<T> {

    /**
     * Serialize the entity into a {@link Element}.
     *
     * @return serialized document
     */
    Element serialize();

    /**
     * Parse a {@link Element} and return the entity.
     *
     * @param element element to parse
     * @return parsed entity
     */
    T parse(Element element);

}
