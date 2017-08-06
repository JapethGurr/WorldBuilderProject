import processing.core.PApplet;
import processing.core.PImage;

import java.util.*;

public class ProcessingController extends PApplet{
    Random rand;
    private PImage sourceImage;
    private SettingsManager settings;
    private ArrayList<Region> allRegions;
    ArrayList<GeographicalRegion> allGeogRegions;
    private ArrayList<Cell> allCells;
    double minElevation;
    double maxElevation;
    double minMoisture;
    double maxMoisture;

    public static void main(String[] args){
        PApplet.main("ProcessingController");
    }

    public void settings(){
        rand = new Random(12345);
        //TODO Make size dynamic based on image, potentially scaling the image up if needs be
        settings = new SettingsManager();
        settings.load();
        size(settings.horizontalTiles * settings.tileSize,
                settings.verticalTiles * settings.tileSize);
    }

    public void setup(){
        noLoop();
        sourceImage = loadImage(settings.source);
        sourceImage.resize(width,height);
        sourceImage.loadPixels();
        prepareWorld();
        redraw();
    }

    public void draw(){
//        drawRegions();
//        drawElevation();
//        drawMoisture();
        drawBiomes();
        save(settings.source.substring(0, settings.source.indexOf('.')) + "World.jpg");
    }

    private void drawBiomes(){
        sourceImage.loadPixels();
        for(Cell cell : allCells){
            for(Pixel pixel : cell.area){
                switch (cell.biome){
                    case SEA:
                    case RIVER:
                    case LAKE:
                        sourceImage.pixels[pixel.y * width + pixel.x] = color(20, 90, 204);
                        break;
                    case BARE:
                        sourceImage.pixels[pixel.y * width + pixel.x] = color(150, 150, 150);
                        break;
                    case GRASSLAND:
                        sourceImage.pixels[pixel.y *  width + pixel.x] = color(231, 255, 164);
                        break;
                    case SCORCHED:
                        sourceImage.pixels[pixel.y * width + pixel.x] = color(66, 66, 66);
                        break;
                    case SHRUBLAND:
                        sourceImage.pixels[pixel.y * width + pixel.x] = color(198, 224, 186);
                        break;
                    case SNOW:
                        sourceImage.pixels[pixel.y * width + pixel.x] = color(248, 248, 248);
                        break;
                    case SUBTROPICAL_DESERT:
                    case BEACH:
                        sourceImage.pixels[pixel.y * width + pixel.x] = color(255, 230, 100);
                        break;
                    case TAIGA:
                        sourceImage.pixels[pixel.y * width + pixel.x] = color(210, 247, 200);
                        break;
                    case TEMPERATE_DECIDUOUS_FOREST:
                        sourceImage.pixels[pixel.y * width + pixel.x] = color(119, 153, 110);
                        break;
                    case TEMPERATE_DESERT:
                        sourceImage.pixels[pixel.y * width + pixel.x] = color(255, 252, 165);
                        break;
                    case TEMPERATE_RAIN_FOREST:
                        sourceImage.pixels[pixel.y * width + pixel.x] = color(199, 247, 66);
                        break;
                    case TROPICAL_RAIN_FOREST:
                        sourceImage.pixels[pixel.y * width + pixel.x] = color(176, 234, 14);
                        break;
                    case TROPICAL_SEASONAL_FOREST:
                        sourceImage.pixels[pixel.y * width + pixel.x] = color(71, 114, 59);
                        break;
                    case TUNDRA:
                        sourceImage.pixels[pixel.y * width + pixel.x] = color(184, 201, 169);
                        break;
                }
            }
        }
        image(sourceImage,0,0);
    }
    private void drawRegions(){
        sourceImage.loadPixels();
        for(Region region : allRegions){
            int regionColor = rand.nextInt();
            for(Cell cell : region.area){
                for(Pixel pixel : cell.area){
                    sourceImage.pixels[pixel.y * width + pixel.x] = color(regionColor);
                }
            }
        }
        image(sourceImage,0,0);
    }

