package vista.diagrama;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.graph.Graph;
import modelo.transfers.Transfer;

public class GrafoLayout<V, E> extends AbstractLayout<V, E>{

	protected GrafoLayout(Graph<V, E> graph) {
		super(graph);
	}

	public void initialize() {}

	public void reset() {}
	
	public boolean anadeVertice(V arg0){
		Graph<V,E> grafo = getGraph();
		grafo.addVertex(arg0);
		setLocation(arg0, ((Transfer)arg0).getPosicion());
		return true;
	}
	
	public boolean eliminaVertice(V arg0){
		Graph<V,E> grafo = getGraph();
		grafo.removeVertex(arg0);
		return true;
	}

}
