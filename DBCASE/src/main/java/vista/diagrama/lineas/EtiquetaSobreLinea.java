package vista.diagrama.lineas;

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
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.renderers.EdgeLabelRenderer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import modelo.transfers.TransferAtributo;
import modelo.transfers.TransferEntidad;
import modelo.transfers.TransferRelacion;

public class EtiquetaSobreLinea<V,E> extends edu.uci.ics.jung.visualization.renderers.BasicEdgeLabelRenderer<V,E>{	
	
	@Override
	public Component prepareRenderer(RenderContext<V,E> rc, EdgeLabelRenderer graphLabelRenderer, Object value, 
			boolean isSelected, E edge) {
		return rc.getEdgeLabelRenderer().<E>getEdgeLabelRendererComponent(rc.getScreenDevice(), value, 
				rc.getEdgeFontTransformer().transform(edge), isSelected, edge);
	}
	    
    @Override
    public void labelEdge(RenderContext<V,E> rc, Layout<V,E> layout, E e, String label) {
    	if(label == null || label.length() == 0) return;
    	
    	Graph<V,E> graph = layout.getGraph();
        // don't draw edge if either incident vertex is not drawn
        Pair<V> endpoints = graph.getEndpoints(e);
        V v1 = endpoints.getFirst();
        V v2 = endpoints.getSecond();
        if (!rc.getVertexIncludePredicate().evaluate(Context.<Graph<V,E>,V>getInstance(graph,v1)) || 
            !rc.getVertexIncludePredicate().evaluate(Context.<Graph<V,E>,V>getInstance(graph,v2)))
            return;

        Point2D p1 = layout.transform(v1);
        Point2D p2 = layout.transform(v2);
        p1 = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p1);
        p2 = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p2);
        float x1 = (float) p1.getX();
        float y1 = (float) p1.getY();
        float x2 = (float) p2.getX();
        float y2 = (float) p2.getY();

        GraphicsDecorator g = rc.getGraphicsContext();
        float distX = x2 - x1;
        float distY = y2 - y1;
        double totalLength = Math.sqrt(distX * distX + distY * distY);

        double closeness = rc.getEdgeLabelClosenessTransformer().transform(Context.<Graph<V,E>,E>getInstance(graph, e)).doubleValue();

        int posX = (int) (x1 + (closeness) * distX);
        int posY = (int) (y1 + (closeness) * distY);

        int xDisplacement = (int) (rc.getLabelOffset() * (distY / totalLength));
        int yDisplacement = (int) (rc.getLabelOffset() * (-distX / totalLength));
        Component component = prepareRenderer(rc, rc.getEdgeLabelRenderer(), label, rc.getPickedEdgeState().isPicked(e), e);
        Dimension d = component.getPreferredSize();
        Shape edgeShape = rc.getEdgeShapeTransformer().transform(Context.<Graph<V,E>,E>getInstance(graph, e));
        Collection<E> aris= graph.getEdges();
        ArrayList<Pair<V>> lista= new ArrayList<Pair<V>>();//Lista que representa el grafo
        Pair<V> par;
        int numApariciones= 0;//Numero de veces que aparece la arista a dibujar en el grafo
        String nom1="";
        String nom2="";
        int tipo1=0, tipo2=0;
        
        for (E o : aris){
        	par = graph.getEndpoints(o);
        	if (endpoints.equals(par))//Lo que voy a añadir en la lista es igual que la arista que voy a pintar
        		numApariciones++;
        	lista.add(par);
        	//Para la distribución de las lineas de los roles
        	if(par.getFirst() instanceof TransferRelacion){
        		TransferRelacion nombre1 = (TransferRelacion)par.getFirst();
        		nom1 = nombre1.toString();
        		tipo1=1;//Relación
        	}
        	if(par.getFirst() instanceof TransferEntidad){
        		TransferEntidad nombre1 = (TransferEntidad)par.getFirst();
        		nom1 = nombre1.toString();
        		tipo1=2;//Entidad
        	}
        	if(par.getFirst() instanceof TransferAtributo){
        		TransferAtributo nombre1 = (TransferAtributo)par.getFirst();
        		nom1 = nombre1.toString();
        		tipo1=3;//Atributo
        	}
        	if(par.getSecond() instanceof TransferRelacion){
        		TransferRelacion nombre2 = (TransferRelacion)par.getSecond();
        		nom2 = nombre2.toString();
        		tipo2=1;//Relación
        	}
        	if(par.getSecond() instanceof TransferEntidad){
        		TransferEntidad nombre2 = (TransferEntidad)par.getSecond();
        		nom2 = nombre2.toString();
        		tipo2=2;//Entidad
        	}
        	if(par.getSecond() instanceof TransferAtributo){
        		TransferAtributo nombre2 = (TransferAtributo)par.getSecond();
        		nom2 = nombre2.toString();
        		tipo2=3;//Atributo
        	}
        } //Fin del bucle  
        //Coordenadas de los centros de la entidad y de la relación
        float xRela = (float) p1.getX();//Coordenada x de la Relacion
       	float yRela = (float) p1.getY();//Coordenada y de la Relacion
       	float xEnti = (float) p2.getX();//Coordenada x de la entidad	        	        
       	float yEnti= (float) p2.getY();//Coordenada y de la entidad
        //parte de código nueva para las líneas de los roles
        float xCentro;
       	float yCentro;
       	float xNoCentro;
       	float yNoCentro;	       	
       	
       	int anchoNoCentro=0, altoNoCentro=0;
        //Calculo el ancho mínimo entre la relación y la entidad.
		int ancho = Math.min(nom1.length(),nom2.length());
        //Hay que saber si el ancho mínimo es de la entidad o de la relación
        if(ancho == nom1.length()){
        	if (tipo1==1){
        		xCentro = xRela;
        		yCentro = yRela;
        		xNoCentro = xEnti;
        		yNoCentro = yEnti;
        		anchoNoCentro = nom2.length();
        	}
        	else if(tipo1==3){
        		xCentro = xRela;
        		yCentro = yRela;
        		xNoCentro = xEnti;
        		yNoCentro = yEnti;
        		anchoNoCentro = nom2.length();
        	}
        	else{
        		xCentro = xRela;
        		yCentro = yRela;
        		xNoCentro = xEnti;
        		yNoCentro = yEnti;
        		anchoNoCentro = nom2.length();
        	}
        }
        else{
        	//Es una relacion
        	if (tipo2==1){
        		xCentro = xEnti;
        		yCentro = yEnti;
        		xNoCentro = xRela;
        		yNoCentro = yRela;
        		anchoNoCentro=nom1.length();
        	}
        	//Es un atributo
        	else if(tipo2==3){
        		xCentro = xRela;
        		yCentro = yRela;
        		xNoCentro = xEnti;
        		yNoCentro = yEnti;
        		anchoNoCentro=nom2.length();
        	}
        	//Es una entidad
        	else{
        		xCentro = xRela;
        		yCentro = yRela;
        		xNoCentro = xEnti;
        		yNoCentro = yEnti;
        		anchoNoCentro=nom2.length();
        	}
        	//anchoNoCentro=nombre2.length();//habia un 1
        }
        //Si el ancho  es menor que 8 la figura tiene un tamaño fijo
        if(ancho < 8) ancho = 45;
        //Si no el ancho es proporcional a la longitud del nombre
        else ancho = (ancho *5) +5;
        //Si el ancho de la otra figura es menor que 8 la figura tiene un tamaño fijo
        if(anchoNoCentro < 8){
        	anchoNoCentro = 45;
        	altoNoCentro = 20;
        }
        //Si no el ancho de la otra figura es proporcional a la longitud del nombre
        else{
        	anchoNoCentro = (anchoNoCentro *5) +5;
        	altoNoCentro = 20;
        }
        
        //Dependiendo de la situación relativa entre la entidad y la relación se colocarán más cerca o más lejos las etiquetas
        int offset=0;
        //Si no hay que ajustar las líneas de los roles para que se vean correctamente
        if (numApariciones > 1){
        	double incrementoX = 0;
        	int epsilon = ancho /4;
        	/*La separación entre las líneas de los roles es proporcional al número de veces que participe la entidad en la relación
        	 * y la posición relativa entre la entidad y la relacion*/
        	if ((((xNoCentro+anchoNoCentro)>=(xCentro-epsilon))&&((xNoCentro-anchoNoCentro) <= xCentro+epsilon)) ||
        		((xNoCentro>=(xCentro-epsilon))&&(xNoCentro <= xCentro+epsilon))){
        		//Están a distintas alturas pero en la misma franja de las xs
        		//Coloco el offset de las etiquetas 
        		if(rc.getParallelEdgeIndexFunction().getIndex(graph, e)==0)
        			offset = 60;
        		else if(rc.getParallelEdgeIndexFunction().getIndex(graph, e)==1)
        			offset = -20;
        		else if(rc.getParallelEdgeIndexFunction().getIndex(graph, e)==2)
        			offset= 20;
        		else{
        			incrementoX = (ancho*2)/(numApariciones+1);
        			offset = (int) (rc.getParallelEdgeIndexFunction().getIndex(graph, e)*(incrementoX))+10;
        		}
        	}
        	else if((((yNoCentro+altoNoCentro)>=(yCentro-epsilon))&&((yNoCentro-altoNoCentro)<=(yCentro+epsilon))) ||
        			((yNoCentro>=(yCentro-epsilon))&&(yNoCentro<=(yCentro+epsilon)))){
        	//Están en la misma franja de las ys
        	//Coloco el offset de las etiquetas
        		if(rc.getParallelEdgeIndexFunction().getIndex(graph, e)==0)
        			offset = 30;
        		else if(rc.getParallelEdgeIndexFunction().getIndex(graph, e)==1)
        			offset = 2;
        		else if(rc.getParallelEdgeIndexFunction().getIndex(graph, e)==2)
        			offset= 15;
        		else{
        			incrementoX = (ancho*2)/(numApariciones+1);
        			offset = (int) (rc.getParallelEdgeIndexFunction().getIndex(graph, e)*(incrementoX))+5;
        		}	        		
        	}
        	//Diagonal inferior izquierda
        	else if(((yNoCentro-altoNoCentro)>(yCentro+epsilon)) && ((xNoCentro-anchoNoCentro)<(xCentro-epsilon))){
        		incrementoX = ancho/(numApariciones+1);
        		if(rc.getParallelEdgeIndexFunction().getIndex(graph, e)==0)
        			offset = 30;
        		else if(rc.getParallelEdgeIndexFunction().getIndex(graph, e)==1)
        			offset = 2;
        		else if(rc.getParallelEdgeIndexFunction().getIndex(graph, e)==2)
        			offset= 15;
        		else{
        			incrementoX = (ancho*2)/(numApariciones+1);
        			offset = (int) (rc.getParallelEdgeIndexFunction().getIndex(graph, e)*(incrementoX))+5;
        		}
        	}
        	//Diagonal superior izquierda
        	else if(((yNoCentro+altoNoCentro)<(yCentro-epsilon))&&(xNoCentro-anchoNoCentro)<=(xCentro+epsilon)){
        		incrementoX = ancho/(numApariciones+1);
        		if(rc.getParallelEdgeIndexFunction().getIndex(graph, e)==0)
        			offset = 28;
        		else if(rc.getParallelEdgeIndexFunction().getIndex(graph, e)==1)
        			offset = 1;
        		else if(rc.getParallelEdgeIndexFunction().getIndex(graph, e)==2)
        			offset= 10;
        		else{
        			incrementoX = (ancho*2)/(numApariciones+1);
        			offset = (int) (rc.getParallelEdgeIndexFunction().getIndex(graph, e)*(incrementoX))+5;
        		}
        	}
        	//Diagonal superior derecha
        	else if(((xNoCentro-anchoNoCentro)>(xCentro+epsilon))&& (yNoCentro+altoNoCentro)<(yCentro-epsilon)){
        		incrementoX = ancho/(numApariciones+1);
        		if(rc.getParallelEdgeIndexFunction().getIndex(graph, e)==0)
        			offset = 30;
        		else if(rc.getParallelEdgeIndexFunction().getIndex(graph, e)==1)
        			offset = 2;
        		else if(rc.getParallelEdgeIndexFunction().getIndex(graph, e)==2)
        			offset= 15;
        		else{
        			incrementoX = (ancho*2)/(numApariciones+1);
        			offset = (int) (rc.getParallelEdgeIndexFunction().getIndex(graph, e)*(incrementoX))+5;
        		}
        	}
        	//Diagonal inferior derecha
        	else if(((xNoCentro-anchoNoCentro)>(xCentro+epsilon)&&((yNoCentro-altoNoCentro)>yCentro+epsilon))){
        		incrementoX = ancho/(numApariciones+1);
        		if(rc.getParallelEdgeIndexFunction().getIndex(graph, e)==0)
        			offset = -15;
        		else if(rc.getParallelEdgeIndexFunction().getIndex(graph, e)==1)
        			offset = 13;
        		else if(rc.getParallelEdgeIndexFunction().getIndex(graph, e)==2)
        			offset= 1;
        		else{
        			incrementoX = (ancho*2)/(numApariciones+1);
        			offset = (int) (rc.getParallelEdgeIndexFunction().getIndex(graph, e)*(incrementoX))+5;
        		}
        	}
        }else offset= -18;
        double parallelOffset = 1;
        parallelOffset += rc.getParallelEdgeIndexFunction().getIndex(graph, e);
        if(edgeShape instanceof Ellipse2D) {
            parallelOffset += edgeShape.getBounds().getHeight();
            parallelOffset = -parallelOffset;
        }
        parallelOffset *= d.height;
        parallelOffset =offset;//Es el valor que he calculado para las etiquetas!!!!!!!!!
        
        AffineTransform old = g.getTransform();
        AffineTransform xform2 = new AffineTransform(old);
        xform2.translate(posX+xDisplacement, posY+yDisplacement);
        double dx = x2 - x1;
        double dy = y2 - y1;
        if(rc.getEdgeLabelRenderer().isRotateEdgeLabels()) {
            double theta = Math.atan2(dy, dx);
            if(dx < 0) theta += Math.PI;
            xform2.rotate(theta);
        }
        if(dx < 0) parallelOffset = -parallelOffset;
        xform2.translate(-d.width/2, -(d.height/2-parallelOffset));
        g.setTransform(xform2);
        g.draw(component, rc.getRendererPane(), 0, 0, d.width, d.height, true);

        g.setTransform(old);
    }
}
