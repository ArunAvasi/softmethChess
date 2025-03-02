package chess;

public class Knight extends Piece {

    /**
     * Constructs a Knight at the specified board location.
     * @param row Row index (0-7).
     * @param col Column index (0-7), where 0 corresponds to file 'a'.
     * @param isWhite True if the knight is white, false if black.
     */
    public Knight(int row, int col, boolean isWhite) {
        super(row, col, isWhite);
    }

    /**
     * Validates the knight's move.
     * The knight moves in an L-shape: two squares in one direction and one square in the perpendicular direction.
     * It can jump over other pieces.
     *
     * @param toRow Destination row index (0-7).
     * @param toCol Destination column index (0-7).
     * @param board The current board represented as a 2D array of Piece objects.
     * @return True if the knight's move is valid; false otherwise.
     */
    @Override
    public boolean isMoveValid(int toRow, int toCol, Piece[][] board) {
        int rowDiff = Math.abs(this.row - toRow);
        int colDiff = Math.abs(this.col - toCol);

        // Knight moves: 2 squares in one direction and 1 square in the perpendicular direction.
        if ((rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2)) {
            Piece destination = board[toRow][toCol];
            // Destination must be either empty or occupied by an opponent's piece.
            return (destination == null || destination.isWhite() != this.isWhite);
        }
        return false;
    }

    /**
     * Returns a string representing the knight's piece code.
     * For example, "WN" for a white knight and "BN" for a black knight.
     * @return The knight's piece code.
     */
    @Override
    public String getPieceCode() {
        return isWhite ? "WN" : "BN";
    }
}