    private void drawElevation(){
        sourceImage.loadPixels();
        colorMode(HSB, 360, (int)maxElevation, 100);
        for(Cell cell : allCells){
            for(Pixel pixel : cell.area){
                if(cell.biome == Cell.Biome.RIVER){
                    sourceImage.pixels[pixel.y * width + pixel.x] = color(185, 141, 70);
                }
                else {
                    switch (cell.parentRegion.geographicalRegion.type) {
                        case CONTINENT:
                            sourceImage.pixels[pixel.y * width + pixel.x] =
                                    color(125, (int) maxElevation - (int) cell.elevation, 70);
                            break;
                        case ISLAND:
                            sourceImage.pixels[pixel.y * width + pixel.x] =
                                    color(56, (int) maxElevation - (int) cell.elevation, 70);
                            break;
                        case SEA:
                            sourceImage.pixels[pixel.y * width + pixel.x] = color(220, 60, 70);
                            break;
                        case LAKE:
                            sourceImage.pixels[pixel.y * width + pixel.x] = color(185, 141, 70);
                            break;
                    }
                }
            }
        }
        image(sourceImage,0,0);
    }

    private void drawMoisture(){
        sourceImage.loadPixels();
        colorMode(HSB, 360, (int)maxMoisture, 100);
        for(Cell cell : allCells){
            for(Pixel pixel : cell.area){
                if(cell.biome == Cell.Biome.RIVER){
                    sourceImage.pixels[pixel.y * width + pixel.x] = color(185, 141, 70);
                }
                else {
                    switch (cell.parentRegion.geographicalRegion.type) {
                        case CONTINENT:
                            sourceImage.pixels[pixel.y * width + pixel.x] =
                                    color(125, (int) maxMoisture - (int) cell.moisture, 70);
                            break;
                        case ISLAND:
                            sourceImage.pixels[pixel.y * width + pixel.x] =
                                    color(56, (int) maxMoisture - (int) cell.moisture, 70);
                            break;
                        case SEA:
                            sourceImage.pixels[pixel.y * width + pixel.x] = color(220, 60, 70);
                            break;
                        case LAKE:
                            sourceImage.pixels[pixel.y * width + pixel.x] = color(185, 141, 70);
                            break;
                    }
                }
            }
        }
        image(sourceImage,0,0);
    }

    public void prepareWorld(){
        generateCells();
        generateRegions();
        generateLandmasses();
        classifyGeography();
        calculateElevation();
        calculateSeaMoisture();
        calculateRivers();
        calculateRiverMoisture();
        calculateBiomes();
    }

    public void generateCells() {
        // Generate all cells with information
        allCells = new ArrayList<>();
        for (int h = 0; h < height; h += settings.tileSize) {
            for (int w = 0; w < width; w += settings.tileSize) {
                Cell cell = new Cell();
                int avgRed = 0;
                int avgBlue = 0;
                int avgGreen = 0;
                for (int y = h; y < h + settings.tileSize; y++) {
                    for (int x = w; x < w + settings.tileSize; x++) {
                        cell.area.add(new Pixel(x, y));
                        avgRed += red(sourceImage.pixels[y * width + x]);
                        avgBlue += blue(sourceImage.pixels[y * width + x]);
                        avgGreen += green(sourceImage.pixels[y * width + x]);
                    }
                }
                int pixels = cell.area.size();
                cell.averageColor = color(avgRed/pixels,avgGreen/pixels,avgBlue/pixels);
                allCells.add(cell);
            }
        }
        // Assign cells their neighbours
        for (int i = 0; i < allCells.size(); i++) {
            Cell cell = allCells.get(i);
            if (i - settings.horizontalTiles >= 0) { // Up
                cell.neighbours.add(allCells.get(i - settings.horizontalTiles));
            }
            if (i % settings.horizontalTiles != 0 && i != 0) { // Left
                cell.neighbours.add(allCells.get(i - 1));
            }
            if ((i + 1) % settings.horizontalTiles != 0 && (i+1) != allCells.size()) { // Right
                cell.neighbours.add(allCells.get(i + 1));
            }
            if (i + settings.horizontalTiles < settings.verticalTiles * settings.horizontalTiles) { // Down
                cell.neighbours.add(allCells.get(i + settings.horizontalTiles));
            }
            if(cell.neighbours.size() < 4){
                cell.border = true;
            }
        }
    }

