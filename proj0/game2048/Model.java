package game2048;

import java.util.Formatter;
import java.util.Observable;


/** The state of a game of 2048.
 *  @author PM25OO
 */
public class Model extends Observable {
    /** Current contents of the board. */
    private final Board board;
    /** Current score. */
    private int score;
    /** Maximum score so far.  Updated when game ends. */
    private int maxScore;
    /** True iff game is ended. */
    private boolean gameOver;

    /* Coordinate System: column C, row R of the board (where row 0,
     * column 0 is the lower-left corner of the board) will correspond
     * to board.tile(c, r).  Be careful! It works like (x, y) coordinates.
     */

    /** Largest piece value. */
    public static final int MAX_PIECE = 2048;

    /** A new 2048 game on a board of size SIZE with no pieces
     *  and score 0. */
    public Model(int size) {
        board = new Board(size);
        score = maxScore = 0;
        gameOver = false;
    }

    /** A new 2048 game where RAWVALUES contain the values of the tiles
     * (0 if null). VALUES is indexed by (row, col) with (0, 0) corresponding
     * to the bottom-left corner. Used for testing purposes. */
    public Model(int[][] rawValues, int score, int maxScore, boolean gameOver) {
        int size = rawValues.length;
        board = new Board(rawValues, score);
        this.score = score;
        this.maxScore = maxScore;
        this.gameOver = gameOver;
    }

    /** Return the current Tile at (COL, ROW), where 0 <= ROW < size(),
     *  0 <= COL < size(). Returns null if there is no tile there.
     *  Used for testing. Should be deprecated and removed.
     *  */
    public Tile tile(int col, int row) {
        return board.tile(col, row);
    }

    /** Return the number of squares on one side of the board.
     *  Used for testing. Should be deprecated and removed. */
    public int size() {
        return board.size();
    }

    /** Return true iff the game is over (there are no moves, or
     *  there is a tile with value 2048 on the board). */
    public boolean gameOver() {
        checkGameOver();
        if (gameOver) {
            maxScore = Math.max(score, maxScore);
        }
        return gameOver;
    }

    /** Return the current score. */
    public int score() {
        return score;
    }

    /** Return the current maximum game score (updated at end of game). */
    public int maxScore() {
        return maxScore;
    }

    /** Clear the board to empty and reset the score. */
    public void clear() {
        score = 0;
        gameOver = false;
        board.clear();
        setChanged();
    }

    /** Add TILE to the board. There must be no Tile currently at the
     *  same position. */
    public void addTile(Tile tile) {
        board.addTile(tile);
        checkGameOver();
        setChanged();
    }

    /** Tilt the board toward SIDE. Return true iff this changes the board.
     *
     * 1. If two Tile objects are adjacent in the direction of motion and have
     *    the same value, they are merged into one Tile of twice the original
     *    value and that new value is added to the score instance variable
     * 2. A tile that is the result of a merge will not merge again on that
     *    tilt. So each move, every tile will only ever be part of at most one
     *    merge (perhaps zero).
     * 3. When three adjacent tiles in the direction of motion have the same
     *    value, then the leading two tiles in the direction of motion merge,
     *    and the trailing tile does not.
     * */
    public boolean tilt(Side side) {

        if (side == Side.EAST) board.setViewingPerspective(Side.EAST);
        if (side == Side.WEST) board.setViewingPerspective(Side.WEST);
        if (side == Side.SOUTH) board.setViewingPerspective(Side.SOUTH);
        boolean changed;
        changed = false;

        // TODO: Modify this.board (and perhaps this.score) to account
        // for the tilt to the Side SIDE. If the board changed, set the
        // changed local variable to true.

        int size = this.board.size();

        for (int col = 0; col < size; col++) {
            for (int row = size - 1; row >= 0; row--) {
                if (board.tile(col, row) == null) { // 检测到空格时
                    for(int i = row - 1; i >= 0; i--) { // 向下查找遇到的第一片Tile
                        if(board.tile(col, i) != null) {
                            if(row + 1 < size) { // 检查能否向上合并
                                if(board.tile(col, row + 1).value() == board.tile(col, i).value()) {
                                    int mergedValue = board.tile(col, i).value() * 2;
                                    this.score += mergedValue;
                                    mergeTile(col, row, board.tile(col, i));
                                }
                                else {
                                    board.move(col, row, board.tile(col, i));
                                }
                            }
                            else {
                                board.move(col, row, board.tile(col, i));
                            }
                            changed = true;
                            break;
                        }
                    }
                } else {
                    if(row + 1 < size) {
                        if(board.tile(col, row + 1).value() == board.tile(col, row).value()) {
                            int mergedValue = board.tile(col, row).value() * 2;
                            this.score += mergedValue;
                            changed = true;
                            mergeTile(col, row, board.tile(col, row));
                        }
                    }
                }
            }
        }

        checkGameOver();
        if (changed) {
            setChanged();
        }
        board.setViewingPerspective(Side.NORTH);
        return changed;
    }

