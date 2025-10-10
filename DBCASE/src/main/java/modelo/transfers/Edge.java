package modelo.transfers;

public class Edge {
	
	int from;
	int to;
	Color color;
	String id;
	String label;
	String labelFrom;
	String labelTo;
	String name;
	String type;
	
	public Edge() {
		
	} 
	
	public Edge(int from, int to, Color color, String id, String label, String labelFrom, String labelTo, String name,String type) {
		this.from = from;
		this.to = to;
		this.color = color;
		this.id = id;
		this.label = label;
		this.labelFrom = labelFrom;
		this.labelTo = labelTo;
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getFrom() {
		return from;
	}

	public void setFrom(int from) {
		this.from = from;
	}

	public int getTo() {
		return to;
	}

	public void setTo(int to) {
		this.to = to;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabelFrom() {
		return labelFrom;
	}

	public void setLabelFrom(String labelFrom) {
		this.labelFrom = labelFrom;
	}

	public String getLabelTo() {
		return labelTo;
	}

	public void setLabelTo(String labelTo) {
		this.labelTo = labelTo;
	}
	
	public void updateLabelFromName() {
		this.label = this.name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}
