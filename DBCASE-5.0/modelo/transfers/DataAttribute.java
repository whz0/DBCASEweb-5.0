package modelo.transfers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DataAttribute {
    private boolean primaryKey;
    private boolean composite;
    private boolean notNull;
    private boolean unique;
    private boolean multivalued;
    private String domain;
    private String size;
}
