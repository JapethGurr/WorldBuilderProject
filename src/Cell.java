import java.util.ArrayList;

public class Cell {
    ArrayList<Pixel> area;
    ArrayList<Cell> neighbours;
    Region parentRegion;
    int averageColor;
    boolean border = false;
    double elevation = 0;
    double moisture = 0;
    Biome biome = null;

    public Cell(){
        area = new ArrayList<>();
        neighbours = new ArrayList<>();
    }

    public enum Biome{
        BEACH,SEA,LAKE,RIVER,SNOW,TUNDRA,BARE,SCORCHED,TAIGA,SHRUBLAND,TEMPERATE_DESERT,TEMPERATE_RAIN_FOREST,
        SUBTROPICAL_DESERT,TEMPERATE_DECIDUOUS_FOREST,GRASSLAND,TROPICAL_RAIN_FOREST,TROPICAL_SEASONAL_FOREST
    }
}
