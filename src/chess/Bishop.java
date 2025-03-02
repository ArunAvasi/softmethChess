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
        // ✅ Ensure destination is within board bounds
        if (toRow < 0 || toRow >= 8 || toCol < 0 || toCol >= 8) {
            //System.out.println("Bishop.isMoveValid() Error: Destination out of bounds (" + toRow + ", " + toCol + ")");
            return false;
        }

        int rowDiff = toRow - row;
        int colDiff = toCol - col;

        // ✅ Must move diagonally
        if (Math.abs(rowDiff) != Math.abs(colDiff)) {
            return false;
        }

        // Determine movement direction
        int rowStep = (rowDiff > 0) ? 1 : -1;
        int colStep = (colDiff > 0) ? 1 : -1;

        // ✅ Check if the path between source and destination is clear
        int currentRow = row + rowStep;
        int currentCol = col + colStep;
        while (currentRow != toRow && currentCol != toCol) {
            // ✅ Ensure currentRow and currentCol are within bounds before accessing the board
            if (currentRow < 0 || currentRow >= 8 || currentCol < 0 || currentCol >= 8) {
                //System.out.println("Bishop.isMoveValid() Error: Moving out of bounds (" + currentRow + ", " + currentCol + ")");
                return false;
            }

            if (board[currentRow][currentCol] != null) {
                //System.out.println("Bishop.isMoveValid(): Path blocked at (" + currentRow + ", " + currentCol + ")");
                return false; // Path is blocked
            }
            currentRow += rowStep;
            currentCol += colStep;
        }

        // ✅ Ensure destination square is valid (empty or opponent piece)
        Piece destinationPiece = board[toRow][toCol];
        return (destinationPiece == null || destinationPiece.isWhite() != this.isWhite);
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