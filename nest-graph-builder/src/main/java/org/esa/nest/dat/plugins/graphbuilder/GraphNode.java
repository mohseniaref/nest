package org.esa.nest.dat.plugins.graphbuilder;

import com.bc.ceres.binding.*;
import com.thoughtworks.xstream.io.xml.xppdom.Xpp3Dom;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.GPF;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.OperatorUI;
import org.esa.beam.framework.gpf.annotations.ParameterDescriptorFactory;
import org.esa.beam.framework.gpf.graph.Node;
import org.esa.beam.framework.gpf.graph.NodeSource;
import org.esa.beam.framework.gpf.internal.Xpp3DomElement;
import org.esa.beam.framework.gpf.ui.UIValidation;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a node of the graph for the GraphBuilder
 * Stores, saves and loads the display position for the node
 * User: lveci
 * Date: Jan 17, 2008
 */
public class GraphNode {

    private final Node node;
    private final Map<String, Object> parameterMap = new HashMap<String, Object>(10);
    private OperatorUI operatorUI = null;

    private int nodeWidth = 60;
    private int nodeHeight = 30;
    private int halfNodeHeight = 0;
    private int halfNodeWidth = 0;
    static final private int hotSpotSize = 10;
    private int hotSpotOffset = 0;

    private Point displayPosition = new Point(0,0);

    private Xpp3Dom displayParameters;

    GraphNode(final Node n) {
        node = n;
        displayParameters = new Xpp3Dom("node");
        displayParameters.setAttribute("id", node.getId());

        initParameters();
    }

    public void setOperatorUI(final OperatorUI ui) {
        operatorUI = ui;
    }

    public OperatorUI GetOperatorUI() {
        return operatorUI;
    }

    private void initParameters() {

        final OperatorSpi operatorSpi = GPF.getDefaultInstance().getOperatorSpiRegistry().getOperatorSpi(node.getOperatorName());
        if(operatorSpi == null) return;

        final ParameterDescriptorFactory parameterDescriptorFactory = new ParameterDescriptorFactory();
        final ValueContainer valueContainer = ValueContainer.createMapBacked(parameterMap, operatorSpi.getOperatorClass(), parameterDescriptorFactory);

        final Xpp3Dom config = node.getConfiguration();
        final int count = config.getChildCount();
        for (int i = 0; i < count; ++i) {
            final Xpp3Dom child = config.getChild(i);
            final String name = child.getName();
            final String value = child.getValue();
            if(name == null || value == null)
                continue;

            try {
                if(child.getChildCount() == 0) {
                    final Converter converter = getConverter(valueContainer, name);
                    parameterMap.put(name, converter.parse(value));
                } else {
                    final Converter converter = getConverter(valueContainer, name);
                    final Object[] objArray = new Object[child.getChildCount()];
                    int c = 0;
                    for(Xpp3Dom ch : child.getChildren()) {
                        final String v = ch.getValue();

                        objArray[c++] = converter.parse(v);
                    }
                    parameterMap.put(name, objArray);
                }

            } catch(ConversionException e) {
                throw new IllegalArgumentException(name);
            }
        }
    }

    private static Converter getConverter(final ValueContainer valueContainer, final String name) {
        final ValueModel[] models = valueContainer.getModels();

        for (ValueModel model : models) {

            final ValueDescriptor descriptor = model.getDescriptor();
            if(descriptor != null && (descriptor.getName().equals(name) ||
               (descriptor.getAlias() != null && descriptor.getAlias().equals(name)))) {
                return descriptor.getConverter();
            }
        }
        return null;
    }

    void setDisplayParameters(final Xpp3Dom presentationXML) {
        for(Xpp3Dom params : presentationXML.getChildren()) {
            final String id = params.getAttribute("id");
            if(id != null && id.equals(node.getId())) {
                displayParameters = params;
                final Xpp3Dom dpElem = displayParameters.getChild("displayPosition");
                if(dpElem != null) {
                    displayPosition.x = (int)Float.parseFloat(dpElem.getAttribute("x"));
                    displayPosition.y = (int)Float.parseFloat(dpElem.getAttribute("y"));
                }
                return;
            }
        }
    }

