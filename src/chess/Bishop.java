package chess;

public class Bishop extends Piece {

    /**
     * Constructs a Bishop at the specified board location.
     * @param row     Row index (0-7), where 0 is rank 8.
     * @param col     Column index (0-7), where 0 corresponds to file 'a'.
     * @param isWhite True if the bishop is white, false if black.
     */
    public Bishop(int row, int col, boolean isWhite) {
        super(row, col, isWhite);
    }

    /**
     * Validates the bishop's move.
     * The bishop moves diagonally so the absolute difference between the source
     * and destination rows must equal that of the columns.
     * It also ensures that the path is unobstructed.
     *
     * @param toRow Destination row index (0-7).
     * @param toCol Destination column index (0-7).
     * @param board The current board represented as a 2D array of Piece objects.
     * @return True if the bishop's move is valid; false otherwise.
     */
    @Override
    public boolean isMoveValid(int toRow, int toCol, Piece[][] board) {
        int rowDiff = toRow - row;
        int colDiff = toCol - col;

        // Must move diagonally
        if (Math.abs(rowDiff) != Math.abs(colDiff)) {
            return false;
        }

        // Determine the direction of movement
        int rowStep = (rowDiff > 0) ? 1 : -1;
        int colStep = (colDiff > 0) ? 1 : -1;

        // Check if the path between source and destination is clear
        int currentRow = row + rowStep;
        int currentCol = col + colStep;
        while (currentRow != toRow && currentCol != toCol) {
            if (board[currentRow][currentCol] != null) {
                return false; // Path is blocked
            }
            currentRow += rowStep;
            currentCol += colStep;
        }

        // Check the destination square:
        // It must be either empty or contain an opponent's piece.
        Piece destinationPiece = board[toRow][toCol];
        if (destinationPiece == null) {
            return true;
        }
        return destinationPiece.isWhite() != this.isWhite;
    }

    /**
     * Returns a string representing this bishop's code.
     * For example, "WB" for a white bishop and "BB" for a black bishop.
     * @return The bishop's piece code.
     */
    @Override
    public String getPieceCode() {
        return isWhite ? "WB" : "BB";
    }
}