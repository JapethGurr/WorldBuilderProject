import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Japeth on 01/05/2017.
 */
public class SettingsManager {
    public enum Feature{
        CAPITAL,CITY,TOWN,VILLAGE,FARM,DOCKS,FORREST,MOUNTAINS
    }

    //TODO Make biomemap loadable?
    // [Moisture][Elevation]
    Cell.Biome[][] biomeMap = new Cell.Biome[][]{
            {Cell.Biome.SUBTROPICAL_DESERT, Cell.Biome.TEMPERATE_DESERT, Cell.Biome.TEMPERATE_DESERT,
                    Cell.Biome.SCORCHED},
            {Cell.Biome.GRASSLAND, Cell.Biome.GRASSLAND, Cell.Biome.TEMPERATE_DESERT, Cell.Biome.BARE},
            {Cell.Biome.TROPICAL_SEASONAL_FOREST, Cell.Biome.GRASSLAND, Cell.Biome.SHRUBLAND, Cell.Biome.TUNDRA},
            {Cell.Biome.TROPICAL_SEASONAL_FOREST, Cell.Biome.TEMPERATE_DECIDUOUS_FOREST, Cell.Biome.SHRUBLAND,
                    Cell.Biome.SNOW},
            {Cell.Biome.TROPICAL_RAIN_FOREST, Cell.Biome.TEMPERATE_DECIDUOUS_FOREST, Cell.Biome.TAIGA, Cell.Biome.SNOW},
            {Cell.Biome.TROPICAL_RAIN_FOREST, Cell.Biome.TEMPERATE_RAIN_FOREST, Cell.Biome.TAIGA, Cell.Biome.SNOW}
    };

    String source;
    int tileSize;
    int horizontalTiles;
    int verticalTiles;
    int regionFrequency;
    boolean generateIsland;
    double waterPercentage;
    double landScale;
    int mountainRivers;
    double moistureReduction;

    public SettingsManager(){
    }

    public boolean load(){
        return load("settings.cfg");
    }

    public boolean load(String uri){
        List<String> data ;
        try {
            //TODO Seperate errors for missing file or malphormed file
            data = Files.readAllLines(Paths.get(uri));
            for(String datum : data){
                if(!datum.equals("") && !datum.contains("#")) {
                    switch(datum.substring(0, datum.indexOf('='))){
                        case("source"):
                            source = datum.substring(datum.indexOf('=') + 1);
                            break;
                        case("tile_size"):
                            tileSize = Integer.parseInt(datum.substring(datum.indexOf('=') + 1));
                            break;
                        case("horizontal_tiles"):
                            horizontalTiles = Integer.parseInt(datum.substring(datum.indexOf('=') + 1));
                            break;
                        case("vertical_tiles"):
                            verticalTiles = Integer.parseInt(datum.substring(datum.indexOf('=') + 1));
                            break;
                        case("region_frequency"):
                            regionFrequency = Integer.parseInt(datum.substring(datum.indexOf('=') + 1));
                            break;
                        case("generate_island"):
                            generateIsland = Boolean.parseBoolean(datum.substring(datum.indexOf('=') + 1));
                            break;
                        case("water_percentage"):
                            waterPercentage = Double.parseDouble(datum.substring(datum.indexOf('=') + 1));
                            break;
                        case("land_Step"):
                            landScale = Double.parseDouble(datum.substring(datum.indexOf('=') + 1));
                            break;
                        case("mountain_rivers"):
                            mountainRivers = Integer.parseInt(datum.substring(datum.indexOf('=') + 1));
                            break;
                        case("moisture_reduction"):
                            moistureReduction = Double.parseDouble(datum.substring(datum.indexOf('=') + 1));
                            break;
                    }
                }
            }
        }
        catch (Exception e){
            return false;
        }
        return true;
    }
}
