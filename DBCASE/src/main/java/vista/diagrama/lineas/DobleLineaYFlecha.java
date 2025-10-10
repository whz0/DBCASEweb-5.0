package vista.diagrama.lineas;

import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import javax.swing.JComponent;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;

public class DobleLineaYFlecha<V,E> {
	//separacion entre las rectas
	private float separac = (float) 2.8;
	Path2D.Double arrow;
	
	public DobleLineaYFlecha(RenderContext<V,E> rc, E e, Graph<V,E> graph, boolean diagonal,
			 float thetaRadians,  
			float xRel, float yRel, float xEnti, float yEnti, double entiSize, GraphicsDecorator g) {

		Shape edgeShape = rc.getEdgeShapeTransformer().transform(Context.<Graph<V,E>,E>getInstance(graph, e));
        Shape edgeShape2 = rc.getEdgeShapeTransformer().transform(Context.<Graph<V,E>,E>getInstance(graph, e));
        Rectangle deviceRectangle = null;
        JComponent vv = rc.getScreenDevice();
        if(vv != null) {
            Dimension d = vv.getSize();
            deviceRectangle = new Rectangle(0,0,d.width,d.height);
        }
		/*
		 * Calculo de los puntos de las dos rectas, esta bastante guay
		 * */
        float angle = getAngle(xRel, yRel, xEnti, yEnti);
		AffineTransform xform = AffineTransform.getTranslateInstance(xRel+Math.sin(angle)*separac, yRel+Math.sin(angle-Math.PI/2)*separac);
		AffineTransform xform2 = AffineTransform.getTranslateInstance(xRel+Math.sin(angle+Math.PI)*separac, yRel+Math.sin(angle+Math.PI/2)*separac);
		//Saca las distancias entre los elementos
		float dx = xEnti-xRel;
		float dy = yEnti-yRel;
		
		thetaRadians = (float) Math.atan2(dy, dx);//Pasa de coordenadas cartesianas a coordenadas polares
		xform.rotate(thetaRadians);// Concatenates this transform with a rotation transformation.
		xform2.rotate(thetaRadians);// Concatenates this transform with a rotation transformation.
		float dist = (float) Math.sqrt(dx*dx + dy*dy);
		if(diagonal) dist = dist -15;
		xform.scale(dist, 1.0);//Concatenates this transform with a scaling transformation.
		xform2.scale(dist, 1.0);//Concatenates this transform with a scaling transformation.
		
		edgeShape = xform.createTransformedShape(edgeShape);
		edgeShape2 = xform2.createTransformedShape(edgeShape2);
		MutableTransformer vt = rc.getMultiLayerTransformer().getTransformer(Layer.VIEW);
		vt.transform(edgeShape).intersects(deviceRectangle);
		vt.transform(edgeShape2).intersects(deviceRectangle);
	    Paint oldPaint = g.getPaint();
	    // get Paints for filling and drawing
	    Paint draw_paint = rc.getEdgeDrawPaintTransformer().transform(e);
	    if (draw_paint != null){
	        g.setPaint(draw_paint);
	        g.draw(edgeShape);
	        g.draw(edgeShape2);
	    }
	    float scalex = (float)g.getTransform().getScaleX();
	    float scaley = (float)g.getTransform().getScaleY();
	    if(scalex < .3 || scaley < .3) return;
	    g.setPaint(oldPaint);
	    AffineTransform at = AffineTransform.getTranslateInstance(xEnti,yEnti);
        at.rotate(thetaRadians);
        at.scale(2.0, 2.0);
        arrow = createArrow((entiSize/2)*Math.cos(thetaRadians*2+Math.PI)-20-(entiSize/2));
        Shape shape = at.createTransformedShape(arrow);
        if (arrow!= null)g.draw(shape);
	}
	/*
	 * Author: John Ericksen @ stackoverflow
	 * */
	private float getAngle(float xRel, float yRel, float xEnti, float yEnti) {
	    float angle = (float) Math.toDegrees(Math.atan2(yRel - yEnti, xRel - xEnti));
	    return (float) Math.toRadians(angle<0?angle += 360:angle);
	}
	
	private Path2D.Double createArrow(double length) {
        int barb = 10;//Tamaño del pico
        double angle = Math.toRadians(20);
        Path2D.Double path = new Path2D.Double();
        double x = length/2 - barb*Math.cos(angle);
        double y = barb*Math.sin(angle);
        path.moveTo(length/2, 0);
        path.lineTo(x, y);//Crear la linea del pico de arriba  ---\
        x = length/2 - barb*Math.cos(-angle);
        y = barb*Math.sin(-angle);
        path.moveTo(length/2, 0);
        path.lineTo(x, y); //Crear la linea del pico de abajo  ---/
        this.arrow = path;
        return path;
    }
}