    void AssignParameters(final Xpp3Dom presentationXML) {

        final Xpp3DomElement config = Xpp3DomElement.createDomElement("parameters");
        updateParameterMap(config);
        node.setConfiguration(config.getXpp3Dom());

        AssignDisplayParameters(presentationXML);
    }

    void AssignDisplayParameters(final Xpp3Dom presentationXML) {
        Xpp3Dom nodeElem = null;
        for(Xpp3Dom elem : presentationXML.getChildren()) {
            final String id = elem.getAttribute("id");
            if(id != null && id.equals(node.getId())) {
                nodeElem = elem;
                break;
            }
        }
        if(nodeElem == null) {
            presentationXML.addChild(displayParameters);
        }

        Xpp3Dom dpElem = displayParameters.getChild("displayPosition");
        if(dpElem == null) {
            dpElem = new Xpp3Dom("displayPosition");
            displayParameters.addChild(dpElem);
        }

        dpElem.setAttribute("y", String.valueOf(displayPosition.getY()));
        dpElem.setAttribute("x", String.valueOf(displayPosition.getX()));
    }

    /**
     * Gets the display position of a node
     * @return Point The position of the node
     */
    public Point getPos() {
        return displayPosition;
    }

    /**
     * Sets the display position of a node and writes it to the xml
     * @param p The position of the node
     */
    public void setPos(Point p) {
        displayPosition = p;
    }

    public Node getNode() {
        return node;
    }

    public int getWidth() {
        return nodeWidth;
    }

    public int getHeight() {
        return nodeHeight;
    }

    public static int getHotSpotSize() {
        return hotSpotSize;
    }

    public int getHalfNodeWidth() {
        return halfNodeWidth;
    }

    public int getHalfNodeHeight() {
        return halfNodeHeight;
    }

    private void setSize(final int width, final int height) {
        nodeWidth = width;
        nodeHeight = height;
        halfNodeHeight = nodeHeight / 2;
        halfNodeWidth = nodeWidth / 2;
        hotSpotOffset = halfNodeHeight - (hotSpotSize / 2);
    }

    public int getHotSpotOffset() {
        return hotSpotOffset;
    }

    /**
     * Gets the uniqe node identifier.
     * @return the identifier
     */
    public String getID() {
        return node.getId();
    }

    /**
     * Gets the name of the operator.
     * @return the name of the operator.
     */
    public String getOperatorName() {
        return node.getOperatorName();
    }

    public Map<String, Object> getParameterMap() {
        return parameterMap;
    }

    public void connectOperatorSource(final String id) {
        // check if already a source for this node
        disconnectOperatorSources(id);

        final NodeSource ns = new NodeSource("sourceProduct", id);
        node.addSource(ns);
    }

    void disconnectOperatorSources(final String id) {

        for (NodeSource ns : node.getSources()) {
            if (ns.getSourceNodeId().equals(id)) {
                node.removeSource(ns);
            }
        }
    }

    boolean isNodeSource(final GraphNode source) {
            
        for (NodeSource ns : node.getSources()) {
            if (ns.getSourceNodeId().equals(source.getID())) {
                return true;
            }
        }
        return false;
    }

    boolean HasSources() {
        return node.getSources().length > 0;
    }

    public UIValidation validateParameterMap() {
        if(operatorUI != null)
            return operatorUI.validateParameters();
        return new UIValidation(true,"");
    }

    void setSourceProducts(final Product[] products) {
        if(operatorUI != null) {
            operatorUI.setSourceProducts(products);
        }
    }

    void updateParameterMap(final Xpp3DomElement parentElement) {
        if(operatorUI != null) {
            operatorUI.updateParameters();
            operatorUI.convertToDOM(parentElement);
        }
    }

