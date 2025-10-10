package vista.diagrama;

import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;
import controlador.Controlador;
import controlador.TC;
import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeIndexFunction;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.picking.PickedState;
import modelo.transfers.Transfer;
import modelo.transfers.TransferAtributo;
import modelo.transfers.TransferEntidad;
import modelo.transfers.TransferRelacion;
import persistencia.EntidadYAridad;
import persistencia.NodoEntidad;
import vista.componentes.MyTableModel;
import vista.diagrama.lineas.CreaLineas;
import vista.diagrama.lineas.EtiquetaSobreLinea;
import vista.lenguaje.Lenguaje;
import vista.tema.Theme;

/**
 * This code was edited or generated using CloudGarden's Jigloo SWT/Swing GUI
 * Builder, which is free for non-commercial use. If Jigloo is being used
 * commercially (ie, by a corporation, company or business for any purpose
 * whatever) then you should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details. Use of Jigloo implies
 * acceptance of these licensing terms. A COMMERCIAL LICENSE HAS NOT BEEN
 * PURCHASED FOR THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED LEGALLY FOR
 * ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
@SuppressWarnings({ "rawtypes", "serial", "unchecked" })
public class PanelGrafo extends JPanel implements Printable, KeyListener {

	Graph<Transfer, Object> graph;
	VisualizationViewer<Transfer, Object> vv;
	GrafoLayout<Transfer, Object> layout;
	private Controlador controlador;
	// Mapas para el acceso correcto a los nodos
	protected Map<Integer, TransferEntidad> entidades;
	protected Map<Integer, TransferAtributo> atributos;
	protected Map<Integer, TransferRelacion> relaciones;
	// guarda los elementos que formaran tablas
	protected ArrayList<Transfer> tablas;
	private final MenuDesplegable clickDerecho;
	private TranslatingGraphMousePlugin translating;	
	private PickingGraphMousePlugin picking;
	private boolean mouseMode = false;
	private DefaultModalGraphMouse graphMouse;
	private Theme theme = Theme.getInstancia();

	public PanelGrafo(Vector<TransferEntidad> entidades, Vector<TransferAtributo> atributos, Vector<TransferRelacion> relaciones) {
		this.setLayout(new GridLayout(1, 1));
		// Para que los grafos admitan paralelas el tipo de grafo debe ser este:
		graph = new UndirectedSparseMultigraph<Transfer, Object>();
		// Inicializa el layout, el visualizador y el renderer
		layout = new GrafoLayout<Transfer, Object>(graph);
		// Inserta las entidades, atributos, relaciones al grafo
		this.generaTablasNodos(entidades, atributos, relaciones);
		Collection<TransferEntidad> entities = this.entidades.values();
		for (Iterator<TransferEntidad> it = entities.iterator(); it.hasNext();) {
			TransferEntidad entidad = it.next();
			graph.addVertex(entidad);
			layout.setLocation(entidad, entidad.getPosicion());
		}
		Collection<TransferAtributo> attributes = this.atributos.values();
		for (Iterator<TransferAtributo> it = attributes.iterator(); it.hasNext();) {
			TransferAtributo atributo = it.next();
			graph.addVertex(atributo);
			layout.setLocation(atributo, atributo.getPosicion());
		}
		Collection<TransferRelacion> relations = this.relaciones.values();
		for (Iterator<TransferRelacion> it = relations.iterator(); it.hasNext();) {
			TransferRelacion relation = it.next();
			graph.addVertex(relation);
			layout.setLocation(relation, relation.getPosicion());
		}

		// Añade las aristas al grafo
		// Aristas entre relaciones y entidades
		for (Iterator<TransferRelacion> it = relations.iterator(); it.hasNext();) {
			TransferRelacion relation = it.next();
			if (!relation.getListaAtributos().isEmpty())
				// Añado sus atributos
				for (Iterator<String> it2 = relation.getListaAtributos().iterator(); it2.hasNext();) {
					Integer id = Integer.parseInt(it2.next());
					graph.addEdge(new Double(Math.random()), relation, this.atributos.get(id));
				}
			if (!relation.getListaEntidadesYAridades().isEmpty())
				for (Iterator<EntidadYAridad> it3 = relation.getListaEntidadesYAridades().iterator(); it3.hasNext();) {
					EntidadYAridad data = it3.next();
					graph.addEdge(data, relation, this.entidades.get(data.getEntidad()));
				}
		}
		// Aristas entre entidades y atributos
		for (Iterator<TransferEntidad> it = entities.iterator(); it.hasNext();) {
			TransferEntidad entity = it.next();
			if (!entity.getListaAtributos().isEmpty())
				for (Iterator<String> it2 = entity.getListaAtributos().iterator(); it2.hasNext();) {
					Integer id = Integer.parseInt(it2.next());
					graph.addEdge(new Double(Math.random()), entity, this.atributos.get(id));
				}
			// Señala la/s claves primarias como tales
			if (!entity.getListaClavesPrimarias().isEmpty())
				for (Iterator<String> it2 = entity.getListaClavesPrimarias().iterator(); it2.hasNext();) {
					Integer id = Integer.parseInt(it2.next());
					this.atributos.get(id).setClavePrimaria(true);
				}
		}
		// Aristas entre atributos compuestos y sus valores
		for (Iterator<TransferAtributo> it = attributes.iterator(); it.hasNext();) {
			TransferAtributo atrib = it.next();
			if (atrib.getCompuesto())
				for (Iterator<String> it2 = atrib.getListaComponentes().iterator(); it2.hasNext();) {
					Integer id = Integer.parseInt(it2.next());
					graph.addEdge(new Double(Math.random()), atrib, this.atributos.get(id));
				}
		}

		vv = new VisualizationViewer<Transfer, Object>(layout);
		clickDerecho = new MenuDesplegable(vv, this.entidades);
		// this class will provide both label drawing and vertex shapes
		LabelRenderer<Transfer, Object> vlasr = new LabelRenderer<Transfer, Object>();
		vv.setDoubleBuffered(true); // Incrementa rendimiento
		// Color del panel grande sobre el que se pinta
		vv.setBackground(theme.background());
		// Renderiza las lineas que unen los elementos
		vv.getRenderer().setEdgeLabelRenderer(new EtiquetaSobreLinea<Transfer, Object>());

		RenderContext<Transfer, Object> rc = vv.getRenderContext();
		rc.getEdgeLabelRenderer();
		// Distancia de las etiquetas a las lineas
		rc.setLabelOffset(9);
		rc.getParallelEdgeIndexFunction();
		EdgeIndexFunction<Transfer, Object> mp = MiParallelEdgeIndexFunction.getInstance();
		rc.setParallelEdgeIndexFunction(mp);

		// Escribe el texto del elemento
		vv.getRenderContext().setVertexLabelTransformer(new Transformer<Transfer, String>() {
			public String transform(Transfer input) {
				if (input instanceof TransferEntidad)
					return "<html><center><font color=" + theme.labelFontColorDark().hexValue() + ">" + input
							+ "<p></font>";
				if (input instanceof TransferAtributo) {
					TransferAtributo atributo = (TransferAtributo) input;
					String texto = new String();
					texto += "<html><center><center><font color=" + theme.labelFontColorDark().hexValue() + ">";
					if (atributo.isClavePrimaria())
						texto += "<U>";
					texto += input;
					if (atributo.isClavePrimaria())
						texto += "</U>";
					texto += "<p></font>";
					return texto;
				}
				if (input instanceof TransferRelacion)
					return "<html><center><font color=" + theme.labelFontColorLight().hexValue() + ">" + input
							+ "<p></font>";
				return "<html><center>" + input + "<p>";
			}
		});

		vv.getRenderContext().setVertexShapeTransformer(vlasr);
		// Color de la letra al pinchar
		//vv.getRenderContext().setVertexLabelRenderer(new DefaultVertexLabelRenderer(Color.white));
		// Ancho de las aristas
		vv.getRenderContext().setEdgeStrokeTransformer(new ConstantTransformer<Stroke>(new BasicStroke(2f)));
		// Hace las lineas rectas
		vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
		// Escribe datos sobre las lineas
		vv.getRenderContext().setEdgeLabelTransformer(new Transformer<Object, String>() {
			public String transform(Object input) {
				if (input instanceof EntidadYAridad) {
					EntidadYAridad dato = (EntidadYAridad) input;
					String iniRango, finRango, strRol, numerito = "";

					if (dato.getFinalRango() == Integer.MAX_VALUE) finRango = "N";
					else finRango = String.valueOf(dato.getFinalRango());

					if (dato.getPrincipioRango() == 0) {
						if (dato.getFinalRango() == 0)
							return null;// es IsA
						else {// Es rango min max
							if (dato.getPrincipioRango() == Integer.MAX_VALUE) iniRango = "N";
							else iniRango = String.valueOf(dato.getPrincipioRango());
							numerito = iniRango + "  . .  " + finRango;
						}
					} else {// Es rango min max
						if (dato.getPrincipioRango() == Integer.MAX_VALUE) iniRango = "N";
						else iniRango = String.valueOf(dato.getPrincipioRango());
						numerito = iniRango + "  . .  " + finRango;
					}
					strRol = dato.getRol();
					// Color de las cardinalidades
					return "<html><center><font size=\"5\" color=\"" + theme.lines().hexValue() + "\">"
							+ numerito + "   " + strRol + "<p>";
				} else return null; // Si no es una relación no escribe la aridad
			}
		});

		// Color elementos
		vv.getRenderer().setVertexRenderer(new VertexRenderer<Transfer, Object>(theme.entity(), theme.entity(), true));
		// Etiquetas de los vertices
		vv.getRenderer().setVertexLabelRenderer(vlasr);
		// Ancho del borde de los elementos
		vv.getRenderContext().setVertexStrokeTransformer(new Transformer<Transfer, Stroke>() {
			@Override
			public Stroke transform(Transfer arg0) {
				return new BasicStroke(1f);
			}
		});
		vv.getRenderer().setEdgeRenderer(new CreaLineas<Transfer, Object>());
		// add a listener for ToolTips
		vv.setVertexToolTipTransformer(new ToStringLabeller<Transfer>() {
			@Override
			public String transform(Transfer t) {
				return "<html><p><font color=\""+theme.labelFontColorDark().hexValue()+"\"" +
			            "size=\"5\">"+t+"</font></p></html>";
			}
		});

		graphMouse = new DefaultModalGraphMouse() {
			// Esta historia invierte el zoom de la rueda del raton
			@Override
			public void mouseWheelMoved(final MouseWheelEvent e) {
				int rot = e.getWheelRotation();
				rot *= -1;
				super.mouseWheelMoved(new MouseWheelEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiersEx(),
						e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger(), e.getScrollType(),
						e.getScrollAmount(), rot));
			}
		};
		translating = new TranslatingGraphMousePlugin() {
			@Override
			public void mouseExited(MouseEvent e) {
		        JComponent c = (JComponent)e.getSource();
		        c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		    }
			@Override
			public void mouseReleased(MouseEvent e) {
		        VisualizationViewer vv = (VisualizationViewer)e.getSource();
		        vv.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		    }
		};

		// El evento se dispara al terminar de mover un nodo
		picking = new PickingGraphMousePlugin<Transfer, Double>() {
			@Override
			public void mouseReleased(MouseEvent e) {
				SwingUtilities.invokeLater(doFocus);
				VisualizationViewer<Transfer, Double> vv2 = (VisualizationViewer<Transfer, Double>) e.getSource();
				// Aqui empieza lo de cambiar la fuente
				final PickedState<Transfer> ps = vv2.getPickedVertexState();

				// Marca los elementos seleccionados con un borde más ancho
				vv2.getRenderContext().setVertexStrokeTransformer(new Transformer<Transfer, Stroke>() {
					@Override
					public Stroke transform(Transfer arg0) {
						// Ancho del borde al seleccionar
						for (Transfer t : ps.getPicked())
							if (arg0.equals(t)) return new BasicStroke(3f);
						return new BasicStroke(1f);// Ancho del borde por defecto
					}
				});

				if (e.getModifiers() == modifiers) {
					if (down != null) {
						Point2D out = obtenerPuntoExacto(e);
						if (vertex == null && heyThatsTooClose(down, out, 5) == false)
							pickContainedVertices(vv2, down, out, true);

						if (vertex != null) {
							for (Transfer t : ps.getPicked()) {
			//					Point2D actual = layout.getLocation(t);
								Point2D actual = null;
								layout.setLocation(t, actual);
								// Ha pulsado en un nodo -> Obtenemos la información primero
								if (ps.getSelectedObjects().length == 1) {
									EnviaInformacionNodo(t);
								}
								// Si se ha movido lo guardamos
								if (!actual.equals(t.getPosicion())) {
									if (t instanceof TransferEntidad) {
										TransferEntidad entidad = (TransferEntidad) t;
										TransferEntidad clon = entidad.clonar();
										clon.setPosicion(actual);
										controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_MoverEntidad, clon);
									}
									if (t instanceof TransferAtributo) {
										TransferAtributo atributo = (TransferAtributo) t;
										TransferAtributo clon = atributo.clonar();
										clon.setPosicion(actual);
										controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_MoverAtributo, clon);
									}
									if (t instanceof TransferRelacion) {
										TransferRelacion relacion = (TransferRelacion) t;
										TransferRelacion clon = relacion.clonar();
										clon.setPosicion(actual);
										controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_MoverRelacion, clon);
									}
								}
							}
						} else { // Si se ha pulsado en el vacío
							EnviaInformacionNodo(null);
						}
					}
				} else if (e.getModifiers() == this.addToSelectionModifiers)
					if (down != null) {
						Point2D out = obtenerPuntoExacto(e);
						if (vertex == null && heyThatsTooClose(down, out, 5) == false)
							pickContainedVertices(vv2, down, out, false);
					}

				down = null;
				vertex = null;
				edge = null;
				rect.setFrame(100, 100, 110, 100);
				vv2.removePostRenderPaintable(lensPaintable);
				vv2.repaint();
			}

			private boolean heyThatsTooClose(Point2D p, Point2D q, double min) {
				return Math.abs(p.getX() - q.getX()) < min && Math.abs(p.getY() - q.getY()) < min;
			}
		};
		creaGraphMouse();
		graphMouse.add(picking);
		vv.setGraphMouse(graphMouse);
		vv.addKeyListener(this);
		vv.setDoubleBuffered(true);
		this.add(vv);
		graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
	}

	public Controlador getControlador() {
		return controlador;
	}
	
	public void setControlador(Controlador controlador) {
		this.controlador = controlador;
		this.clickDerecho.setControlador(controlador);
	}
	private void creaGraphMouse() {
		graphMouse = new DefaultModalGraphMouse() {
			// Esta historia invierte el zoom de la rueda del raton
			@Override
			public void mouseWheelMoved(final MouseWheelEvent e) {
				int rot = e.getWheelRotation();
				rot *= -1;
				super.mouseWheelMoved(new MouseWheelEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiersEx(),
						e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger(), e.getScrollType(),
						e.getScrollAmount(), rot));
			}
		};
		// Plugin para el acceso al menú desplegable
		graphMouse.add(new AbstractPopupGraphMousePlugin() {
			@Override
			protected void handlePopup(final MouseEvent e) {
				final VisualizationViewer<Transfer, Double> vv = (VisualizationViewer<Transfer, Double>) e.getSource();
				Point2D p = e.getPoint();

				GraphElementAccessor<Transfer, Double> pickSupport = vv.getPickSupport();
				if (pickSupport != null) {
					final Transfer v = pickSupport.getVertex(vv.getGraphLayout(), p.getX(), p.getY());
					if (v != null) { // Se ha pinchado en un nodo
						clickDerecho.Reinicializa(v, p);
						clickDerecho.show(vv, e.getX(), e.getY());
					} else { // Se ha pinchado en el área
						p = obtenerPuntoExacto(e);
						clickDerecho.Reinicializa(null, p);
						clickDerecho.show(vv, e.getX(), e.getY());
					}
				}
			}
		});
	}
	
	public void toggleDragMode(boolean b) {
		if(b==mouseMode) return;
		if(!mouseMode) {
			creaGraphMouse();
			graphMouse.add(translating);
			graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);
			vv.setGraphMouse(graphMouse);
		}else {
			creaGraphMouse();
			graphMouse.add(picking);
			graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
			vv.setGraphMouse(graphMouse);
		}
		mouseMode = !mouseMode;
	}
	
	private void generaTablasNodos(Vector<TransferEntidad> entidades, Vector<TransferAtributo> atributos,
			Vector<TransferRelacion> relaciones) {
		this.entidades = new HashMap<Integer, TransferEntidad>();
		this.atributos = new HashMap<Integer, TransferAtributo>();
		this.relaciones = new HashMap<Integer, TransferRelacion>();
		// Inserta las entidades con su id como clave
		for (Iterator<TransferEntidad> it = entidades.iterator(); it.hasNext();) {
			TransferEntidad entidad = it.next();
			this.entidades.put(entidad.getIdEntidad(), entidad);
		}
		// Inserta los atributos con su id como clave
		for (Iterator<TransferAtributo> it = atributos.iterator(); it.hasNext();) {
			TransferAtributo atributo = it.next();
			this.atributos.put(atributo.getIdAtributo(), atributo);
		}
		// Inserta las relaciones con su id como clave
		for (Iterator<TransferRelacion> it = relaciones.iterator(); it.hasNext();) {
			TransferRelacion relacion = it.next();
			this.relaciones.put(relacion.getIdRelacion(), relacion);
		}
		creaArrayTablas();
	}

	/*
	 * Oyentes teclado
	 */
	private Runnable doFocus = new Runnable() {
		public void run() {
			vv.grabFocus();
		}
	};

	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case 127:
			this.clickDerecho.suprimir();
			break;
		case 83:// CTRL S
			if (e.isControlDown())
				this.controlador.mensajeDesde_GUIPrincipal(TC.GUI_Principal_Click_Submenu_Guardar, null);
			break;
		case 32:// Space
			toggleDragMode(true);
		break;
		}
	}


	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case 32:// Space
			toggleDragMode(false);
		break;
		}
	}

	public void keyTyped(KeyEvent arg0) {}

	/**
	 * Este método enviará la información referente al nodo t contenida en un JTree
	 * para su exposición en el panel de información.
	 * 
	 * @param t
	 *            Transfer que se ha pulsado
	 */
	public void EnviaInformacionNodo(Transfer t) {
		if (t == null) {
			// Envia mensaje al controlador para que vacíe el panel
			controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_LimpiarPanelInformacion, null);
			return;
		}
		controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_MostrarDatosEnPanelDeInformacion, generaArbolInformacion());
		controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_MostrarDatosEnTablaDeVolumenes, generaTablaVolumenes());
	}

	private void creaArrayTablas() {
		tablas = new ArrayList<Transfer>();
		tablas.addAll(entidades.values());
		for (HashMap.Entry<Integer, TransferRelacion> rel : this.relaciones.entrySet())
			if (!rel.getValue().isIsA()) tablas.add(rel.getValue());
		tablas.addAll(listaMultivalorados().values());
	}

	public void refreshTables(TableModelEvent datos) {
		MyTableModel tabla = (MyTableModel) datos.getSource();
		int row = 0;
		for (Transfer t : this.tablas) {
			for (int col = 0; col < tabla.getColumnCount(); col++) {
				if (col == 1)
					t.setVolumen(Integer.parseInt(tabla.getValueAt(row, col)));
				if (col == 2)
					t.setFrecuencia(Integer.parseInt(tabla.getValueAt(row, col)));
			}
			row++;
		}
	}

	private HashMap<Integer, TransferAtributo> listaMultivalorados() {
		HashMap<Integer, TransferAtributo> multis = new HashMap<Integer, TransferAtributo>();
		int p = 1;
		for (HashMap.Entry<Integer, TransferAtributo> a : atributos.entrySet())
			if (a.getValue().isMultivalorado()) multis.put(p++, a.getValue());
		return multis;
	}

	public String[][] generaTablaVolumenes() {
		int filas = tablas.size(), columnas = 3, row = 0;
		String valor;
		String[][] tabla = new String[filas][3];
		for (Transfer t : this.tablas) {
			for (int col = 0; col < columnas; col++) {
				if (col == 0) valor = t.getNombre();
				else if (col == 1) valor = String.valueOf(t.getVolumen());
				else valor = String.valueOf(t.getFrecuencia());
				tabla[row][col] = valor;
			}
			row++;
		}
		return tabla;
	}
	
	private DefaultMutableTreeNode nodoAtributo(TransferAtributo a, DefaultMutableTreeNode nodoAtr) {
		if(a.getCompuesto()) 
			for(int k=0;k<a.getListaComponentes().size();k++) {
				TransferAtributo subAtr = atributos.get(Integer.parseInt((String)a.getListaComponentes().get(k)));
				DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(subAtr);
				nodoAtributo(subAtr, subNode);
				nodoAtr.add(subNode);
				subAtr.setSubatributo(true);
			}
		return nodoAtr;
	}
	
	public JTree generaArbolInformacion() {
		JTree arbolInformacion;
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		DefaultMutableTreeNode arbolEntidades = new DefaultMutableTreeNode(Lenguaje.text(Lenguaje.ENTITIES));
		DefaultMutableTreeNode arbolRelaciones = new DefaultMutableTreeNode(Lenguaje.text(Lenguaje.RELATIONS));
		for (HashMap.Entry<Integer, TransferEntidad> ent : this.entidades.entrySet()) {
			DefaultMutableTreeNode nodoEntidad = new DefaultMutableTreeNode(ent.getValue());
			Vector lista = (ent.getValue().getListaAtributos());
			for (int j = 0; j < lista.size(); j++) {
				TransferAtributo a = this.atributos.get(Integer.parseInt((String) lista.get(j)));
				a.setSubatributo(false);
				nodoEntidad.add(nodoAtributo(a,new DefaultMutableTreeNode(a)));
			}
			arbolEntidades.add(nodoEntidad);
		}
		for (HashMap.Entry<Integer, TransferRelacion> rel : this.relaciones.entrySet()) {
			DefaultMutableTreeNode nodoRelacion = new DefaultMutableTreeNode(rel.getValue());
			String tipo = "";
			Vector listaEnt = (rel.getValue().getListaEntidadesYAridades());
			if (!listaEnt.isEmpty())
				for (int j = 0; j < listaEnt.size(); j++) {
					tipo = rel.getValue().getTipo().equals("IsA") ? j == 0 ? "padre" : "hija" : "normal";
					NodoEntidad ne = new NodoEntidad(
							this.entidades.get(((EntidadYAridad) listaEnt.get(j)).getEntidad()).getNombre(),
							((EntidadYAridad) listaEnt.get(j)), tipo);
					nodoRelacion.add(new DefaultMutableTreeNode(ne));
				}
			Vector listaAtr = (rel.getValue().getListaAtributos());
			if (!listaAtr.isEmpty())
				for (int j = 0; j < listaAtr.size(); j++) {
					TransferAtributo a = this.atributos.get(Integer.parseInt((String) listaAtr.get(j)));
					nodoRelacion.add(nodoAtributo(a,new DefaultMutableTreeNode(a)));
				}
			arbolRelaciones.add(nodoRelacion);
		}
		root.add(arbolEntidades);
		root.add(arbolRelaciones);
		arbolInformacion = new JTree(root);
		arbolInformacion.setRootVisible(false);
		for (int i = 0; i < arbolInformacion.getRowCount(); i++)
			arbolInformacion.expandRow(i);
		arbolInformacion.setToggleClickCount(2);
		return arbolInformacion;
	}

	/**
	 * Este método modificará el transfer que se envíe en el grafo. Es necesario
	 * para actualizar contenidos por parte del Controlador.
	 * 
	 * @param object
	 *            Dato que actualizará en el grafo
	 */
	public Transfer ModificaValorInterno(Transfer object) {
		// Si es entidad se actualiza
		if (object instanceof TransferEntidad) {
			TransferEntidad entidad = (TransferEntidad) object;
			TransferEntidad antigua = entidades.get(entidad.getIdEntidad());
			antigua.CopiarEntidad(entidad);
			if (!antigua.getListaAtributos().isEmpty()) {
				// Añado sus atributos
				for (Iterator<String> it2 = antigua.getListaAtributos().iterator(); it2.hasNext();) {
					Integer id = Integer.parseInt(it2.next());
				//	if (!graph.areNeighbors(antigua, this.atributos.get(id))) { // Añade aristas que no existiesen
						graph.addEdge(new Double(Math.random()), antigua, this.atributos.get(id));
				//	}
				}
			}
			vv.repaint(); // Se redibuja todo el grafo actualizado
			return antigua;
		}
		// Si es atributo se actualiza
		if (object instanceof TransferAtributo) {
			TransferAtributo atributo = (TransferAtributo) object;
			TransferAtributo antigua = atributos.get(atributo.getIdAtributo());
			antigua.CopiarAtributo(atributo);
			if (!antigua.getListaComponentes().isEmpty()) {
				// Añado sus atributos
				for (Iterator<String> it2 = antigua.getListaComponentes().iterator(); it2.hasNext();) {
					Integer id = Integer.parseInt(it2.next());
			//		if (!graph.areNeighbors(antigua, this.atributos.get(id))) { // Añade aristas que no existiesen
						graph.addEdge(new Double(Math.random()), antigua, this.atributos.get(id));
			//		}
				}
			}
			vv.repaint(); // Se redibuja todo el grafo actualizado
			return antigua;
		}
		// Si es relacion se actualiza
		if (object instanceof TransferRelacion) {
			TransferRelacion relacion = (TransferRelacion) object;
			TransferRelacion antigua = relaciones.get(relacion.getIdRelacion());
			Boolean rolRepe = false;
			antigua.CopiarRelacion(relacion, relacion.getIdRelacion(), rolRepe);
			graph.removeVertex(antigua);
			graph.addVertex(antigua);
			layout.setLocation(antigua, antigua.getPosicion());
			if (!antigua.getListaAtributos().isEmpty())
				// Añado sus atributos
				for (Iterator<String> it2 = antigua.getListaAtributos().iterator(); it2.hasNext();) {
					Integer id = Integer.parseInt(it2.next());
					graph.addEdge(new Double(Math.random()), antigua, this.atributos.get(id));
				}
			// Añado las aristas del grafo con las aridades.
			if (!antigua.getListaEntidadesYAridades().isEmpty()) {
				for (Iterator<EntidadYAridad> it3 = antigua.getListaEntidadesYAridades().iterator(); it3.hasNext();) {
					EntidadYAridad data = it3.next();
					graph.addEdge(data, antigua, this.entidades.get(data.getEntidad()));
				}
			}
			vv.repaint(); // Se redibuja todo el grafo actualizado
			return antigua;
		}
		return null;
	}

	/**
	 * Este método es parecido a ModificaVAlorInterno. Es necesario para actualizar
	 * contenidos por parte del Controlador.
	 * 
	 * @param object
	 *            Dato que actualizará en el grafo
	 */
	public Transfer ModificaValorInterno1a1(Vector v) {
		// Si es relacion se actualiza
		TransferRelacion antigua = relaciones.get(v.get(0));
		antigua.CopiarRelacionUnoUno(v);
		graph.removeVertex(antigua);
		graph.addVertex(antigua);
		layout.setLocation(antigua, antigua.getPosicion());
		if (!antigua.getListaAtributos().isEmpty()) {
			// Añado sus atributos
			for (Iterator<String> it2 = antigua.getListaAtributos().iterator(); it2.hasNext();) {
				Integer id = Integer.parseInt(it2.next());
				graph.addEdge(new Double(Math.random()), antigua, this.atributos.get(id));
			}
		}
		// Añado las aristas del grafo con las aridades.
		if (!antigua.getListaEntidadesYAridades().isEmpty())
			for (Iterator<EntidadYAridad> it3 = antigua.getListaEntidadesYAridades().iterator(); it3.hasNext();) {
				EntidadYAridad data = it3.next();
				graph.addEdge(data, antigua, this.entidades.get(data.getEntidad()));
			}
		vv.repaint(); // Se redibuja todo el grafo actualizado
		return antigua;
	}

	/**
	 * Añade el nodo al grafo
	 * 
	 * @param arg0
	 *            Objeto entidad que se añade
	 */
	public void anadirNodo(Transfer arg0) {
		if (arg0 instanceof TransferEntidad) {
			TransferEntidad entidad = (TransferEntidad) arg0;
			entidades.put(entidad.getIdEntidad(), entidad);
			layout.anadeVertice(entidad);
		}
		if (arg0 instanceof TransferAtributo) {
			TransferAtributo atributo = (TransferAtributo) arg0;
			atributos.put(atributo.getIdAtributo(), atributo);
			layout.anadeVertice(atributo);
		}
		if (arg0 instanceof TransferRelacion) {
			TransferRelacion relacion = (TransferRelacion) arg0;
			relaciones.put(relacion.getIdRelacion(), relacion);
			layout.anadeVertice(relacion);
		}
		creaArrayTablas();
		vv.repaint();
	}

	/**
	 * Elimina la entidad que se referencie
	 * 
	 * @param arg0
	 *            Objeto que se elimina del grafo
	 */
	public void eliminaNodo(Transfer arg0) {
		if (arg0 instanceof TransferEntidad) {
			TransferEntidad entidad = (TransferEntidad) arg0;
			entidad = entidades.get(entidad.getIdEntidad());
			graph.removeVertex(entidad);
			entidades.remove(entidad.getIdEntidad());
			tablas.remove(entidad);
		}
		if (arg0 instanceof TransferAtributo) {
			TransferAtributo atributo = (TransferAtributo) arg0;
			atributo = atributos.get(atributo.getIdAtributo());
			graph.removeVertex(atributo);
			atributos.remove(atributo.getIdAtributo());
			if (atributo.isMultivalorado())
				tablas.remove(atributo);
		}
		if (arg0 instanceof TransferRelacion) {
			TransferRelacion relacion = (TransferRelacion) arg0;
			relacion = relaciones.get(relacion.getIdRelacion());
			graph.removeVertex(relacion);
			relaciones.remove(relacion.getIdRelacion());
			tablas.remove(relacion);
		}
		vv.repaint();
	}

	/**
	 * Añade la arista entre los nodos especificados
	 *
	 */
	public void anadirArista(Transfer arg0, Transfer arg1) {
		graph.addEdge(new String(), arg0, arg1);
		vv.repaint();
	}

	/**
	 * Guarda el grafo en un fichero gráfico JPEG
	 * 
	 * @param file
	 *            Fichero a guardar la imagen
	 */
	public void writeJPEGGraph(File file) {
		int width = vv.getWidth();
		int height = vv.getHeight();

		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = bi.createGraphics();
		vv.paint(graphics);
		graphics.dispose();

		try {
			ImageIO.write(bi, "jpeg", file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Imprime el grafo presente en el panel. Muestra el dialog para seleccionar
	 * impresora.
	 */
	public void printGraph() {
		PrinterJob printJob = PrinterJob.getPrinterJob();
		printJob.setPrintable(this);
		try {
			if (printJob.printDialog()) {
				try {
					printJob.print();
				} catch (PrinterException e) {
					System.out.println("Error en la impresión");
				}
			}
		} catch (NullPointerException e) {
			System.out.println("Opciones de impresión inválidas");
		}

	}

	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
		if (pageIndex > 0)
			return (Printable.NO_SUCH_PAGE);
		else {
			java.awt.Graphics2D g2d = (java.awt.Graphics2D) graphics;
			g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
			vv.setDoubleBuffered(false);
			vv.paint(g2d);
			vv.setDoubleBuffered(true);
			return (Printable.PAGE_EXISTS);
		}
	}


	/**
	 * Método auxiliar para determinar la posición relativa en que el usuario hizo
	 * click dentro del panel.
	 * 
	 * @param ev
	 *            Evento de ratón qdel que se quiere conocer la posición
	 * 
	 * @return El lugar del click, relativo a los scrolls y al zoom.
	 */
	private Point2D obtenerPuntoExacto(MouseEvent ev) {
		return vv.getRenderContext().getMultiLayerTransformer().inverseTransform(ev.getPoint());
	}

}