    public void generateRegions(){
        // Divide cells into only a handful of colors
        PImage cellToRegionMap = new PImage(allCells.size(),1);
        for(int i = 0; i < allCells.size(); i++){
            cellToRegionMap.pixels[i] = allCells.get(i).averageColor;
        }
        cellToRegionMap.filter(POSTERIZE,settings.regionFrequency);
        for(int i = 0; i < allCells.size(); i++){
            allCells.get(i).averageColor = cellToRegionMap.pixels[i];
        }
        // Generate regions based on groups of adjacent cells that share a mapped color
        allRegions = new ArrayList<>();
        Stack<Cell> toProcess = new Stack<>();
        for(Cell cell : allCells){
            if(cell.parentRegion == null){
                Region region = new Region();
                region.area.add(cell);
                cell.parentRegion = region;
                if(cell.border){
                    cell.parentRegion.border = true;
                }
                allRegions.add(region);
                toProcess.add(cell);
                while(!toProcess.isEmpty()){
                    Cell next = toProcess.pop();
                    for(Cell neighbour : next.neighbours){
                        if(!next.parentRegion.area.contains(neighbour) && neighbour.averageColor == next.averageColor){
                            next.parentRegion.area.add(neighbour);
                            neighbour.parentRegion = next.parentRegion;
                            if(neighbour.border){
                                neighbour.parentRegion.border = true;
                            }
                            toProcess.add(neighbour);
                        }
                    }
                }
            }
        }
        // Assign Regions their neighbours
        for(Region region : allRegions){
            for(Cell cell : region.area){
                for(Cell neighbour : cell.neighbours){
                    if(neighbour.parentRegion != region && !region.neighbours.contains(neighbour.parentRegion)){
                        region.neighbours.add(neighbour.parentRegion);
                    }
                }
            }
        }
        Collections.sort(allRegions, Comparator.comparingInt(o -> o.area.size()));
        Collections.reverse(allRegions);
    }

    public void generateLandmasses(){
        boolean generateIsland = settings.generateIsland;
        double desiredWaterRatio = settings.waterPercentage / 100.0;
        if(generateIsland){
            for(Cell cell : allCells){
                if(cell.border){
                    cell.parentRegion.land = false;
                }
            }
        }
        int targetSeaArea = (int)Math.round(desiredWaterRatio * settings.horizontalTiles * settings.verticalTiles);
        int seaArea = 0;
        for(Region region : allRegions){
            if(!region.land){seaArea += region.area.size();}
        }
        for(int i = 0; i < allRegions.size() && seaArea < targetSeaArea; i++){
            if(allRegions.get(i).land && allRegions.get(i).area.size() <= targetSeaArea - seaArea){
                seaArea += allRegions.get(i).area.size();
                allRegions.get(i).land = false;
            }
        }
    }

    public void classifyGeography(){
        // Generate geographical regions based on shared land/sea adjacency
        allGeogRegions = new ArrayList<>();
        Stack<Region> toProcess = new Stack();
        for(Region region : allRegions){
            if(region.geographicalRegion == null){
                GeographicalRegion geogRegion = new GeographicalRegion();
                geogRegion.area.add(region);
                region.geographicalRegion = geogRegion;
                if(region.border){
                    region.geographicalRegion.border = true;
                }
                allGeogRegions.add(geogRegion);
                toProcess.add(region);
                while(!toProcess.isEmpty()){
                    Region next = toProcess.pop();
                    for(Region neighbour : next.neighbours){
                        if(!next.geographicalRegion.area.contains(neighbour) && neighbour.land == next.land){
                            next.geographicalRegion.area.add(neighbour);
                            neighbour.geographicalRegion = next.geographicalRegion;
                            if(neighbour.border){
                                neighbour.geographicalRegion.border = true;
                            }
                            toProcess.add(neighbour);
                        }
                    }
                }
            }
        }
        // Assign geographical regions their neighbours
        for(GeographicalRegion geogRegion : allGeogRegions){
            for(Region region : geogRegion.area){
                for(Region neighbour : region.neighbours){
                    if(neighbour.geographicalRegion != region.geographicalRegion &&
                            !geogRegion.neighbours.contains(neighbour.geographicalRegion)){
                        geogRegion.neighbours.add(neighbour.geographicalRegion);
                    }
                }
            }
        }
        // Classify geographical regions
        for(GeographicalRegion geogRegion : allGeogRegions) {
            if(geogRegion.border){
                geogRegion.type = geogRegion.area.get(0).land ?
                        GeographicalRegion.GeographyType.CONTINENT : GeographicalRegion.GeographyType.SEA;
            }
            else{
                geogRegion.type = geogRegion.area.get(0).land ?
                        GeographicalRegion.GeographyType.ISLAND : GeographicalRegion.GeographyType.LAKE;
            }
        }
        Collections.sort(allGeogRegions, Comparator.comparingDouble(o -> o.area.size()));
        Collections.reverse(allGeogRegions);
    }