    /**
     * Draw a GraphNode as a rectangle with a name
     * @param g The Java2D Graphics
     * @param col The color to draw
     */
    public void drawNode(final Graphics g, final Color col) {
        final int x = displayPosition.x;
        final int y = displayPosition.y;

        final FontMetrics metrics = g.getFontMetrics();
        final String name = getOperatorName();
        final Rectangle2D rect = metrics.getStringBounds(name, g);
        final int stringWidth = (int) rect.getWidth();
        setSize(Math.max(stringWidth, 50) + 10, 30);

        g.setColor(col);
        g.fill3DRect(x, y, nodeWidth, nodeHeight, true);
        g.setColor(Color.blue);
        g.draw3DRect(x, y, nodeWidth, nodeHeight, true);

        g.setColor(Color.black);
        g.drawString(name, x + (nodeWidth - stringWidth) / 2, y + 20);
    }

    /**
     * Draws the hotspot where the user can join the node to a source node
     * @param g The Java2D Graphics
     * @param col The color to draw
     */
    public void drawHotspot(final Graphics g, final Color col) {
        final Point p = displayPosition;
        g.setColor(col);
        g.drawOval(p.x - hotSpotSize / 2, p.y + hotSpotOffset, hotSpotSize, hotSpotSize);
    }

    /**
     * Draw a line between source and target nodes
     * @param g The Java2D Graphics
     * @param src the source GraphNode
     */
    public void drawConnectionLine(final Graphics g, final GraphNode src) {

        final Point tail = displayPosition;
        final Point head = src.displayPosition;
        if (tail.x + nodeWidth < head.x) {
            drawArrow(g, tail.x + nodeWidth, tail.y + halfNodeHeight,
                    head.x, head.y + src.getHalfNodeHeight());
        } else if (tail.x < head.x + nodeWidth && head.y > tail.y) {
            drawArrow(g, tail.x + halfNodeWidth, tail.y + nodeHeight,
                    head.x + src.getHalfNodeWidth(), head.y);
        } else if (tail.x < head.x + nodeWidth && head.y < tail.y) {
            drawArrow(g, tail.x + halfNodeWidth, tail.y,
                    head.x + src.getHalfNodeWidth(), head.y + nodeHeight);
        } else {
            drawArrow(g, tail.x, tail.y + halfNodeHeight,
                    head.x + src.getWidth(), head.y + src.getHalfNodeHeight());
        }
    }

    /**
     * Draws an arrow head at the correct angle
     * @param g The Java2D Graphics
     * @param tailX position X on target node
     * @param tailY position Y on target node
     * @param headX position X on source node
     * @param headY position Y on source node
     */
    private static void drawArrow(final Graphics g, final int tailX, final int tailY, final int headX, final int headY) {

        final double t1 = Math.abs(headY - tailY);
        final double t2 = Math.abs(headX - tailX);
        double theta = Math.atan(t1 / t2);
        if (headX > tailX) {
            if (headY > tailY)
                theta = Math.PI + theta;
            else
                theta = -(Math.PI + theta);
        } else if (headX < tailX && headY > tailY)
            theta = 2 * Math.PI - theta;
        final double cosTheta = Math.cos(theta);
        final double sinTheta = Math.sin(theta);

        final Point p2 = new Point(-8, -3);
        final Point p3 = new Point(-8, +3);

        int x = (int)Math.round((cosTheta * p2.x) - (sinTheta * p2.y));
        p2.y = (int)Math.round((sinTheta * p2.x) + (cosTheta * p2.y));
        p2.x = x;
        x = (int)Math.round((cosTheta * p3.x) - (sinTheta * p3.y));
        p3.y = (int)Math.round((sinTheta * p3.x) + (cosTheta * p3.y));
        p3.x = x;

        p2.translate(tailX, tailY);
        p3.translate(tailX, tailY);

        g.drawLine(tailX, tailY, headX, headY);
        g.drawLine(tailX, tailY, p2.x, p2.y);
        g.drawLine(p3.x, p3.y, tailX, tailY);
    }

}
