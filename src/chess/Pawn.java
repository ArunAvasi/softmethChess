package chess;

public class Pawn extends Piece {

    /**
     * Constructs a Pawn at the specified board location.
     * @param row Row index (0-7). For white pawns, row 6 is the starting row (rank 2);
     *            for black pawns, row 1 is the starting row (rank 7).
     * @param col Column index (0-7), where 0 corresponds to file 'a'.
     * @param isWhite True if the pawn is white, false if black.
     */
    public Pawn(int row, int col, boolean isWhite) {
        super(row, col, isWhite);
    }

    /**
     * Validates the pawn's move.
     * <ul>
     *   <li>Standard move: One square forward if the destination is empty.</li>
     *   <li>Initial move: Two squares forward if both the intermediate and destination squares are empty and the pawn is on its starting row.</li>
     *   <li>Capture: One square diagonally forward if the destination has an opponent’s piece.</li>
     * </ul>
     * Note: En passant and promotion are not handled here.
     *
     * @param toRow Destination row index (0-7).
     * @param toCol Destination column index (0-7).
     * @param board The current board represented as a 2D array of Piece objects.
     * @return True if the pawn's move is valid; false otherwise.
     */
    @Override
    public boolean isMoveValid(int toRow, int toCol, Piece[][] board) {
        int direction = isWhite ? -1 : 1; // White moves upward (row decreases), black moves downward (row increases)
        int startRow = isWhite ? 6 : 1;     // Starting row: white pawn row 6, black pawn row 1

        int rowDiff = toRow - this.row;
        int colDiff = Math.abs(toCol - this.col);

        // Standard one-square forward move (no capture)
        if (colDiff == 0 && rowDiff == direction) {
            if (board[toRow][toCol] == null) {
                return true;
            }
        }

        // Two-square forward move from starting position
        if (colDiff == 0 && rowDiff == 2 * direction && this.row == startRow) {
            int intermediateRow = this.row + direction;
            if (board[intermediateRow][toCol] == null && board[toRow][toCol] == null) {
                return true;
            }
        }

        // Diagonal capture move: one square diagonally forward if an opponent piece is present
        if (colDiff == 1 && rowDiff == direction) {
            Piece target = board[toRow][toCol];
            if (target != null && target.isWhite() != this.isWhite) {
                return true;
            }
        }

        // If none of the valid conditions apply, the move is invalid.
        return false;
    }

    /**
     * Returns a string representing the pawn's piece code.
     * For example, "WP" for a white pawn and "BP" for a black pawn.
     * @return The pawn's piece code.
     */
    @Override
    public String getPieceCode() {
        return isWhite ? "WP" : "BP";
    }
}