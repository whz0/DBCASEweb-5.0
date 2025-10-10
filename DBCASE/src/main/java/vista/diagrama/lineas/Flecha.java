package vista.diagrama.lineas;

import java.awt.*;
import java.awt.geom.*;

import javax.swing.*;
import javax.swing.event.*;

import vista.diagrama.geometria.Punto;
import vista.diagrama.geometria.Recta;
import vista.tema.Theme;


@SuppressWarnings("serial")
public class Flecha extends JPanel implements ChangeListener {
    Path2D.Double arrow = createArrow();
    //Coordenadas x e y desde donde se dibuja la flecha
    private float cx;
    private float cy;
    //Variables para calcular la inclinación con la que se dibujará la flecha
    private float dx;
    private float dy;
    //Longitud de la flecha
    private double length;
    private double theta = 0;
    private Punto corte;
    private Theme theme = Theme.getInstancia();
    
    public void stateChanged(ChangeEvent e) {
        int value = ((JSlider)e.getSource()).getValue();
        theta = Math.toRadians(value);
        repaint();
    }
   
    public void paintComponent(Graphics g,float xIsA,float yIsA,float xEnti,float yEnti,boolean esPadre,int anchoRect) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON); 
        if(esPadre){
        	this.dx = -(float) (xEnti-corte.x);
            this.dy = -(float) (yEnti-corte.y);
        	this.cx = (float) ((xEnti+corte.x)/2);
    		this.cy = (float) ((yEnti+corte.y)/2);
        }
        else{
        	this.dx = -(float) (xIsA-corte.x);
            this.dy = -(float) (yIsA-corte.y);
        	this.cx = (float) ((xIsA+corte.x)/2);
    		this.cy = (float) ((yIsA+corte.y)/2);
        }
        
        theta = (float) Math.atan2(this.dy, this.dx); //Inclinación de la flecha       
    	
        /*Hay que pasarle el centro desde donde se pinta la flecha.
         * y hace desce el centro tiro la longitud hacia la izq, y hacia la derecha*/
        AffineTransform at = AffineTransform.getTranslateInstance(this.cx,this.cy);
        at.rotate(theta);
        at.scale(2.0, 2.0);
        Shape shape = at.createTransformedShape(arrow);
        g2.setPaint(theme.lines());
        if (arrow!= null)g2.draw(shape);
    }
    
    private Punto calcularInterseccion(float yEnti,float yIsA,float xEnti,float xIsA,boolean esPadre,int anchoRect){
    	Punto interseccion= new Punto();
    	Punto cIsa = new Punto((double)xIsA,(double)yIsA);//IsA
    	Punto cEnt = new Punto((double)xEnti,(double)yEnti);//Entidad
    	Recta rCentros = null;
    	try {
			rCentros = new Recta( cIsa, cEnt );
		} catch (Exception e) {
			e.printStackTrace();
		}
		//Variables para calcular las coordenadas del triángulo (Ver TransferRelacion)
		int tri1 = 25;
		int tri2 = 15;
		int tri3 = 27;
		/*Variables para calcular las coordenadas del rectángulo(Ver TransferRelacion)
		 * Sólo importa cuando la flecha va de IsA-> Hija. La longitud del rectángulo depende de la 
		 * longitud del nombre de la entidad hija.*/
		int ancho; 
		int alto;
		if(anchoRect < 8){
			ancho = 45;
			alto= 18;
		}
		else{
			ancho= (anchoRect*5)+5;
			alto=18;
		}		
		//Rectas que definen el triangulo y el rectángulo
		Recta rSup = null;
		Recta rDech = null;
		Recta rIzq= null;
		Recta rInf= null;//Esta recta sólo se utiliza para el rectángulo en el caso IsA->Hija
		Recta rectaAIntersecar = null;
		int ri = 0;//Recta interseccion-> indica en que arísta está el punto de corte
		//Auxiliares para calcular las rectas de intersección
		double distAInf;
		double distAIzq;
		double distASup;
		double distADch;
		
		//La flecha va de la entidad padre a la relación IsA
		if (esPadre){
			//Creo los puntos que definen los vertices de la IsA
			Punto izq = new Punto((double)xIsA-tri1,(double)yIsA-tri2);
			Punto dech = new Punto((double)xIsA+tri1,(double)yIsA-tri2);
			Punto inf = new Punto((double)xIsA,(double)yIsA+tri3);
			//Creo las rectas que unen los vertices y definen el triángulo
			try {
				rSup = new Recta( izq, dech ); 	// ri = 1
				rDech = new Recta( inf, dech ); // ri = 2
				rIzq = new Recta( inf, izq ); 	// ri = 3
				rectaAIntersecar=rIzq;//Le asigno un valor por defecto distinto de null
			} catch (Exception e) {
				e.printStackTrace();
			}
			/*Calculo la interseccion entre las rectas que unen los centros de la entidad padre
			 * con la IsA, y las aristas del triangulo */
			ri = 0;
			if( xEnti >= inf.x ){
				distAInf = distanciaEntrePuntos( xEnti, yEnti, inf.x, inf.y );
				distAIzq = distanciaEntrePuntos( xEnti, yEnti, izq.x, izq.y );
				if( distAInf > distAIzq ){
					rectaAIntersecar = rSup;
					ri = 1;
				}else{
					rectaAIntersecar = rDech;
					ri = 2;
				}
			}else{

				distAInf = distanciaEntrePuntos( xEnti, yEnti, inf.x, inf.y );
				distADch = distanciaEntrePuntos( xEnti, yEnti, dech.x, dech.y );
				if( distAInf > distADch ){
					rectaAIntersecar = rSup;
					ri = 1;
				}else{
					rectaAIntersecar = rIzq;
					ri = 3;
				}
			}
			interseccion = rCentros.interseccion( rectaAIntersecar );
			/*Si por error de redondeo la interseccion no cae justo en las rectas se aproxima al vértice
			 * más cercano*/
			if(! checkInteresct( ri, interseccion, izq, dech, inf ) ){
				if((ri==1)&&(xEnti>=xIsA))
					interseccion=dech;
				else if((ri==1)&&(xEnti<xIsA))
					interseccion=izq;
				else if((ri==2)&&(yEnti<=yIsA))
					interseccion=dech;
				else if((ri==2)&&(yEnti>yIsA))
					interseccion=inf;
				else if((ri==3)&&(yEnti<=yIsA)&&(xEnti<=xIsA))
					interseccion=izq;
				else if((ri==3)&&(yEnti<=yIsA)&&(xEnti>xIsA))
					interseccion=dech;
				else if((ri==3)&&(yEnti<yIsA))
					interseccion=inf;
			}
			this.cx = (float) ((cEnt.x+interseccion.x)/2);
			this.cy = (float) ((cEnt.y+interseccion.y)/2);
			/*La longitud se divide entre dos para que vaya desde el centro la mitad para cada lado
			 * y le quito 10 para que no se meta dentro de los picos del triángulo*/
			this.length=(distanciaEntrePuntos(cEnt.x,cEnt.y,interseccion.x,interseccion.y)/2)-10;
				
 		}
		//Si la flecha va de la IsA a la entidad hija
		else{
			//Creo los puntos que definien los vértices del rectángulo de la entidad hija
			Punto supIzq = new Punto((double)xEnti-ancho,(double)yEnti-alto);
			Punto supDech = new Punto((double)xEnti+ancho,(double)yEnti-alto);
			Punto infIzq = new Punto((double)xEnti-ancho,(double)yEnti+alto);
			Punto infDech = new Punto((double)xEnti+ancho,(double)yEnti+alto);
			//Creo las rectas que unen los vértices y definen el rectangulo
			try {
				rSup = new Recta( supIzq, supDech );   // ri = 1
				rDech = new Recta( supDech, infDech ); // ri = 2
				rIzq = new Recta( supIzq, infIzq );    // ri = 3
				rInf = new Recta(infIzq, infDech);     // ri = 4
				rectaAIntersecar=rIzq;//Le asigno un valor por defecto distinto de null
			} catch (Exception e) {
				e.printStackTrace();
			}
			//Calculo con qué arista corta
			
			//Está a la izquierda de la entidad
			if(xIsA <= supIzq.x){
				//Cuadrante de la esquina supIzq
				if(yIsA <= supIzq.y){
					distASup = distanciaEntrePuntos( xIsA, yIsA, supDech.x, supDech.y );
					distAIzq = distanciaEntrePuntos( xIsA, yIsA, infIzq.x, infIzq.y );
					//Corta con la arista izquierda
					if( distASup > distAIzq ){
						rectaAIntersecar = rIzq;
						ri = 3;
					//Corta con la arista superior
					}else{
						rectaAIntersecar = rSup;
						ri = 1;
					}
				}
				//Medio, corta con la arista izquierda
				else if((supIzq.y < yIsA) && (yIsA < infIzq.y)){
					rectaAIntersecar = rIzq;
					ri = 3;
				}
				//Cuadrante de la esquina infIzq
				else if(yIsA >= infIzq.y){
					distAInf = distanciaEntrePuntos( xIsA, yIsA, infDech.x, infDech.y );
					distAIzq = distanciaEntrePuntos( xIsA, yIsA, supIzq.x, supIzq.y );
					//Corta con la arista izquierda
					if( distAInf > distAIzq ){
						rectaAIntersecar = rIzq;
						ri = 3;
					//Corta con la arista inferior
					}else{
						rectaAIntersecar = rInf;
						ri = 4;
					}
				}
			}
			//Está por encima de la entidad (medio )y corta con la arista superior
			else if((yIsA <= supIzq.y) && (xIsA > supIzq.x ) && (xIsA <= supDech.x)){
				rectaAIntersecar=rSup;
				ri = 1;
			}
			//Está por debajo de la entidad (medio )y corta con la arista inferior
			else if((yIsA > infIzq.y) && (xIsA > infIzq.x ) && (xIsA <= infDech.x)){
				rectaAIntersecar=rInf;
				ri = 4;
			}
			//Está a la derecha de la entidad 
			else if(xIsA > supDech.x){
					//cuadrante de la esquina supDech
					if(yIsA < supDech.y){
						distAInf = distanciaEntrePuntos( xIsA, yIsA, infDech.x, infDech.y );
						distAIzq = distanciaEntrePuntos( xIsA, yIsA, supIzq.x, supIzq.y );
						//Corta con la superior
						if( distAInf > distAIzq ){
							rectaAIntersecar = rSup;
							ri = 1;
						//Corta con la derecha
						}else{
							rectaAIntersecar = rDech;
							ri = 2;
						}
					}
					//Medio
					else if((yIsA >= supDech.y) && (yIsA <= infDech.y)){
						rectaAIntersecar = rDech;
						ri = 2;
					}
					
					else if(yIsA >= infDech.y){
						distAInf = distanciaEntrePuntos( xIsA, yIsA, infIzq.x, infIzq.y );
						distADch = distanciaEntrePuntos( xIsA, yIsA, supDech.x, supDech.y );
						//Corta con la superior
						if( distAInf > distADch ){
							rectaAIntersecar = rDech;
							ri = 2;
						//Corta con la derecha
						}else{
							rectaAIntersecar = rInf;
							ri = 4;
						}
					}
			}
			interseccion = rCentros.interseccion( rectaAIntersecar );			
			//Si por errores de calculo queda un poco separada la aproximo a la esquina más cercana
				if((ri==1)&&(xIsA>=supDech.x))
					interseccion=supDech;
				else if((ri==1)&&(xIsA<supIzq.x))
					interseccion=supIzq;
				else if((ri==2)&&(yIsA<=supDech.y))
					interseccion=supDech;
				else if((ri==2)&&(yIsA>infDech.y))
					interseccion=infDech;
				else if((ri==3)&&(yIsA<=supIzq.y)&&(xIsA<=supIzq.x))
					interseccion=supIzq;
				else if((ri==3)&&(yIsA>=infIzq.y))
					interseccion=infIzq;
				else if((ri==4)&&(xIsA > infDech.x)&&(yIsA > infDech.y))
					interseccion=infDech;
			this.cx = (float) ((cIsa.x+interseccion.x)/2);
			this.cy = (float) ((cIsa.y+interseccion.y)/2);
			this.length=(distanciaEntrePuntos(cIsa.x,cIsa.y,interseccion.x,interseccion.y)/2);
				
		}
		
    	return interseccion;    	
    }
    
    private boolean checkInteresct( int ri, Punto interseccion, Punto izq, Punto dech, Punto inf) {
		// recta izq2 va de 
    	Recta r = null;
    	try{
    		if( ri == 1 )	r = new Recta( izq, dech ); 	// ri = 1
			if( ri == 2 ) 	r = new Recta( inf, dech ); // ri = 2
			if( ri == 3 ) 	r = new Recta( inf, izq ); 	// ri = 3
	    	if( r.pasaPor( interseccion )){
	    		if( ri == 1 ){
	    			if( ( interseccion.x < izq.x ) || ( interseccion.x > dech.x) ){
	    				return false;
	    			}
	    			return true;
	    		}else if( ri == 2){
	    			if( ( interseccion.x < inf.x) || (interseccion.x > dech.x) 
	    				|| (interseccion.y < dech.y) || ( interseccion.y > inf.y) ){
	    				return false;
	    			}
	    			return true;
	    		}else if( ri == 3){
	    			if( ( interseccion.x > inf.x) || (interseccion.x < izq.x) 
		    				|| (interseccion.y < izq.y) || ( interseccion.y > inf.y) ){
		    			return false;
		    		}
		    		return true;
	    		}
	    	}else{
	    		return false;
	    	}
    	}catch( Exception e ){
    		return false;
    	}
		return false;
	}

	private double distanciaEntrePuntos(double x1,double y1,double x2,double y2){
    	double dist;
    	dist =Math.sqrt(Math.pow((x1-x2),2.0)+ Math.pow((y1-y2),2.0));
    	return dist;
    }
    
    private Path2D.Double createArrow() {
        this.length = 70;//Longitud de la flecha
        int barb = 5;//Tamaño del pico
        double angle = Math.toRadians(20);
        Path2D.Double path = new Path2D.Double();
        path.moveTo(-this.length/2, 0);
        path.lineTo(this.length/2, 0);
        double x = this.length/2 - barb*Math.cos(angle);
        double y = barb*Math.sin(angle);
        path.lineTo(x, y);//Crear la linea del pico de arriba  ---\
        x = this.length/2 - barb*Math.cos(-angle);
        y = barb*Math.sin(-angle);
        path.moveTo(this.length/2, 0);
        path.lineTo(x, y); //Crear la linea del pico de abajo  ---/
        this.arrow = path;
        return path;
    }
    
    public Path2D.Double createArrow(float yEnti,float yIsA,float xEnti,float xIsA,boolean esPadre,int anchoRect) {
    	this.corte =calcularInterseccion(yEnti,yIsA, xEnti,xIsA, esPadre,anchoRect);  
        if(esPadre){
        	this.dx = xEnti-(float)this.corte.x;
        	this.dy = yEnti-(float)this.corte.y;
        }
        else{
        	this.dx = xIsA-(float)this.corte.x;
        	this.dy = yIsA-(float)this.corte.y;
        }
       theta = (float) Math.atan2(dy, dx); //Inclinación de la flecha       
    	int barb = 5;//Tamaño del pico 	
        double angle = Math.toRadians(20);
        Path2D.Double path = new Path2D.Double();
        path.moveTo(-length/2, 0);
        path.lineTo(length/2, 0);
        double x = (length/2 - barb*Math.cos(angle));
        double y = barb*Math.sin(angle);
        path.lineTo(x, y);//Crear la linea del pico de arriba  ---\
        x = (length/2 - barb*Math.cos(-angle));
        y =( barb*Math.sin(-angle));
        path.moveTo(length/2, 0);
        path.lineTo(x, y);//Crear la linea del pico de abajo  ---/
        this.arrow = path;
        return path;
    } 
    
}




	
	         

    
    
    
    
    
    
    
    

