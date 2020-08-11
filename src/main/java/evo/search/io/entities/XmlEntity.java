package evo.search.io.entities;

import org.dom4j.Document;

/**
 * Entity interface for objects which can be parsed from and converted to xml.
 *
 * @param <T> return type of the parsed {@link Document}
 */
public interface XmlEntity<T> {

    /**
     * Serialize the entity into a {@link Document}.
     *
     * @return serialized document
     */
    Document serialize();

    /**
     * Parse a {@link Document} and return the entity.
     *
     * @param document document to parse
     * @return parsed entity
     */
    T parse(Document document);

}
