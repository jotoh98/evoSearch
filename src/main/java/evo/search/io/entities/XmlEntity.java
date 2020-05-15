package evo.search.io.entities;

import org.dom4j.Element;

public interface XmlEntity {
    Element serialize();

    void parse(Element element);
}
