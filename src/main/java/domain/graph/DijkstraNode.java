package domain.graph;

import domain.common.Airport;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DijkstraNode {
    Airport airport;
    double cost; //Costo TOTAL desde el origen
    List<Airport> path; //Ruta hasta este nodo

    public DijkstraNode(Airport airport, double cost, List<Airport> path) {
        this.airport = airport;
        this.cost = cost;
        this.path = new ArrayList<>(path); //Copiar la lista para evitar mutaciones
    }

    public Airport getAirport() {
        return airport;
    }

    public void setAirport(Airport airport) {
        this.airport = airport;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public List<Airport> getPath() {
        return path;
    }

    public void setPath(List<Airport> path) {
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DijkstraNode that = (DijkstraNode) o;
        return Objects.equals(airport, that.airport);
    }

    @Override
    public int hashCode() {
        return Objects.hash(airport);
    }
}
