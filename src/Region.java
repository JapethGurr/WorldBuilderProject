import java.util.ArrayList;

public class Region {
    ArrayList<Cell> area;
    ArrayList<Region> neighbours;
    boolean land = true;
    GeographicalRegion geographicalRegion;
    boolean border = false;

    public Region(){
        area = new ArrayList<>();
        neighbours = new ArrayList<>();
    }
}
