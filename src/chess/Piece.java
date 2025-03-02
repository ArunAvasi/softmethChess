package chess;

public abstract class Piece {
    // Row indices: 0 (top, rank 8) to 7 (bottom, rank 1)
    // Column indices: 0 corresponds to file 'a', 7 to file 'h'
    protected int row;
    protected int col;
    protected boolean isWhite;
    protected boolean hasMoved;

    /**
     * Constructor for a chess piece.
     * @param row     Row index (0-7), where 0 is rank 8.
     * @param col     Column index (0-7), where 0 is file 'a'.
     * @param isWhite True if the piece is white, false if black.
     */
    public Piece(int row, int col, boolean isWhite) {
        this.row = row;
        this.col = col;
        this.isWhite = isWhite;
        this.hasMoved = false;
    }

    /**
     * Gets the file (letter a-h) for this piece based on its column.
     * @return File letter.
     */
    public char getFile() {
        return (char) ('a' + col);
    }

    /**
     * Gets the rank (number 1-8) for this piece based on its row.
     * Row 0 corresponds to rank 8, row 7 to rank 1.
     * @return Rank number.
     */
    public int getRank() {
        return 8 - row;
    }

    /**
     * Returns true if this piece is white.
     * @return True if white, false if black.
     */
    public boolean isWhite() {
        return isWhite;
    }

    /**
     * Returns whether this piece has moved before.
     * @return True if it has moved, false otherwise.
     */
    public boolean hasMoved() {
        return hasMoved;
    }

    /**
     * Sets the flag indicating whether this piece has moved.
     * @param moved True if the piece has moved.
     */
    public void setHasMoved(boolean moved) {
        this.hasMoved = moved;
    }

    /**
     * Checks if moving this piece to the destination (toRow, toCol) is valid,
     * given the current board state.
     *
     * @param toRow Destination row index (0-7).
     * @param toCol Destination column index (0-7).
     * @param board The current board represented as a 2D array of Piece objects.
     * @return True if the move is valid; false otherwise.
     */
    public abstract boolean isMoveValid(int toRow, int toCol, Piece[][] board);

    /**
     * Moves the piece to the destination coordinates and marks it as having moved.
     * @param toRow Destination row index (0-7).
     * @param toCol Destination column index (0-7).
     */
    public void move(int toRow, int toCol) {
        this.row = toRow;
        this.col = toCol;
        this.hasMoved = true;
    }

    /**
     * Returns a string code representing this piece (e.g., "WP" for a white pawn,
     * "BN" for a black knight, etc.). This code will be used to map your internal
     * Piece objects to the provided ReturnPiece.PieceType.
     *
     * @return A string representing the piece code.
     */
    public abstract String getPieceCode();
}