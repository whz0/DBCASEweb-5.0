package modelo.transfers;

public class Node { 
	String label;
	boolean strong;
	String shape;
	String color;
	int scale;
	int widthConstraints;
	int heightConstraints;
	boolean physics;
	int id;
	DataAttribute dataAttribute;
	int id_origin;
	boolean isWeak;

	int superEntity;

	public Node() {
		this.id_origin = -1;
	}
	
	public Node(String label, boolean strong, String shape, String color, int scale, int widthConstraints,
			int heightConstraints, boolean physics, int id, DataAttribute dataAttribute, int superEntity) {
		this.label = label;
		this.strong = strong;
		this.shape = shape;
		this.color = color;
		this.scale = scale;
		this.widthConstraints = widthConstraints;
		this.heightConstraints = heightConstraints;
		this.physics = physics;
		this.id = id;
		this.dataAttribute = dataAttribute;
		this.id_origin = -1;
		this.isWeak = false;
		this.superEntity = superEntity;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isStrong() {
		return strong;
	}

	public void setStrong(boolean strong) {
		this.strong = strong;
	}

	public String getShape() {
		return shape;
	}

	public void setShape(String shape) {
		this.shape = shape;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	public int getWidthConstraints() {
		return widthConstraints;
	}

	public void setWidthConstraints(int widthConstraints) {
		this.widthConstraints = widthConstraints;
	}

	public int getHeightConstraints() {
		return heightConstraints;
	}

	public void setHeightConstraints(int heightConstraints) {
		this.heightConstraints = heightConstraints;
	}

	public boolean isPhysics() {
		return physics;
	}

	public void setPhysics(boolean physics) {
		this.physics = physics;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSuperEntity() {
		return superEntity;
	}

	public void setSuperEntity(int superEntity) {
		this.superEntity = superEntity;
	}

	public DataAttribute getDataAttribute() {
		return dataAttribute;
	}

	public void setDataAttribute(DataAttribute dataAttribute) {
		this.dataAttribute = dataAttribute;
	}

	public int getId_origin() {
		return id_origin;
	}

	public void setId_origin(int id_origin) {
		this.id_origin = id_origin;
	}

	public boolean isWeak() {
		return isWeak;
	}

	public void setWeak(boolean isWeak) {
		this.isWeak = isWeak;
	}

	public String getTypeNode(){
		return isWeak?"Debil":"Normal";
	}
 
}