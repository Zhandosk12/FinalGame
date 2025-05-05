package com.gdx.game.map;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.gdx.game.Enums.TILETYPE;
import com.gdx.game.Media;
import com.gdx.game.box2d.Box2dHelper;
import com.gdx.game.box2d.Box2dWorld;
import com.gdx.game.entities.Entity;
import com.gdx.game.entities.Tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;


public class Island {
    private Tile centreTile;

    private Chunk chunk;
    private final ArrayList<Entity> entities = new ArrayList<>();
    String[] aGrassLeft = {"001001001", "001001000", "000001001"};
    String[] aGrassRight = {"100100100","100100000","000100100"};
    String[] aGrassREnd = {"100000000"};
    String[] aGrassLEnd = {"001000000"};
    String[] aGrassTop = {"000000111", "000000011","000000110"};
    String[] aGrassTopRight = {"000000100"};
    String[] aGrassTopLeft = {"000000001"};

    public Island(Box2dWorld box2d) {
        setupTiles();
        codeTiles();
        generateHitboxes(box2d);
        generateTreeEntities(box2d);
    }

    public Tile getCentreTile() {
        return centreTile;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public ArrayList<Entity> getEntities() {
        return entities;
    }

    private void setupTiles() {
        chunk = new Chunk(33,33, 8);

        int currentRow = 0;
        int centreTileRow = chunk.getNumberRows() / 2;
        int centreTileCol = chunk.getNumberCols() /2;

        int rngW = MathUtils.random(5,8);
        int rngH = MathUtils.random(5,8);
        int maxRow = centreTileRow + rngH;
        int minRow = centreTileRow - rngH;
        int maxCol = centreTileCol + rngW;
        int minCol = centreTileCol - rngW;
        int firstTileRow = centreTileRow - (rngH);

        ArrayList<Tile> chunkRow = new ArrayList<>();
        HashMap<Integer, ArrayList<Tile>> map = new HashMap<>();

        for(int row = 0; row < chunk.getNumberRows(); row ++) {
            for(int col = 0; col < chunk.getNumberCols(); col ++) {
                Tile tile = new Tile(col, row, chunk.getTileSize(), TILETYPE.WATER, randomWater());

                createIsland(row, col, tile, minRow, maxRow, minCol, maxCol, firstTileRow);

                HashMap<Integer, ArrayList<Tile>> completeMap = addTilesToChunk(row, col, tile, currentRow, chunkRow, map);
                for ( Map.Entry<Integer, ArrayList<Tile>> entry : completeMap.entrySet()) {
                    currentRow = entry.getKey();
                    chunkRow = entry.getValue();
                }
            }
        }

        centreTile = chunk.getTile(centreTileRow, centreTileCol);
    }

    private void createIsland(int row, int col, Tile tile, int minRow, int maxRow, int minCol, int maxCol, int firstTileRow) {
        if(row > minRow && row < maxRow && col > minCol && col < maxCol) {
            tile.setTexture(randomGrass());
            tile.setTiletype(TILETYPE.GRASS);

            if(row == firstTileRow + 1) {
                tile.setTexture(Media.cliff);
                tile.setTiletype(TILETYPE.CLIFF);
            }
        }
    }

    private HashMap<Integer, ArrayList<Tile>> addTilesToChunk(int row, int col, Tile tile, int currentRow, ArrayList<Tile> chunkRow, HashMap<Integer, ArrayList<Tile>> map) {
        if(currentRow == row) {
            chunkRow.add(tile);
            if (row == chunk.getNumberRows() - 1 && col == chunk.getNumberCols() - 1) {
                chunk.getTiles().add(chunkRow);
            }
            map.put(currentRow, chunkRow);
        } else {
            currentRow = row;
            chunk.getTiles().add(chunkRow);
            chunkRow = new ArrayList<>();
            chunkRow.add(tile);

            map.put(currentRow, chunkRow);
        }
        return map;
    }

    private void updateImage(Tile tile) {
        if(Arrays.asList(aGrassLeft).contains(tile.getCode())) {
            tile.setSecondaryTexture(Media.grassLeft);
        } else if(Arrays.asList(aGrassRight).contains(tile.getCode())) {
            tile.setSecondaryTexture(Media.grassRight);
        } else if(Arrays.asList(aGrassREnd).contains(tile.getCode())) {
            tile.setSecondaryTexture(Media.grassLeftUpperEdge);
        } else if(Arrays.asList(aGrassLEnd).contains(tile.getCode())) {
            tile.setSecondaryTexture(Media.grassRightUpperEdge);
        } else if(Arrays.asList(aGrassTop).contains(tile.getCode())) {
            tile.setSecondaryTexture(Media.grassTop);
        } else if(Arrays.asList(aGrassTopRight).contains(tile.getCode())) {
            tile.setSecondaryTexture(Media.grassTopRight);
        } else if(Arrays.asList(aGrassTopLeft).contains(tile.getCode())) {
            tile.setSecondaryTexture(Media.grassTopLeft);
        }
    }

    private Texture randomGrass() {
        Texture grass;
        int tile = MathUtils.random(20);
        grass = switch (tile) {
            case 2 -> Media.grass02;
            case 3 -> Media.grass03;
            case 4 -> Media.grass04;
            default -> Media.grass01;
        };
        return grass;
    }

    private Texture randomWater() {
        Texture water;
        int tile = MathUtils.random(20);
        water = switch (tile) {
            case 2 -> Media.water02;
            case 3 -> Media.water03;
            case 4 -> Media.water04;
            default -> Media.water01;
        };
        return water;
    }

    private void codeTiles() {
        for(ArrayList<Tile> row : chunk.getTiles()) {
            for(Tile tile : row){
                int[] rows = {1,0,-1};
                int[] cols = {-1,0,1};

                for(int r: rows) {
                    for(int c: cols) {
                        tile.setCode(tile.getCode() + chunk.getTileCode(tile.getRow() + r, tile.getCol() + c));
                        updateImage(tile);
                    }
                }
            }
        }
    }

    private void generateTreeEntities(Box2dWorld box2d) {
        chunk.getTiles().forEach(r -> r.forEach(t -> addRandomTrees(box2d, t)));
    }

    private void addRandomTrees(Box2dWorld box2D, Tile tile) {
        Stream.of(tile)
                .filter(Tile::isGrass)
                .filter(t -> MathUtils.random(100) > 90)
                .forEach(t -> entities.add(new Tree(tile.getPos3(), box2D)));
    }

    private void generateHitboxes(Box2dWorld box2d) {
        chunk.getTiles().forEach(r -> r.forEach(t -> createTileBody(box2d, t)));
    }

    private void createTileBody(Box2dWorld box2d, Tile tile) {
        Stream.of(tile)
                .filter(t -> t.isNotPassable() && t.notIsAllWater())
                .forEach(t -> Box2dHelper.createBody(box2d.getWorld(), chunk.getTileSize(), chunk.getTileSize(), 0, 0, t.getPos3(), null, BodyDef.BodyType.StaticBody));
    }
}
