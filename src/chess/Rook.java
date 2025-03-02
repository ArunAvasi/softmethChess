package chess;

public class Rook extends Piece {

    /**
     * Constructs a Rook at the specified board location.
     * @param row Row index (0-7), where 0 corresponds to rank 8.
     * @param col Column index (0-7), where 0 corresponds to file 'a'.
     * @param isWhite True if the rook is white, false if black.
     */
    public Rook(int row, int col, boolean isWhite) {
        super(row, col, isWhite);
    }

    /**
     * Validates the rook's move.
     * The rook moves horizontally or vertically any number of squares.
     * All squares between the starting position and the destination must be empty.
     *
     * @param toRow Destination row index (0-7).
     * @param toCol Destination column index (0-7).
     * @param board The current board represented as a 2D array of Piece objects.
     * @return True if the rook's move is valid; false otherwise.
     */
    @Override
    public boolean isMoveValid(int toRow, int toCol, Piece[][] board) {
        // Rook must move in a straight line: either row stays the same or column stays the same.
        if (this.row != toRow && this.col != toCol) {
            return false;
        }

        // Moving horizontally:
        if (this.row == toRow) {
            int startCol = Math.min(this.col, toCol) + 1;
            int endCol = Math.max(this.col, toCol);
            for (int c = startCol; c < endCol; c++) {
                if (board[this.row][c] != null) {
                    return false; // Path is blocked.
                }
            }
        }

        // Moving vertically:
        if (this.col == toCol) {
            int startRow = Math.min(this.row, toRow) + 1;
            int endRow = Math.max(this.row, toRow);
            for (int r = startRow; r < endRow; r++) {
                if (board[r][this.col] != null) {
                    return false; // Path is blocked.
                }
            }
        }

        // Check destination square:
        // It must be empty or contain an opponent's piece.
        Piece destinationPiece = board[toRow][toCol];
        if (destinationPiece == null || destinationPiece.isWhite() != this.isWhite) {
            return true;
        }
        return false;
    }

    /**
     * Returns a string representing the rook's code.
     * For example, "WR" for a white rook and "BR" for a black rook.
     * @return The rook's piece code.
     */
    @Override
    public String getPieceCode() {
        return isWhite ? "WR" : "BR";
    }
}