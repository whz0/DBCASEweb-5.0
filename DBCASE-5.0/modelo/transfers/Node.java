package modelo.transfers;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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

    public Node(String label,
                boolean strong,
                String shape,
                String color,
                int scale,
                int widthConstraints,
                int heightConstraints,
                boolean physics,
                int id,
                DataAttribute dataAttribute,
                int superEntity) {
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

    public String getTypeNode() {
        return isWeak ? "Debil" : "Normal";
    }

}