    private void mergeTile(int col, int row, Tile tile) {
        board.move(col, row + 1, tile);
        for(int j = row - 1; j >= 0; j--) { // 再次向下查找遇到的第一片Tile
            if(board.tile(col, j) != null) {
                board.move(col, row, board.tile(col, j));
                break;
            }
        }
    }

    /** Checks if the game is over and sets the gameOver variable
     *  appropriately.
     */
    private void checkGameOver() {
        gameOver = checkGameOver(board);
    }

    /** Determine whether game is over. */
    private static boolean checkGameOver(Board b) {
        return maxTileExists(b) || !atLeastOneMoveExists(b);
    }

    /** Returns true if at least one space on the Board is empty.
     *  Empty spaces are stored as null.
     * */
    public static boolean emptySpaceExists(Board b) {
        // TODO: Fill in this function.
        int size = b.size();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (b.tile(i, j) == null){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if any tile is equal to the maximum valid value.
     * Maximum valid value is given by MAX_PIECE. Note that
     * given a Tile object t, we get its value with t.value().
     */
    public static boolean maxTileExists(Board b) {
        // TODO: Fill in this function.
        int size = b.size();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (b.tile(i, j) != null && b.tile(i, j).value() == MAX_PIECE) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if there are any valid moves on the board.
     * There are two ways that there can be valid moves:
     * 1. There is at least one empty space on the board.
     * 2. There are two adjacent tiles with the same value.
     */
    public static boolean atLeastOneMoveExists(Board b) {
        // TODO: Fill in this function.
        class isSameWithBeside {
            public boolean hasSame(Board board, Tile tile) {
                int size = b.size();
                int value = tile.value();
                if (tile.row() - 1 >= 0 && value == board.tile(tile.col(), tile.row() - 1).value()) {
                    return true;
                } else if (tile.row() + 1 < size && value == board.tile(tile.col(), tile.row() + 1).value()) {
                    return true;
                } else if (tile.col() - 1 >= 0 && value == board.tile(tile.col() - 1, tile.row()).value()) {
                    return true;
                } else return tile.col() + 1 < size && value == board.tile(tile.col() + 1, tile.row()).value();
            }
        }
        if (emptySpaceExists(b)) {
            return true;
        } else {
            int size = b.size();
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    isSameWithBeside judge = new isSameWithBeside();
                    if (judge.hasSame(b, b.tile(i, j))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    @Override
     /** Returns the model as a string, used for debugging. */
    public String toString() {
        Formatter out = new Formatter();
        out.format("%n[%n");
        for (int row = size() - 1; row >= 0; row -= 1) {
            for (int col = 0; col < size(); col += 1) {
                if (tile(col, row) == null) {
                    out.format("|    ");
                } else {
                    out.format("|%4d", tile(col, row).value());
                }
            }
            out.format("|%n");
        }
        String over = gameOver() ? "over" : "not over";
        out.format("] %d (max: %d) (game is %s) %n", score(), maxScore(), over);
        return out.toString();
    }

    @Override
    /** Returns whether two models are equal. */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (getClass() != o.getClass()) {
            return false;
        } else {
            return toString().equals(o.toString());
        }
    }

    @Override
    /** Returns hash code of Model’s string. */
    public int hashCode() {
        return toString().hashCode();
    }
}
