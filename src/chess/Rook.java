package chess;

public class Rook extends Piece {
    public Rook(boolean isWhite, int row, int col) {
        super(isWhite, row, col);
    }

    @Override
    public boolean isValidMove(int newRow, int newCol, Piece[][] board) {
        if (row == newRow || col == newCol) { // Moves in a straight line
            int rowStep = Integer.compare(newRow, row);
            int colStep = Integer.compare(newCol, col);
            int r = row + rowStep, c = col + colStep;
            while (r != newRow || c != newCol) {
                if (board[r][c] != null) return false;
                r += rowStep;
                c += colStep;
            }
            return board[newRow][newCol] == null || board[newRow][newCol].isWhite != isWhite;
        }
        return false;
    }
}

