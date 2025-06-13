package domain.graph;

import util.Utility;

import java.util.Objects;

public class EdgeWeight {
    private Object edge; //arista
    private Object weight; //peso

    public EdgeWeight(Object edge, Object weight) {
        this.edge = edge;
        this.weight = weight;
    }

    public Object getEdge() {
        return edge;
    }

    public void setEdge(Object edge) {
        this.edge = edge;
    }

    public Object getWeight() {
        return weight;
    }

    public void setWeight(Object weight) {
        this.weight = weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EdgeWeight that = (EdgeWeight) o;
        // Dos EdgeWeight son iguales si su 'edge' (el vértice de destino) es igual.
        // Ignoramos el 'weight' para propósitos de identificar si una arista existe o no.
        // Usamos Utility.compare para la comparación de 'edge'.
        return Utility.compare(edge, that.edge) == 0;
    }

    @Override
    public int hashCode() {
        // El hashCode debe ser consistente con equals.
        // Si equals solo usa 'edge', hashCode también debe basarse solo en 'edge'.
        // Esto asume que el `hashCode` de `edge` (el objeto Airport) está bien definido.
        return Objects.hash(edge);
    }

    @Override
    public String toString() {
        if(weight==null) return "Edge="+edge;
        else return "Edge="+edge+". Weight="+weight;
    }
}