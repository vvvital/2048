package com.javarush.task.task35.task3513;

import java.util.*;

public class Model {
    private Tile[][] gameTiles;
    Tile[] updateTiles;
    private static final int FIELD_WIDTH = 4;
    int maxTile = 2;
    int score = 0;
    private Model model;
    private View view;
    private Stack<Tile[][]> previousStates = new Stack<>();
    private Stack<Integer> previousScores = new Stack<>();
    private boolean isSaveNeeded = true;

    public void randomMove(){
        int rndMove=(int) ((Math.random()*100)%4);
        switch (rndMove){
            case (0):{
                left();
                break;
            }
            case (1):{
                right();
                break;
            }
            case (2):{
                up();
                break;
            }
            case (3):{
                down();
                break;
            }
        }
    }

    private void saveState(Tile[][]tiles) {
        Tile[][]tilesForSave=new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                tilesForSave[i][j]=new Tile(tiles[i][j].value);
            }
        }
        previousStates.push(tilesForSave);
        previousScores.push(score);
        isSaveNeeded = false;
    }

    public void rollback() {
        if (!previousStates.isEmpty() && !previousScores.isEmpty()) {
            gameTiles = (Tile[][]) previousStates.pop();
            score = (int) previousScores.pop();
        }
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    public int getScore() {
        return score;
    }

    public boolean canMove() {
        if (getEmptyTiles().size() > 0) {
            return true;
        } else {
            for (int i = 0; i < FIELD_WIDTH; i++) {
                for (int j = 0; j < FIELD_WIDTH; j++) {
                    for (int k = -1; k < 2; k++) {
                        if (i + k > 0 && i + k < FIELD_WIDTH && k != 0) {
                            if (gameTiles[i][j].value == gameTiles[i + k][j].value) {
                                return true;
                            }
                            if (j + k > 0 && j + k < FIELD_WIDTH && k != 0) {
                                if (gameTiles[i][j].value == gameTiles[i][j + k].value) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public Model() {
        resetGameTiles();
    }

    void resetGameTiles() {
        gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                gameTiles[i][j] = new Tile();
            }
        }
        addTile();
        addTile();
    }

    protected void left() {
        boolean isChanged = false;
        if (isSaveNeeded){
            saveState(gameTiles);
        }
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (compressTiles(gameTiles[i]) | mergeTiles(gameTiles[i])) {
                isChanged = true;
            }
        }
        if (isChanged) addTile();
        isSaveNeeded=true;
    }

    protected void right() {
        saveState(gameTiles);
        gameTiles = turnTiles(gameTiles);
        gameTiles = turnTiles(gameTiles);
        left();
        gameTiles = turnTiles(gameTiles);
        gameTiles = turnTiles(gameTiles);
    }

    protected void up() {
        saveState(gameTiles);
        gameTiles = turnTiles(gameTiles);
        gameTiles = turnTiles(gameTiles);
        gameTiles = turnTiles(gameTiles);
        left();
        gameTiles = turnTiles(gameTiles);
    }

    protected void down() {
        saveState(gameTiles);
        gameTiles = turnTiles(gameTiles);
        left();
        gameTiles = turnTiles(gameTiles);
        gameTiles = turnTiles(gameTiles);
        gameTiles = turnTiles(gameTiles);
    }

    protected Tile[][] turnTiles(Tile[][] tiles) {
        Tile[][] resultTile = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                resultTile[j][FIELD_WIDTH - 1 - i] = tiles[i][j];
            }
        }

        return resultTile;
    }

    private void addTile() {
        List<Tile> emptyTiles = getEmptyTiles();
        if (!emptyTiles.isEmpty()) {
            int index = (int) (Math.random() * emptyTiles.size()) % emptyTiles.size();
            Tile emptyTile = emptyTiles.get(index);
            emptyTile.value = Math.random() < 0.9 ? 2 : 4;
        }
    }

    private List<Tile> getEmptyTiles() {
        final List<Tile> list = new ArrayList<Tile>();
        for (Tile[] tileArray : gameTiles) {
            for (Tile t : tileArray)
                if (t.isEmpty()) {
                    list.add(t);
                }
        }
        return list;
    }

    private boolean compressTiles(Tile[] tiles) {
        int insertPosition = 0;
        boolean isChange = false;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (!tiles[i].isEmpty()) {
                if (i != insertPosition) {
                    tiles[insertPosition] = tiles[i];
                    tiles[i] = new Tile();
                    isChange = true;
                }
                insertPosition++;
            }
        }
        updateTiles = tiles;
        return isChange;

    }

    private boolean mergeTiles(Tile[] tiles) {
        LinkedList<Tile> tilesList = new LinkedList<>();
        boolean isChange = false;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (tiles[i].isEmpty()) {
                continue;
            }

            if (i < FIELD_WIDTH - 1 && tiles[i].value == tiles[i + 1].value) {
                int updatedValue = tiles[i].value * 2;
                if (updatedValue > maxTile) {
                    maxTile = updatedValue;
                }
                score += updatedValue;
                tilesList.addLast(new Tile(updatedValue));
                tiles[i + 1].value = 0;
                isChange = true;
            } else {
                tilesList.addLast(new Tile(tiles[i].value));
            }
            tiles[i].value = 0;
        }

        for (int i = 0; i < tilesList.size(); i++) {
            tiles[i] = tilesList.get(i);
        }
        updateTiles = tiles;
        return isChange;
    }

    private MoveEfficiency getMoveEfficiency(Move move) {
        MoveEfficiency moveEfficiency = new MoveEfficiency(-1, 0, move);
        move.move();
        if (hasBoardChanged()) {
            moveEfficiency = new MoveEfficiency(getEmptyTiles().size(), score, move);
        }
        rollback();
        return moveEfficiency;
    }

    private boolean hasBoardChanged() {
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                if (gameTiles[i][j].value != previousStates.peek()[i][j].value) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void autoMove(){
        PriorityQueue<MoveEfficiency>moveEfficiencies=new PriorityQueue<>(4,Collections.reverseOrder());

        moveEfficiencies.offer(getMoveEfficiency(this::left));
        moveEfficiencies.offer(getMoveEfficiency(this::right));
        moveEfficiencies.offer(getMoveEfficiency(this::up));
        moveEfficiencies.offer(getMoveEfficiency(this::down));

        assert moveEfficiencies.peek() != null;
        moveEfficiencies.peek().getMove().move();
    }
}