    public void calculateElevation(){
        minElevation = 0;
        maxElevation = 0;
        double currentElevation = 1;
        // Calculate Land heights
        ArrayList<Cell> processed = new ArrayList<>();
        Stack<Cell> toProcess = new Stack<>();
        for(Cell cell : allCells){
            if(!cell.parentRegion.land){
                processed.add(cell);
                cell.elevation = 0;
                if(cell.parentRegion.geographicalRegion.type == GeographicalRegion.GeographyType.SEA){
                    cell.biome = Cell.Biome.SEA;
                    for(Cell neighbour : cell.neighbours){
                        if(neighbour.parentRegion.land && !toProcess.contains(neighbour)){
                            toProcess.add(neighbour);
                        }
                    }
                }
            }
        }
        while(!toProcess.isEmpty() && processed.size() < allCells.size()){
            Stack<Cell> nextSet = new Stack<>();
            while(!toProcess.isEmpty()){
                Cell cell = toProcess.pop();
                cell.elevation = currentElevation;
                for(Cell neighbour : cell.neighbours){
                    if(!processed.contains(neighbour) && !toProcess.contains(neighbour) &&
                            !nextSet.contains(neighbour)){
                        nextSet.add(neighbour);
                    }
                }
                processed.add(cell);
            }
            if(currentElevation > maxElevation){
                maxElevation = currentElevation;
            }
            if (currentElevation < minElevation) {
                minElevation = currentElevation;
            }
            currentElevation += settings.landScale;
            toProcess = nextSet;
        }
    }

    public void calculateSeaMoisture(){
        minMoisture = 0;
        maxMoisture = Math.sqrt(allGeogRegions.get(0).area.size());
        double currentMoisture = maxMoisture;
        double moistureStep = settings.moistureReduction * maxMoisture;
        // Calculate Land heights
        ArrayList<Cell> processed = new ArrayList<>();
        Stack<Cell> toProcess = new Stack<>();
        for(Cell cell : allCells){
            if(!cell.parentRegion.land){
                processed.add(cell);
                if(cell.parentRegion.geographicalRegion.type == GeographicalRegion.GeographyType.SEA){
                    cell.biome = Cell.Biome.SEA;
                    for(Cell neighbour : cell.neighbours){
                        if(neighbour.parentRegion.land && !toProcess.contains(neighbour)){
                            toProcess.add(neighbour);
                        }
                    }
                }
            }
        }
        while(!toProcess.isEmpty() && processed.size() < allCells.size()){
            Stack<Cell> nextSet = new Stack<>();
            while(!toProcess.isEmpty()){
                Cell cell = toProcess.pop();
                cell.moisture = currentMoisture;
                for(Cell neighbour : cell.neighbours){
                    if(!processed.contains(neighbour) && !toProcess.contains(neighbour) &&
                            !nextSet.contains(neighbour)){
                        nextSet.add(neighbour);
                    }
                }
                processed.add(cell);
            }
            if(currentMoisture > maxMoisture){
                maxMoisture = currentMoisture;
            }
            if (currentMoisture < minMoisture) {
                minMoisture = currentMoisture;
            }
            if(currentMoisture > 0){
                currentMoisture = currentMoisture -4 * moistureStep > 0 ? currentMoisture - 4 * moistureStep : 0;
            }
            toProcess = nextSet;
        }
    }

    public void calculateRivers(){
        // Lake rivers first
        for(GeographicalRegion geogRegion : allGeogRegions){
            if(geogRegion.type == GeographicalRegion.GeographyType.LAKE){
                // Shift lakes to their lowest neighbouring cell height
                Cell riverMouth = null;
                double lowestEdgeElevation = Double.MAX_VALUE;
                for(Region region : geogRegion.area){
                    for(Cell cell : region.area){
                        for(Cell neighbour : cell.neighbours) {
                            if (neighbour.parentRegion.geographicalRegion.type !=
                                    GeographicalRegion.GeographyType.LAKE  &&
                                    neighbour.parentRegion.geographicalRegion.type !=
                                            GeographicalRegion.GeographyType.ISLAND &&
                                    neighbour.elevation < lowestEdgeElevation) {
                                lowestEdgeElevation = neighbour.elevation;
                                riverMouth = neighbour;
                            }
                        }
                    }
                }
                for(Region region : geogRegion.area){
                    for(Cell cell : region.area){
                        cell.biome = Cell.Biome.LAKE;
                        cell.elevation = lowestEdgeElevation;
                    }
                }
                // Plot river down
                Cell riverPoint = plotRiver(riverMouth);
                while(riverPoint != null){
                    riverPoint = plotRiver(riverPoint);
                }
            }
        }
        // Now Mountain Rivers
        ArrayList<Cell> cellsByElevation = new ArrayList<>();
        cellsByElevation.addAll(allCells);
        Collections.sort(cellsByElevation, Comparator.comparingDouble(o -> o.elevation));
        Collections.reverse(cellsByElevation);
        int topFivePercent = (int)(0.05 * cellsByElevation.size());
        int remainingRivers = settings.mountainRivers;
        int riverSpacing = topFivePercent / remainingRivers;
        while(remainingRivers > 0){
            // Plot river down
            Cell riverPoint = cellsByElevation.get(rand.nextInt(topFivePercent));
//            Cell riverPoint = cellsByElevation.get(riverSpacing * remainingRivers);
            while(riverPoint != null){
                riverPoint = plotRiver(riverPoint);
            }
            remainingRivers--;
        }
    }

