package vista.diagrama;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import modelo.transfers.TransferAtributo;
import modelo.transfers.TransferEntidad;
import modelo.transfers.TransferRelacion;
import vista.tema.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * A renderer that will fill vertex shapes with a GradientPaint
 *
 * @param <V>
 * @param <E>
 * @author Tom Nelson
 */
public class VertexRenderer<V, E> implements Renderer.Vertex<V, E> {

    Color colorOne;
    Color colorTwo;
    PickedState<V> pickedState;
    boolean cyclic;
    private final Theme theme;

    public VertexRenderer(Color colorOne, Color colorTwo, boolean cyclic) {
        this.colorOne = colorOne;
        this.colorTwo = colorTwo;
        this.cyclic = cyclic;
        this.theme = Theme.getInstancia();
    }

    public void paintVertex(RenderContext<V, E> rc, Layout<V, E> layout, V v) {
        Graph<V, E> graph = layout.getGraph();
        if (rc.getVertexIncludePredicate().evaluate(Context.getInstance(graph, v))) {
            boolean vertexHit = true;
            // get the shape to be rendered
            Shape shape = rc.getVertexShapeTransformer().transform(v);

            Point2D p = layout.transform(v);
            p = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p);

            float x = (float) p.getX();
            float y = (float) p.getY();

            // create a transform that translates to the location of
            // the vertex to be rendered
            AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
            // transform the vertex shape with xtransform
            shape = xform.createTransformedShape(shape);
            vertexHit = vertexHit(rc, shape);

            // Si es atributo multivalorado dibujo la segunda elipse
            if (v instanceof TransferAtributo) {
                TransferAtributo atributo = (TransferAtributo) v;
                if (atributo.isMultivalorado()) {
                    Shape outer = atributo.outerShape();
                    AffineTransform xform1 = AffineTransform.getTranslateInstance(x, y);
                    // transform the vertex shape with xtransform
                    outer = xform1.createTransformedShape(outer);
                    paintShapeForVertex(rc, v, outer);
                }
            }
            // Si es entidad débil dibujo el segundo rectángulo
            if (v instanceof TransferEntidad) {
                TransferEntidad entidad = (TransferEntidad) v;
                if (entidad.isDebil()) {
                    Shape outer = entidad.outerShape();
                    AffineTransform xform1 = AffineTransform.getTranslateInstance(x, y);
                    // transform the vertex shape with xtransform
                    outer = xform1.createTransformedShape(outer);
                    paintShapeForVertex(rc, v, outer);
                }
            }
            // Si es relación débil dibujo el segundo rombo
            if (v instanceof TransferRelacion) {
                TransferRelacion relacion = (TransferRelacion) v;
                if (relacion.getTipo().equals("Debil")) {
                    Shape outer = relacion.outerShape();
                    AffineTransform xform1 = AffineTransform.getTranslateInstance(x, y);
                    // transform the vertex shape with xtransform
                    outer = xform1.createTransformedShape(outer);
                    paintShapeForVertex(rc, v, outer);
                }
            }
            if (vertexHit) paintShapeForVertex(rc, v, shape);
        }
    }

    protected boolean vertexHit(RenderContext<V, E> rc, Shape s) {
        JComponent vv = rc.getScreenDevice();
        Rectangle deviceRectangle = null;
        if (vv != null) {
            Dimension d = vv.getSize();
            deviceRectangle = new Rectangle(0, 0, d.width, d.height);
        }
        return rc.getMultiLayerTransformer().getTransformer(Layer.VIEW).transform(s).intersects(deviceRectangle);
    }

    protected void paintShapeForVertex(RenderContext<V, E> rc, V v, Shape shape) {
        GraphicsDecorator g = rc.getGraphicsContext();

        Paint oldPaint = g.getPaint();
        Rectangle r = shape.getBounds();
        float y2 = (float) r.getMaxY();
        if (cyclic) y2 = (float) (r.getMinY() + r.getHeight() / 2);

        Paint fillPaint = null;
        if (v instanceof TransferAtributo)
            fillPaint = new GradientPaint((float) r.getMinX(), (float) r.getMinY(), this.theme.attribute(),
                    (float) r.getMinX(), y2, this.theme.attribute(), cyclic);
        else if (v instanceof TransferRelacion)
            fillPaint = new GradientPaint((float) r.getMinX(), (float) r.getMinY(), this.theme.relation(),
                    (float) r.getMinX(), y2, this.theme.relation(), cyclic);
        else fillPaint = new GradientPaint((float) r.getMinX(), (float) r.getMinY(), this.theme.entity(),
                    (float) r.getMinX(), y2, this.theme.entity(), cyclic);

        //Pinta el interior del elemento
        if (fillPaint != null) {
            g.setPaint(fillPaint);
            g.fill(shape);
            g.setPaint(oldPaint);
        }

        Paint drawPaint = rc.getVertexDrawPaintTransformer().transform(v);
        if (drawPaint != null) g.setPaint(drawPaint);

        Stroke oldStroke = g.getStroke();
        Stroke stroke = rc.getVertexStrokeTransformer().transform(v);
        if (stroke != null) g.setStroke(stroke);
        //Color de los bordes de los elementos seleccionados
        g.setColor(this.theme.lines());
        g.draw(shape);
        g.setPaint(oldPaint);
        g.setStroke(oldStroke);
    }
}
