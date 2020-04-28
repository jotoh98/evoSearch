package evo.search.io.entities;

import org.dom4j.Document;

public interface XmlEntity<T> {
    Document serialize();

    T parse(Document document);
}
