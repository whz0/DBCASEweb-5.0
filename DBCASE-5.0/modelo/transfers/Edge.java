package modelo.transfers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
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

    public void updateLabelFromName() {
        this.label = this.name;
    }

}
