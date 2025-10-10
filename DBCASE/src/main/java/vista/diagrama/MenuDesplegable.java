package vista.diagrama;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import controlador.Controlador;
import controlador.TC;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.picking.PickedState;
import modelo.transfers.Transfer;
import modelo.transfers.TransferAtributo;
import modelo.transfers.TransferEntidad;
import modelo.transfers.TransferRelacion;
import vista.lenguaje.Lenguaje;
import vista.tema.Theme;

@SuppressWarnings("serial")
public class MenuDesplegable extends JPopupMenu {
	public Transfer nodo; // Nodo sobre el que se ha pulsado
	public Point2D punto; // Punto del plano donde se ha pulsado
	private Controlador controlador;
	private VisualizationViewer<Transfer, Object> vv;
	private Map<Integer, TransferEntidad> entidades;
	private Theme theme = Theme.getInstancia();
	/**
	 * Constructor público
	 * @param vv 
	 * @param entidades 
	 */
	public MenuDesplegable(VisualizationViewer<Transfer, Object> vv, Map<Integer, TransferEntidad> entidades) {
		this.vv = vv;
		this.entidades = entidades;
		this.removeAll();
	}

	public void Reinicializa(Transfer nodo, Point2D punto) {
		this.punto = punto;
		this.removeAll();
		if (nodo == null) { // Si se pide null es que ha pinchado en el área libre
			// Insertar una entidad
			JMenuItem j1 = new JMenuItem(Lenguaje.text(Lenguaje.ADD_ENTITY));
			j1.setFont(theme.font());
			j1.setForeground(theme.fontColor());
			j1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					MenuDesplegable menu = (MenuDesplegable) ((JMenuItem) e.getSource()).getParent();
					Point2D p = menu.punto;
					controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_InsertarEntidad, p);
				}
			});
			this.add(j1);
			this.add(new JSeparator());

			// Insertar una relacion normal
			JMenuItem j2 = new JMenuItem(Lenguaje.text(Lenguaje.ADD_RELATION));
			j2.setFont(theme.font());
			j2.setForeground(theme.fontColor());
			j2.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					MenuDesplegable menu = (MenuDesplegable) ((JMenuItem) e.getSource()).getParent();
					Point2D p = menu.punto;
					controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_InsertarRelacionNormal, p);
				}
			});
			this.add(j2);
			this.add(new JSeparator());

			// Insertar una relacion IsA

			JMenuItem j3 = new JMenuItem(Lenguaje.text(Lenguaje.ADD_ISARELATION));
			j3.setFont(theme.font());
			j3.setForeground(theme.fontColor());
			j3.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					MenuDesplegable menu = (MenuDesplegable) ((JMenuItem) e.getSource()).getParent();
					Point2D p = menu.punto;
					controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_InsertarRelacionIsA, p);
				}
			});
			this.add(j3);
			this.add(new JSeparator());

			// Insertar un dominio

			JMenuItem j4 = new JMenuItem(Lenguaje.text(Lenguaje.ADD_DOMAIN));
			j4.setFont(theme.font());
			j4.setForeground(theme.fontColor());
			j4.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					MenuDesplegable menu = (MenuDesplegable) ((JMenuItem) e.getSource()).getParent();
					Point2D p = menu.punto;
					controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_CrearDominio, p);
				}
			});
			this.add(j4);
			return;
		}
		// Se ha pinchado sobre un nodo
		this.nodo = nodo;
		if (nodo instanceof TransferEntidad) { // Si es entidad
			// Anadir un atributo a una entidad
			JMenuItem j3 = new JMenuItem(Lenguaje.text(Lenguaje.ADD_ATTRIBUTE));
			j3.setFont(theme.font());
			j3.setForeground(theme.fontColor());
			j3.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					MenuDesplegable menu = (MenuDesplegable) ((JMenuItem) e.getSource()).getParent();
					TransferEntidad entidad = (TransferEntidad) menu.nodo;
					TransferEntidad clon_entidad = entidad.clonar();
					controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_AnadirAtributoEntidad, clon_entidad);
				}
			});
			this.add(j3);
			this.add(new JSeparator());

			// Renombrar la entidad
			JMenuItem j1 = new JMenuItem(Lenguaje.text(Lenguaje.RENAME_ENTITY));
			j1.setFont(theme.font());
			j1.setForeground(theme.fontColor());
			j1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					MenuDesplegable menu = (MenuDesplegable) ((JMenuItem) e.getSource()).getParent();
					TransferEntidad entidad = (TransferEntidad) menu.nodo;
					TransferEntidad clon_entidad = entidad.clonar();
					controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_RenombrarEntidad, clon_entidad);
				}
			});
			this.add(j1);

			// Eliminar una entidad
			// Si sólo está seleccionada la entidad..
			PickedState<Transfer> p = vv.getPickedVertexState();
			if (p.getPicked().size() < 2) {
				JMenuItem j4 = new JMenuItem(Lenguaje.text(Lenguaje.DELETE_ENT));
				j4.setFont(theme.font());
				j4.setForeground(theme.fontColor());
				j4.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						MenuDesplegable menu = (MenuDesplegable) ((JMenuItem) e.getSource()).getParent();
						TransferEntidad entidad = (TransferEntidad) menu.nodo;
						TransferEntidad clon_entidad = entidad.clonar();
						Vector<Object> v = new Vector<Object>();
						v.add(clon_entidad);
						v.add(true);
						controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EliminarEntidad, v);
					}
				});
				this.add(j4);
			} else {
				JMenuItem j4 = new JMenuItem(Lenguaje.text(Lenguaje.DELETE));
				j4.setFont(theme.font());
				j4.setForeground(theme.fontColor());
				j4.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						suprimir();
					}
				});
				this.add(j4);
			}
			this.add(new JSeparator());

			// Añadir restricciones
			JMenuItem j5 = new JMenuItem(Lenguaje.text(Lenguaje.RESTRICTIONS));
			j5.setFont(theme.font());
			j5.setForeground(theme.fontColor());
			j5.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					MenuDesplegable menu = (MenuDesplegable) ((JMenuItem) e.getSource()).getParent();
					TransferEntidad entidad = (TransferEntidad) menu.nodo;
					TransferEntidad clon_entidad = entidad.clonar();
					controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_AnadirRestriccionAEntidad,
							clon_entidad);
				}
			});
			this.add(j5);

			// Añadir tablaUnique
			JMenuItem j6 = new JMenuItem(Lenguaje.text(Lenguaje.TABLE_UNIQUE));
			j6.setFont(theme.font());
			j6.setForeground(theme.fontColor());
			j6.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					MenuDesplegable menu = (MenuDesplegable) ((JMenuItem) e.getSource()).getParent();
					TransferEntidad entidad = (TransferEntidad) menu.nodo;
					TransferEntidad clon_entidad = entidad.clonar();
					controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_TablaUniqueAEntidad, clon_entidad);
				}
			});
			this.add(j6);
		}

		if (nodo instanceof TransferAtributo) { // Si es atributo
			// Editar el dominio del atributo
			JMenuItem j2 = new JMenuItem(Lenguaje.text(Lenguaje.EDIT_DOMAIN));
			j2.setFont(theme.font());
			j2.setForeground(theme.fontColor());
			j2.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					MenuDesplegable menu = (MenuDesplegable) ((JMenuItem) e.getSource()).getParent();
					TransferAtributo atributo = (TransferAtributo) menu.nodo;
					TransferAtributo clon_atributo = atributo.clonar();
					controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EditarDominioAtributo, clon_atributo);
				}
			});
			this.add(j2);

			// Renombrar un atributo
			JMenuItem j1 = new JMenuItem(Lenguaje.text(Lenguaje.RENAME_ATTRIB));
			j1.setFont(theme.font());
			j1.setForeground(theme.fontColor());
			j1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					MenuDesplegable menu = (MenuDesplegable) ((JMenuItem) e.getSource()).getParent();
					TransferAtributo atributo = (TransferAtributo) menu.nodo;
					TransferAtributo clon_atributo = atributo.clonar();
					controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_RenombrarAtributo, clon_atributo);
				}
			});
			this.add(j1);

			// Eliminar un atributo
			// Si sólo está seleccionado el atributo..
			PickedState<Transfer> p = vv.getPickedVertexState();
			int seleccionados = 0;
			for (@SuppressWarnings("unused")
			Transfer t : p.getPicked()) {
				seleccionados++;
			}
			if (seleccionados < 2) {
				JMenuItem j7 = new JMenuItem(Lenguaje.text(Lenguaje.DELETE_ATTRIB));
				j7.setFont(theme.font());
				j7.setForeground(theme.fontColor());
				j7.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						MenuDesplegable menu = (MenuDesplegable) ((JMenuItem) e.getSource()).getParent();
						TransferAtributo atributo = (TransferAtributo) menu.nodo;
						TransferAtributo clon_atributo = atributo.clonar();
						Vector<Object> v = new Vector<Object>();
						v.add(clon_atributo);
						v.add(true);
						controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EliminarAtributo, v);
					}
				});
				this.add(j7);
			} else {
				JMenuItem j7 = new JMenuItem(Lenguaje.text(Lenguaje.DELETE));
				j7.setFont(theme.font());
				j7.setForeground(theme.fontColor());
				j7.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						suprimir();
					}
				});
				this.add(j7);
			}

			this.add(new JSeparator());

			// Establecer clave primaria
			// Solamente estara activo cuando sea un atributo directo de una entidad
			final TransferEntidad ent = esAtributoDirecto((TransferAtributo) nodo);
			if (ent != null) {
				JCheckBoxMenuItem j6 = new JCheckBoxMenuItem(
						Lenguaje.text(Lenguaje.IS_PRIMARY_KEY) + " \"" + ent.getNombre() + "\"");
				j6.setFont(theme.font());
				j6.setForeground(theme.fontColor());
				if (((TransferAtributo) nodo).isClavePrimaria()) j6.setSelected(true);
				j6.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						MenuDesplegable menu = (MenuDesplegable) ((JMenuItem) e.getSource()).getParent();
						TransferAtributo atributo = (TransferAtributo) menu.nodo;
						TransferAtributo clon_atributo = atributo.clonar();
						TransferEntidad clon_entidad = ent.clonar();
						Vector<Transfer> vector = new Vector<Transfer>();
						vector.add(clon_atributo);
						vector.add(clon_entidad);
						controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EditarClavePrimariaAtributo,
								vector);
					}
				});
				this.add(j6);
			}
			// Es un atributo compuesto
			JCheckBoxMenuItem j3 = new JCheckBoxMenuItem(Lenguaje.text(Lenguaje.COMPOSED));
			j3.setFont(theme.font());
			j3.setForeground(theme.fontColor());
			final boolean notnul = ((TransferAtributo) nodo).getNotnull();
			final boolean unique = ((TransferAtributo) nodo).getUnique();
			if (((TransferAtributo) nodo).getCompuesto()) j3.setSelected(true);
			else j3.setSelected(false);
			j3.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					MenuDesplegable menu = (MenuDesplegable) ((JMenuItem) e.getSource()).getParent();
					TransferAtributo atributo = (TransferAtributo) menu.nodo;
					TransferAtributo clon_atributo = atributo.clonar();
					if (notnul) controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EditarNotNullAtributo,clon_atributo);
					if (unique) controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EditarUniqueAtributo, clon_atributo);
					controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EditarCompuestoAtributo,clon_atributo);
				}
			});
			this.add(j3);

			// Si es compuesto
			if (((TransferAtributo) nodo).getCompuesto()) {
				JMenuItem j4 = new JMenuItem(Lenguaje.text(Lenguaje.ADD_SUBATTRIBUTE));
				j4.setFont(theme.font());
				j4.setForeground(theme.fontColor());
				j4.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						MenuDesplegable menu = (MenuDesplegable) ((JMenuItem) e.getSource()).getParent();
						TransferAtributo atributo = (TransferAtributo) menu.nodo;
						TransferAtributo clon_atributo = atributo.clonar();
						controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_AnadirSubAtributoAAtributo,clon_atributo);
					}
				});
				this.add(j4);
			}

			// Es un atributo NotNull
			if (!((TransferAtributo) nodo).getCompuesto() && !((TransferAtributo) nodo).isClavePrimaria()) {
				JCheckBoxMenuItem j3a = new JCheckBoxMenuItem(Lenguaje.text(Lenguaje.NOT_NULL));
				j3a.setFont(theme.font());
				j3a.setForeground(theme.fontColor());
				if (((TransferAtributo) nodo).getNotnull()) j3a.setSelected(true);
				else j3a.setSelected(false);
				j3a.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						MenuDesplegable menu = (MenuDesplegable) ((JMenuItem) e.getSource()).getParent();
						TransferAtributo atributo = (TransferAtributo) menu.nodo;
						TransferAtributo clon_atributo = atributo.clonar();
						controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EditarNotNullAtributo,clon_atributo);
					}
				});
				this.add(j3a);
				// this.add(new JSeparator());
			}
			// Es un atributo Unique
			if (!((TransferAtributo) nodo).getCompuesto() && !((TransferAtributo) nodo).isClavePrimaria()) {
				JCheckBoxMenuItem j3b = new JCheckBoxMenuItem(Lenguaje.text(Lenguaje.UNIQUE));
				j3b.setFont(theme.font());
				j3b.setForeground(theme.fontColor());
				if (((TransferAtributo) nodo).getUnique()) j3b.setSelected(true);
				else j3b.setSelected(false);
				j3b.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						MenuDesplegable menu = (MenuDesplegable) ((JMenuItem) e.getSource()).getParent();
						TransferAtributo atributo = (TransferAtributo) menu.nodo;
						TransferAtributo clon_atributo = atributo.clonar();
						controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EditarUniqueAtributo,
								clon_atributo);
					}
				});
				this.add(j3b);
				// this.add(new JSeparator());
			}

			// Es un atributo multivalorado
			if (!((TransferAtributo) nodo).isClavePrimaria()) {
				JCheckBoxMenuItem j5 = new JCheckBoxMenuItem(Lenguaje.text(Lenguaje.VALUE_ATTRIBUTE));
				j5.setFont(theme.font());
				j5.setForeground(theme.fontColor());
				if (((TransferAtributo) nodo).isMultivalorado()) j5.setSelected(true);
				else j5.setSelected(false);
				j5.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						MenuDesplegable menu = (MenuDesplegable) ((JMenuItem) e.getSource()).getParent();
						TransferAtributo atributo = (TransferAtributo) menu.nodo;
						TransferAtributo clon_atributo = atributo.clonar();
						controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EditarMultivaloradoAtributo,
								clon_atributo);
					}
				});
				this.add(j5);
			}
			this.add(new JSeparator());
			// Añadir restricciones
			JMenuItem j8 = new JMenuItem(Lenguaje.text(Lenguaje.RESTRICTIONS));
			j8.setFont(theme.font());
			j8.setForeground(theme.fontColor());
			j8.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					MenuDesplegable menu = (MenuDesplegable) ((JMenuItem) e.getSource()).getParent();
					TransferAtributo atributo = (TransferAtributo) menu.nodo;
					TransferAtributo clon_atributo = atributo.clonar();
					controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_AnadirRestriccionAAtributo,clon_atributo);
				}
			});
			this.add(j8);
		}

		if (nodo instanceof TransferRelacion) { // Si es relación
			// Si es una relacion IsA
			if (((TransferRelacion) nodo).getTipo().equals("IsA")) {
				JMenuItem m1 = new JMenuItem(Lenguaje.text(Lenguaje.SET_PARENT_ENT));
				m1.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						MenuDesplegable menu = (MenuDesplegable) ((JMenuItem) e.getSource()).getParent();
						TransferRelacion relacion = (TransferRelacion) menu.nodo;
						TransferRelacion clon_relacion = relacion.clonar();
						controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EstablecerEntidadPadre,clon_relacion);
					}
				});
				m1.setFont(theme.font());
				m1.setForeground(theme.fontColor());
				this.add(m1);
				JMenuItem m2 = new JMenuItem(Lenguaje.text(Lenguaje.REMOVE_PARENT_ENT));
				m2.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						MenuDesplegable menu = (MenuDesplegable) ((JMenuItem) e.getSource()).getParent();
						TransferRelacion relacion = (TransferRelacion) menu.nodo;
						TransferRelacion clon_relacion = relacion.clonar();
						controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_QuitarEntidadPadre,clon_relacion);
					}
				});
				m2.setFont(theme.font());
				m2.setForeground(theme.fontColor());
				this.add(m2);
				this.add(new JSeparator());
				JMenuItem m3 = new JMenuItem(Lenguaje.text(Lenguaje.ADD_CHILD_ENT));
				m3.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						MenuDesplegable menu = (MenuDesplegable) ((JMenuItem) e.getSource()).getParent();
						TransferRelacion relacion = (TransferRelacion) menu.nodo;
						TransferRelacion clon_relacion = relacion.clonar();
						controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_AnadirEntidadHija, clon_relacion);
					}
				});
				m3.setFont(theme.font());
				m3.setForeground(theme.fontColor());
				this.add(m3);
				JMenuItem m4 = new JMenuItem(Lenguaje.text(Lenguaje.REMOVE_CHILD_ENT));
				m4.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						MenuDesplegable menu = (MenuDesplegable) ((JMenuItem) e.getSource()).getParent();
						TransferRelacion relacion = (TransferRelacion) menu.nodo;
						TransferRelacion clon_relacion = relacion.clonar();
						controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_QuitarEntidadHija, clon_relacion);
					}
				});
				m4.setFont(theme.font());
				m4.setForeground(theme.fontColor());
				this.add(m4);
				this.add(new JSeparator());
				// Eliminar la relacion
				// Si sólo está seleccionada la relacion..
				PickedState<Transfer> p = vv.getPickedVertexState();
				int seleccionados = 0;
				for (@SuppressWarnings("unused")Transfer t : p.getPicked()) seleccionados++;
				
				if (seleccionados < 2) {
					JMenuItem m5 = new JMenuItem(Lenguaje.text(Lenguaje.DELETE_REL));
					m5.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							MenuDesplegable menu = (MenuDesplegable) ((JMenuItem) e.getSource()).getParent();
							TransferRelacion relacion = (TransferRelacion) menu.nodo;
							TransferRelacion clon_relacion = relacion.clonar();
							Vector<Object> v = new Vector<Object>();
							v.add(clon_relacion);
							v.add(true);
							controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EliminarRelacionIsA, v);
						}
					});
				m5.setFont(theme.font());
				m5.setForeground(theme.fontColor());
				this.add(m5);
				} else {
					JMenuItem j6 = new JMenuItem(Lenguaje.text(Lenguaje.DELETE));
					j6.setFont(theme.font());
					j6.setForeground(theme.fontColor());
					j6.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							suprimir();
						}
					});
					this.add(j6);
				}
			}

			// Si es una relacion "Normal"
			else {
				// Anadir una entidad
				JMenuItem j3 = new JMenuItem(Lenguaje.text(Lenguaje.ADD_ENT));
				j3.setFont(theme.font());
				j3.setForeground(theme.fontColor());
				if (((TransferRelacion) nodo).getTipo().equals("Debil")) j3.setEnabled(false);
				else {
					j3.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							MenuDesplegable menu = (MenuDesplegable) ((JMenuItem) e.getSource()).getParent();
							TransferRelacion relacion = (TransferRelacion) menu.nodo;
							TransferRelacion clon_relacion = relacion.clonar();
							controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_AnadirEntidadARelacion,clon_relacion);
						}
					});
				}
				this.add(j3);

				// Quitar una entidad
				JMenuItem j4 = new JMenuItem(Lenguaje.text(Lenguaje.REMOVE_ENTITY));
				j4.setFont(theme.font());
				j4.setForeground(theme.fontColor());
				// Si es débil
				if (((TransferRelacion) nodo).getTipo().equals("Debil")) j4.setEnabled(false);
				// Si es normal
				else {
					j4.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							MenuDesplegable menu = (MenuDesplegable) ((JMenuItem) e.getSource()).getParent();
							TransferRelacion relacion = (TransferRelacion) menu.nodo;
							TransferRelacion clon_relacion = relacion.clonar();
							controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_QuitarEntidadARelacion,clon_relacion);
						}
					});
				}
				this.add(j4);
				// Editar la aridad de una entidad
				JMenuItem j5 = new JMenuItem(Lenguaje.text(Lenguaje.EDIT_CARD_ROL));
				j5.setFont(theme.font());
				j5.setForeground(theme.fontColor());
				if (((TransferRelacion) nodo).getTipo().equals("Debil")) j5.setEnabled(false);
				else {
					j5.setEnabled(true);
					j5.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							MenuDesplegable menu = (MenuDesplegable) ((JMenuItem) e.getSource()).getParent();
							TransferRelacion relacion = (TransferRelacion) menu.nodo;
							TransferRelacion clon_relacion = relacion.clonar();
							controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EditarCardinalidadEntidad,clon_relacion);
						}
					});
				}
				this.add(j5);
				this.add(new JSeparator());

				// Anadir un atributo a la relacion
				JMenuItem j6 = new JMenuItem(Lenguaje.text(Lenguaje.ADD_ATTRIBUTE));
				j6.setFont(theme.font());
				j6.setForeground(theme.fontColor());
				if (((TransferRelacion) nodo).getTipo().equals("Debil"))
					j6.setEnabled(false);
				else {
					j6.setEnabled(true);
					j6.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							MenuDesplegable menu = (MenuDesplegable) ((JMenuItem) e.getSource()).getParent();
							TransferRelacion relacion = (TransferRelacion) menu.nodo;
							TransferRelacion clon_relacion = relacion.clonar();
							controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_AnadirAtributoRelacion,clon_relacion);
						}
					});
				}
				this.add(j6);
				this.add(new JSeparator());

				// Renombrar la relacion
				JMenuItem j1 = new JMenuItem(Lenguaje.text(Lenguaje.RENAME_RELATION));
				j1.setFont(theme.font());
				j1.setForeground(theme.fontColor());
				j1.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						MenuDesplegable menu = (MenuDesplegable) ((JMenuItem) e.getSource()).getParent();
						TransferRelacion relacion = (TransferRelacion) menu.nodo;
						TransferRelacion clon_relacion = relacion.clonar();
						controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_RenombrarRelacion, clon_relacion);
					}
				});
				this.add(j1);

				// Eliminar la relacion
				// Si sólo está seleccionada la relacion..
				PickedState<Transfer> p = vv.getPickedVertexState();
				int seleccionados = 0;
				for (@SuppressWarnings("unused") Transfer t : p.getPicked()) seleccionados++;
				if (seleccionados < 2) {
					JMenuItem j7 = new JMenuItem(Lenguaje.text(Lenguaje.DELETE_REL));
					j7.setFont(theme.font());
					j7.setForeground(theme.fontColor());
					j7.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							MenuDesplegable menu = (MenuDesplegable) ((JMenuItem) e.getSource()).getParent();
							TransferRelacion relacion = (TransferRelacion) menu.nodo;
							TransferRelacion clon_relacion = relacion.clonar();
							Vector<Object> v = new Vector<Object>();
							v.add(clon_relacion);
							v.add(true);
							controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EliminarRelacionNormal, v);
						}
					});
					this.add(j7);
				} else {
					JMenuItem j7 = new JMenuItem(Lenguaje.text(Lenguaje.DELETE));
					j7.setFont(theme.font());
					j7.setForeground(theme.fontColor());
					j7.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							suprimir();
						}
					});
					this.add(j7);
				}
				this.add(new JSeparator());

				// Añadir restricciones
				JMenuItem j8 = new JMenuItem(Lenguaje.text(Lenguaje.RESTRICTIONS));
				j8.setFont(theme.font());
				j8.setForeground(theme.fontColor());
				j8.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						MenuDesplegable menu = (MenuDesplegable) ((JMenuItem) e.getSource()).getParent();
						TransferRelacion relacion = (TransferRelacion) menu.nodo;
						TransferRelacion clon_relacion = relacion.clonar();
						controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_AnadirRestriccionARelacion,clon_relacion);
					}
				});
				this.add(j8);
				// Añadir tablaUnique
				JMenuItem j9 = new JMenuItem(Lenguaje.text(Lenguaje.TABLE_UNIQUE));
				j9.setFont(theme.font());
				j9.setForeground(theme.fontColor());
				j9.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						MenuDesplegable menu = (MenuDesplegable) ((JMenuItem) e.getSource()).getParent();
						TransferRelacion relacion = (TransferRelacion) menu.nodo;
						TransferRelacion clon_relacion = relacion.clonar();
						controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_TablaUniqueARelacion,
								clon_relacion);
					}
				});
				this.add(j9);
			} // else
		}

	}
	
	protected void suprimir() {
		PickedState<Transfer> p = vv.getPickedVertexState();
		int seleccionados = 0;
		for (@SuppressWarnings("unused")Transfer t : p.getPicked()) seleccionados++;
		int respuesta = 1;
		if (seleccionados > 1) {
			respuesta = this.controlador.getPanelOpciones().setActiva(
					Lenguaje.text(Lenguaje.DELETE_ALL_NODES) + "\n" + Lenguaje.text(Lenguaje.WISH_CONTINUE),
					Lenguaje.text(Lenguaje.DBCASE_DELETE));
		}
		if (respuesta == 0 || seleccionados == 1) {
			PickedState<Transfer> ps = vv.getPickedVertexState();
			for (Transfer t : ps.getPicked()) {
				Vector<Object> v = new Vector<Object>();
				v.add(t);
				v.add(respuesta == 1);
				if (t instanceof TransferEntidad)
					controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EliminarEntidad, v);

				if (t instanceof TransferAtributo)
					controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EliminarAtributo, v);

				if (t instanceof TransferRelacion)
					if (((TransferRelacion) t).getTipo().equals("IsA"))
						controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EliminarRelacionIsA, v);
					else controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EliminarRelacionNormal, v);
			}
		}
	}
	
	@Override
    public void paintComponent(final Graphics g) {
        g.setColor(theme.toolBar());
        g.fillRect(0,0,getWidth(), getHeight());
    }
	
	/**
	 * Metodo auxiliar para saber si hay que mostrar en el popup de los atributos la
	 * opcion de "Es clave primaria".
	 * 
	 * @param ta
	 *            - Atributo que se quiere consultar
	 * @return te -> la entidad a la que pertenece null -> en otro caso (sera un
	 *         subatributo o sera un atributo de una relacion)
	 */
	private TransferEntidad esAtributoDirecto(TransferAtributo ta) {
		Collection<TransferEntidad> listaEntidades = this.entidades.values();
		for (Iterator<TransferEntidad> it = listaEntidades.iterator(); it.hasNext();) {
			TransferEntidad te = it.next();
			if (te.getListaAtributos().contains(String.valueOf(ta.getIdAtributo()))) return te;
		}
		return null;
	}

	protected void setControlador(Controlador controlador) {
		this.controlador = controlador;
	}
}
