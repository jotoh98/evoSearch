package evo.search.io.entities;

import evo.search.io.service.XmlService;
import lombok.Data;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

import java.awt.*;

/**
 * The window workspace used to preserve the projects windows locations and settings.
 *
 * @author jotoh
 */
@Data
public class Workspace implements XmlEntity<Workspace> {

    /**
     * Location of the main forms window.
     */
    Point mainLocation = new Point();

    /**
     * Dimensions of the main forms window.
     */
    Dimension mainSize = new Dimension();

    /**
     * Location of the config panels window.
     */
    Point configLocation = new Point();

    /**
     * Dimensions of the config panels window.
     */
    Dimension configSize = new Dimension();

    /**
     * Index of the configuration selected in the {@link evo.search.view.MainForm}.
     */
    int selectedConfiguration = 1;

    /**
     * Divider location of the main split pane (vertical).
     */
    int mainDividerLocation = 50;

    /**
     * Divider location of the log split pane (horizontal).
     */
    int logDividerLocation = 50;

    /**
     * Serialize a {@link Point} to a xml element.
     * The result element looks like this: {@code <[name] x="[x]" y="[y]"/>}.
     *
     * @param name  name of this dimensions element
     * @param point point to serialize
     * @return serialized point element
     */
    public static Element serialize(final String name, final Point point) {
        return new DefaultElement(name)
                .addAttribute("x", String.valueOf(point.x))
                .addAttribute("y", String.valueOf(point.y));
    }

    /**
     * Serialize a {@link Dimension} to a xml element.
     * The result element looks like this: {@code <[name] width="[w]" height="[h]"/>}.
     *
     * @param name      name of this dimensions element
     * @param dimension dimension to serialize
     * @return serialized dimension element
     */
    public static Element serialize(final String name, final Dimension dimension) {
        return new DefaultElement(name)
                .addAttribute("width", String.valueOf(dimension.width))
                .addAttribute("height", String.valueOf(dimension.height));
    }

    /**
     * Serialize the workspace to a xml {@link Document}.
     * The root element is named <em>workspace</em>.
     *
     * @return serialized xml document from this workspace
     */
    @Override
    public Document serialize() {
        final Element workspace = new DefaultElement("workspace");

        workspace.add(serialize("mainLocation", mainLocation));
        workspace.add(serialize("mainSize", mainSize));
        workspace.add(XmlService.writeProperty("configSelected", selectedConfiguration));
        workspace.add(XmlService.writeProperty("logDivider", logDividerLocation));
        workspace.add(XmlService.writeProperty("mainDivider", mainDividerLocation));
        workspace.add(serialize("configLocation", configLocation));
        workspace.add(serialize("configSize", configSize));

        return DocumentHelper.createDocument(workspace);
    }

    /**
     * Parse a {@link Point} from a {@link Element}.
     *
     * @param element element to parse
     * @return parsed point
     */
    public static Point parsePoint(final Element element) {
        final Attribute xAttr = element.attribute("x");
        final Attribute yAttr = element.attribute("y");
        final int x = xAttr == null ? 0 : Integer.parseInt(xAttr.getValue());
        final int y = yAttr == null ? 0 : Integer.parseInt(yAttr.getValue());
        return new Point(x, y);
    }

    /**
     * Parse a {@link Dimension} from a {@link Element}.
     *
     * @param element element to parse
     * @return parsed dimension
     */
    public static Dimension parseDimension(final Element element) {
        final Attribute wAttr = element.attribute("width");
        final Attribute hAttr = element.attribute("height");
        final int width = wAttr == null ? 0 : Integer.parseInt(wAttr.getValue());
        final int height = hAttr == null ? 0 : Integer.parseInt(hAttr.getValue());
        return new Dimension(width, height);
    }

    /**
     * Parse a workspace from a {@link Document}.
     *
     * @param document document to parse
     * @return parsed workspace
     */
    @Override
    public Workspace parse(final Document document) {
        final Workspace workspace = new Workspace();

        final Element rootElement = document.getRootElement();
        if (rootElement == null) return workspace;

        rootElement.elementIterator().forEachRemaining(object -> {
            if (object instanceof Element) {
                final Element element = (Element) object;
                switch (element.getName()) {
                    case "mainLocation":
                        mainLocation = parsePoint(element);
                        break;
                    case "mainSize":
                        mainSize = parseDimension(element);
                        break;
                    case "configLocation":
                        configLocation = parsePoint(element);
                        break;
                    case "configSize":
                        configSize = parseDimension(element);
                        break;
                    case "property":
                        XmlService.readProperty(
                                element,
                                (name, value) -> {
                                    switch (name) {
                                        case "configSelected":
                                            selectedConfiguration = Integer.parseInt(value);
                                            break;
                                        case "logDivider":
                                            logDividerLocation = Integer.parseInt(value);
                                            break;
                                        case "mainDivider":
                                            mainDividerLocation = Integer.parseInt(value);
                                    }
                                }
                        );
                }
            }
        });

        return this;
    }

}
