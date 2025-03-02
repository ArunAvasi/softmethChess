package chess;

public class King extends Piece {

    /**
     * Constructs a King at the specified board location.
     * @param row     Row index (0-7); row 0 is rank 8.
     * @param col     Column index (0-7); col 0 is file 'a'.
     * @param isWhite True if the king is white; false if black.
     */
    public King(int row, int col, boolean isWhite) {
        super(row, col, isWhite);
    }

    /**
     * Validates a move for the king.
     * It allows:
     *  - Normal moves: one square in any direction.
     *  - Castling: a horizontal move of two squares on the same row, with proper conditions.
     *
     * Note: This method does not check for moves that put the king in check;
     * that validation is assumed to be handled elsewhere.
     *
     * @param toRow Destination row index (0-7).
     * @param toCol Destination column index (0-7).
     * @param board The current board represented as a 2D array of Piece objects.
     * @return True if the move is valid, false otherwise.
     */
    @Override
    public boolean isMoveValid(int toRow, int toCol, Piece[][] board) {
        int rowDiff = Math.abs(this.row - toRow);
        int colDiff = Math.abs(this.col - toCol);

        // Normal king move: move one square in any direction.
        if (rowDiff <= 1 && colDiff <= 1) {
            Piece dest = board[toRow][toCol];
            if (dest == null || dest.isWhite() != this.isWhite) {
                return true;
            }
            return false;
        }

        // Castling: king moves two squares horizontally on the same row.
        if (rowDiff == 0 && colDiff == 2) {
            // King must not have moved.
            if (this.hasMoved) {
                return false;
            }

            // Determine direction: kingside (toCol > current col) or queenside.
            if (toCol > this.col) {
                // Kingside castling:
                // Rook should be at the right-most square (column 7) on the same row.
                Piece rook = board[this.row][7];
                if (rook == null || !(rook instanceof Rook) || rook.hasMoved()) {
                    return false;
                }
                // Squares between king and rook must be empty.
                for (int c = this.col + 1; c < 7; c++) {
                    if (board[this.row][c] != null) {
                        return false;
                    }
                }
                // Additional check for king passing through check is assumed to be handled elsewhere.
                return true;
            } else {
                // Queenside castling:
                // Rook should be at the left-most square (column 0) on the same row.
                Piece rook = board[this.row][0];
                if (rook == null || !(rook instanceof Rook) || rook.hasMoved()) {
                    return false;
                }
                // Squares between king and rook must be empty.
                for (int c = 1; c < this.col; c++) {
                    if (board[this.row][c] != null) {
                        return false;
                    }
                }
                // Additional check for king passing through check is assumed to be handled elsewhere.
                return true;
            }
        }

        // Any other move is invalid for the king.
        return false;
    }

    /**
     * Returns a string representing this king's piece code.
     * For example, "WK" for a white king and "BK" for a black king.
     * @return The king's piece code.
     */
    @Override
    public String getPieceCode() {
        return isWhite ? "WK" : "BK";
    }
}