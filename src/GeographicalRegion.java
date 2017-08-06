import java.util.ArrayList;

public class GeographicalRegion {
    ArrayList<Region> area;
    ArrayList<GeographicalRegion> neighbours;
    boolean border = false;
    GeographyType type;

    public enum GeographyType {
        SEA,LAKE,CONTINENT,ISLAND
    }

    public GeographicalRegion(){
        area = new ArrayList<>();
        neighbours = new ArrayList<>();
    }
}
