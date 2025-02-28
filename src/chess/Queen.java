package chess;

public class Queen extends Piece {
    public Queen(boolean isWhite, int row, int col) {
        super(isWhite, row, col);
    }

    @Override
    public boolean isValidMove(int newRow, int newCol, Piece[][] board) {
        return new Rook(isWhite, row, col).isValidMove(newRow, newCol, board) ||
                new Bishop(isWhite, row, col).isValidMove(newRow, newCol, board);
    }
}
