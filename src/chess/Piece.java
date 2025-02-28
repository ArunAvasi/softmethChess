package chess;

public abstract class Piece {
    protected boolean isWhite;
    protected int row, col;

    public Piece(boolean isWhite, int row, int col) {
        this.isWhite = isWhite;
        this.row = row;
        this.col = col;
    }

    public boolean isWhite() {
        return isWhite;
    }

    public abstract boolean isValidMove(int newRow, int newCol, Piece[][] board);
}