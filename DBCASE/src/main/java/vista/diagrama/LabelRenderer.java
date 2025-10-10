package vista.diagrama;

import java.awt.Color;

/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 * Created on Aug 23, 2005
 */

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.renderers.VertexLabelRenderer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import modelo.transfers.Transfer;
import vista.tema.Theme;

/**
 * Renders Vertex Labels, but can also supply Shapes for vertices.
 * This has the effect of making the vertex label the actual vertex
 * shape. The user will probably want to center the vertex label
 * on the vertex location.
 * 
 * @author Tom Nelson
 *
 * @param <V>
 * @param <E>
 */
public class LabelRenderer<V,E> implements edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel<V,E>, Transformer<V,Shape>{

	protected Map<V,Shape> shapes = new HashMap<V,Shape>();

	public Component prepareRenderer(RenderContext<V,E> rc, VertexLabelRenderer graphLabelRenderer, Object value, boolean isSelected, V vertex) {
		return rc.getVertexLabelRenderer().<V>getVertexLabelRendererComponent(rc.getScreenDevice(), value, 
				rc.getVertexFontTransformer().transform(vertex), isSelected, vertex);
	}

	/**
	 * Labels the specified vertex with the specified label.  
	 * Uses the font specified by this instance's 
	 * <code>VertexFontFunction</code>.  (If the font is unspecified, the existing
	 * font for the graphics context is used.)  If vertex label centering
	 * is active, the label is centered on the position of the vertex; otherwise
	 * the label is offset slightly.
	 */
	public void labelVertex(RenderContext<V,E> rc, Layout<V,E> layout, V v, String label) {
		
		Theme theme = Theme.getInstancia();
		/*Cambia la fuente de los elementos*/
		rc.setVertexFontTransformer(new Transformer<V,Font>(){
			@Override public Font transform(V arg0) {return theme.font();}
		});
		
		/*Cambia el color de las uniones entre elementos*/
		rc.setEdgeDrawPaintTransformer(new Transformer<E, Paint>() {
			@Override public Paint transform(E arg0){return theme.lines();}
		});
		
		/*Cambia el color del borde de los elementos*/
		rc.setVertexDrawPaintTransformer(new Transformer<V, Paint>() {
			@Override public Paint transform(V arg0){return Color.black;}
		});
		
		Graph<V,E> graph = layout.getGraph();
		
		if (rc.getVertexIncludePredicate().evaluate(Context.<Graph<V,E>,V>getInstance(graph,v)) == false) return;
		
		GraphicsDecorator g = rc.getGraphicsContext();
		Component component = prepareRenderer(rc, rc.getVertexLabelRenderer(), label,
				rc.getPickedVertexState().isPicked(v), v);
		Dimension d = component.getPreferredSize();
		
		int h_offset = -d.width / 2;
		int v_offset = -d.height / 2;

		Point2D p = layout.transform(v);
		p = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p);

		int x = (int)p.getX();
		int y = (int)p.getY();
		
		g.draw(component, rc.getRendererPane(), x+h_offset, y+v_offset, d.width, d.height, true);
		
	}

	public Shape transform(V v) {
		return (Shape)((Transfer) v).toShape();
	}

	public edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position getPosition() {
		return edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position.CNTR;
	}

	public edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Positioner getPositioner() {
		return new Positioner() {
			public edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position getPosition(float x, float y, Dimension d) {
				return edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position.CNTR;
			}};
	}

	public void setPosition(edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position position) {}

	public void setPositioner(edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Positioner positioner) {}
}
