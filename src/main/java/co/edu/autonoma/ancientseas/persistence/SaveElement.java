package co.edu.autonoma.ancientseas.persistence;

import co.edu.autonoma.ancientseas.error.CorruptSaveException;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

final class SaveElement {

    private final Element element;

    SaveElement(Element element) {
        this.element = element;
    }

    boolean has(String attribute) {
        return element.hasAttribute(attribute);
    }

    String text(String attribute) throws CorruptSaveException {
        if (!element.hasAttribute(attribute)) {
            throw corrupt("is missing attribute '" + attribute + "'", null);
        }
        return element.getAttribute(attribute);
    }

    double number(String attribute) throws CorruptSaveException {
        String raw = text(attribute);
        try {
            double value = Double.parseDouble(raw);
            if (!Double.isFinite(value)) {
                throw corrupt("has a non-finite number in '" + attribute + "': " + raw, null);
            }
            return value;
        } catch (NumberFormatException e) {
            throw corrupt("has an unreadable number in '" + attribute + "': " + raw, e);
        }
    }

    long integer(String attribute) throws CorruptSaveException {
        String raw = text(attribute);
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            throw corrupt("has an unreadable integer in '" + attribute + "': " + raw, e);
        }
    }

    boolean flag(String attribute) throws CorruptSaveException {
        String raw = text(attribute);
        if (!"true".equals(raw) && !"false".equals(raw)) {
            throw corrupt("has an unreadable boolean in '" + attribute + "': " + raw, null);
        }
        return Boolean.parseBoolean(raw);
    }

    <E extends Enum<E>> E constant(String attribute, Class<E> type) throws CorruptSaveException {
        String raw = text(attribute);
        try {
            return Enum.valueOf(type, raw);
        } catch (IllegalArgumentException e) {
            throw corrupt("has an unknown " + type.getSimpleName() + " in '" + attribute + "': " + raw, e);
        }
    }

    SaveElement child(String tag) throws CorruptSaveException {
        List<SaveElement> found = children(tag);
        if (found.isEmpty()) {
            throw corrupt("is missing child element '" + tag + "'", null);
        }
        return found.get(0);
    }

    List<SaveElement> children(String tag) {
        List<SaveElement> found = new ArrayList<>();
        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element child && child.getTagName().equals(tag)) {
                found.add(new SaveElement(child));
            }
        }
        return found;
    }

    String content() {
        return element.getTextContent();
    }

    private CorruptSaveException corrupt(String detail, Throwable cause) {
        return new CorruptSaveException("Element <" + element.getTagName() + "> " + detail, cause);
    }
}