    private Cell plotRiver(Cell riverPoint){
        if(riverPoint.biome != Cell.Biome.LAKE && riverPoint.biome != Cell.Biome.SEA) {
            riverPoint.biome = Cell.Biome.RIVER;
            Collections.sort(riverPoint.neighbours, Comparator.comparingDouble(o -> o.elevation));
            boolean meander = rand.nextBoolean();
            if(meander) {
                return riverPoint.neighbours.get(rand.nextInt(3));
            }
            else{
                return riverPoint.neighbours.get(0);
            }
        }
        else{
            return null;
        }
    }

    public void calculateRiverMoisture(){
        minMoisture = 0;
        maxMoisture = Math.sqrt(allGeogRegions.get(0).area.size());
        double currentMoisture = maxMoisture;
        double moistureStep = settings.moistureReduction * maxMoisture;
        // Calculate Land Moisture
        ArrayList<Cell> processed = new ArrayList<>();
        Stack<Cell> toProcess = new Stack<>();
        for(Cell cell : allCells){
            if(!cell.parentRegion.land || cell.biome == Cell.Biome.RIVER){
                processed.add(cell);
                cell.moisture = currentMoisture;
                for (Cell neighbour : cell.neighbours) {
                    if(cell.parentRegion.geographicalRegion.type == GeographicalRegion.GeographyType.LAKE ||
                            cell.biome == Cell.Biome.RIVER) {
                        if ((neighbour.parentRegion.land && neighbour.biome != Cell.Biome.RIVER) &&
                                !toProcess.contains(neighbour)) {
                            toProcess.add(neighbour);
                        }
                    }
                }
            }
        }
        currentMoisture--;
        while(!toProcess.isEmpty() && processed.size() < allCells.size()){
            Stack<Cell> nextSet = new Stack<>();
            while(!toProcess.isEmpty()){
                Cell cell = toProcess.pop();
                // Ideally would combine them but neede for current biome calcs
                cell.moisture = Math.max(currentMoisture, cell.moisture);
                for(Cell neighbour : cell.neighbours){
                    if(!processed.contains(neighbour) && !toProcess.contains(neighbour) &&
                            !nextSet.contains(neighbour)){
                        nextSet.add(neighbour);
                    }
                }
                processed.add(cell);
            }
            if(currentMoisture > maxMoisture){
                maxMoisture = currentMoisture;
            }
            if (currentMoisture < minMoisture) {
                minMoisture = currentMoisture;
            }
            if(currentMoisture > 0){
                currentMoisture = currentMoisture - moistureStep > 0 ? currentMoisture - moistureStep : 0;
            }
            toProcess = nextSet;
        }
    }

    public void calculateBiomes(){
        double moistureZoneSize = maxMoisture / 6;
        double elevationZoneSize = maxElevation/ 4;
        for(Cell cell : allCells){
            if(cell.biome == null){
                boolean beach = false;
                for(Cell neighbour : cell.neighbours){
                    if(neighbour.biome == Cell.Biome.SEA){
                        beach = true;
                        cell.biome = Cell.Biome.BEACH;
                        break;
                    }
                }
                if(!beach) {
                    int moistureZone = Math.min(5, (int)Math.floor(cell.moisture / moistureZoneSize));
                    int elevationZone =  Math.min(3, (int)Math.floor(cell.elevation / elevationZoneSize));
                    cell.biome = settings.biomeMap[moistureZone][elevationZone];
                }
            }
        }
    }
}