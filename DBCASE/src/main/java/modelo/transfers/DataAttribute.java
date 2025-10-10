package modelo.transfers;

public class DataAttribute {
	private boolean primaryKey;
	private	boolean composite;
	private	boolean notNull;
	private	boolean unique;
	private	boolean multivalued;
	private	String domain;
	private	String size; 
	
	public DataAttribute(){
		
	}
			
	public DataAttribute(boolean primaryKey, boolean composite, boolean notNull, boolean unique, boolean multivalued,
			String domain, String size) { 
		this.primaryKey = primaryKey;
		this.composite = composite;
		this.notNull = notNull;
		this.unique = unique;
		this.multivalued = multivalued;
		this.domain = domain;
		this.size = size;
	}
	
	public boolean isPrimaryKey() {
		return primaryKey;
	}
	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
	}
	public boolean isComposite() {
		return composite;
	}
	public void setComposite(boolean composite) {
		this.composite = composite;
	}
	public boolean isNotNull() {
		return notNull;
	}
	public void setNotNull(boolean notNull) {
		this.notNull = notNull;
	}
	public boolean isUnique() {
		return unique;
	}
	public void setUnique(boolean unique) {
		this.unique = unique;
	}
	public boolean isMultivalued() {
		return multivalued;
	}
	public void setMultivalued(boolean multivalued) {
		this.multivalued = multivalued;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	
	
}
