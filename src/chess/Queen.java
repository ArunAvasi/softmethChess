package chess;

public class Queen extends Piece {

    /**
     * Constructs a Queen at the specified board location.
     * @param row Row index (0-7); row 0 corresponds to rank 8.
     * @param col Column index (0-7); col 0 corresponds to file 'a'.
     * @param isWhite True if the queen is white, false if black.
     */
    public Queen(int row, int col, boolean isWhite) {
        super(row, col, isWhite);
    }

    /**
     * Validates the queen's move.
     * The queen can move horizontally, vertically, or diagonally.
     * This method checks that the move is along one of those directions and that
     * the path between the source and destination is clear.
     *
     * @param toRow Destination row index (0-7).
     * @param toCol Destination column index (0-7).
     * @param board The current board represented as a 2D array of Piece objects.
     * @return True if the queen's move is valid; false otherwise.
     */
    @Override
    public boolean isMoveValid(int toRow, int toCol, Piece[][] board) {
        int rowDiff = Math.abs(this.row - toRow);
        int colDiff = Math.abs(this.col - toCol);

        // Check for diagonal move
        if (rowDiff == colDiff && rowDiff != 0) {
            int rowStep = (toRow - this.row) > 0 ? 1 : -1;
            int colStep = (toCol - this.col) > 0 ? 1 : -1;
            int currentRow = this.row + rowStep;
            int currentCol = this.col + colStep;
            while (currentRow != toRow && currentCol != toCol) {
                if (board[currentRow][currentCol] != null) {
                    return false; // Path is blocked.
                }
                currentRow += rowStep;
                currentCol += colStep;
            }
        }
        // Check for horizontal move
        else if (this.row == toRow && this.col != toCol) {
            int start = Math.min(this.col, toCol) + 1;
            int end = Math.max(this.col, toCol);
            for (int c = start; c < end; c++) {
                if (board[this.row][c] != null) {
                    return false; // Path is blocked.
                }
            }
        }
        // Check for vertical move
        else if (this.col == toCol && this.row != toRow) {
            int start = Math.min(this.row, toRow) + 1;
            int end = Math.max(this.row, toRow);
            for (int r = start; r < end; r++) {
                if (board[r][this.col] != null) {
                    return false; // Path is blocked.
                }
            }
        }
        // Not a valid queen move if not diagonal, horizontal, or vertical.
        else {
            return false;
        }

        // Check the destination square:
        // It must be empty or occupied by an opponent's piece.
        Piece destinationPiece = board[toRow][toCol];
        return (destinationPiece == null) || (destinationPiece.isWhite() != this.isWhite);
    }

    /**
     * Returns a string representing this queen's piece code.
     * For example, "WQ" for a white queen and "BQ" for a black queen.
     * @return The queen's piece code.
     */
    @Override
    public String getPieceCode() {
        return isWhite ? "WQ" : "BQ";
    }
}