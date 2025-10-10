package vista.diagrama;


import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeIndexFunction;
import edu.uci.ics.jung.graph.util.Pair;
import modelo.transfers.Transfer;

/**
 * A class which creates and maintains indices for parallel edges.
 * Edges are evaluated by a Predicate function and those that
 * evaluate to true are excluded from computing a parallel offset
 * 
 * @author Tom Nelson
 *
 */
@SuppressWarnings("hiding")
public class MiParallelEdgeIndexFunction<Transfer, Object> implements EdgeIndexFunction<Transfer, Object> {
	
    protected Map<Object, Integer> edge_index = new HashMap<Object, Integer>();
    //protected Predicate<Object> predicate;
    
    private MiParallelEdgeIndexFunction() {
    }
    
    public static <Transfer, Object> EdgeIndexFunction<Transfer, Object> getInstance() {
        return new MiParallelEdgeIndexFunction<Transfer, Object>();
    }
    /**
     * Returns the index for the specified edge.
     * Calculates the indices for <code>e</code> and for all edges parallel
     * to <code>e</code>.
     */
    public int getIndex(Graph<Transfer, Object> graph, Object e) {
    	   	
        Integer index = edge_index.get(e);
        if(index == null) {
        	Pair<Transfer> endpoints = graph.getEndpoints(e);
        	Transfer u = endpoints.getFirst();
        	Transfer v = endpoints.getSecond();
        	if(u.equals(v)) index = getIndex(graph, e, v);
        	else index = getIndex(graph, e, u, v);
        }
       int v = index.intValue();
        return v; 
    }

    protected int getIndex(Graph<Transfer, Object> graph, Object e, Transfer v, Transfer u) {
    	Collection<Object> commonEdgeSet = new HashSet<Object>(graph.getIncidentEdges(u));
    	commonEdgeSet.retainAll(graph.getIncidentEdges(v));
    	for(Iterator<Object> iterator=commonEdgeSet.iterator(); iterator.hasNext(); ) {
    		Object edge = iterator.next();
    		Pair<Transfer> ep = graph.getEndpoints(edge);
    		Transfer first = ep.getFirst();
    		Transfer second = ep.getSecond();
    		// remove loops
    		if(first.equals(second) == true) iterator.remove();
    		// remove edges in opposite direction
    		if(first.equals(v) == false) iterator.remove();
    	}
    	int count=0;
    	for(Object other : commonEdgeSet) {
    		if(e.equals(other) == false) {
    			edge_index.put(other, count);
    			count++;
    		}
    	}
    	edge_index.put(e, count);
    	return count;
     }
    
    protected int getIndex(Graph<Transfer, Object> graph, Object e, Transfer v) {
    	Collection<Object> commonEdgeSet = new HashSet<Object>();
    	for(Object another : graph.getIncidentEdges(v)) {
    		Transfer u = graph.getOpposite(v, another);
    		if(u.equals(v)) commonEdgeSet.add(another);
    	}
    	int count=0;
    	for(Object other : commonEdgeSet) 
    		if(e.equals(other) == false) {
    			edge_index.put(other, count);
    			count++;
    		}
    	edge_index.put(e, count);
    	return count;
    }



	/**
     * Resets the indices for this edge and its parallel edges.
     * Should be invoked when an edge parallel to <code>e</code>
     * has been added or removed.
     * @param e
     */
    public void reset(Graph<Transfer, Object> graph, Object e) {
    	Pair<Transfer> endpoints = graph.getEndpoints(e);
        getIndex(graph, e, endpoints.getFirst());
        getIndex(graph, e, endpoints.getFirst(), endpoints.getSecond());
    }
    
    /**
     * Clears all edge indices for all edges in all graphs.
     * Does not recalculate the indices.
     */
    public void reset(){
        edge_index.clear();
    }
}
