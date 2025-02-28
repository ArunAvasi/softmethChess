package chess;

public class Pawn extends Piece {
    public Pawn(boolean isWhite, int row, int col) {
        super(isWhite, row, col);
    }

    @Override
    public boolean isValidMove(int newRow, int newCol, Piece[][] board) {
        int direction = isWhite ? -1 : 1; // White moves up, Black moves down
        if (newCol == col && board[newRow][newCol] == null) {
            if (newRow == row + direction) return true; // Regular move
            if ((isWhite && row == 6 || !isWhite && row == 1) && newRow == row + 2 * direction
                    && board[row + direction][col] == null) return true; // First-move double step
        } else if (Math.abs(newCol - col) == 1 && newRow == row + direction
                && board[newRow][newCol] != null && board[newRow][newCol].isWhite != isWhite) {
            return true; // Capturing diagonally
        }
        return false;
    }
}

