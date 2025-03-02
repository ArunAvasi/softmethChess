package chess;

abstract class Piece {
    protected ReturnPiece.PieceFile pieceFile;
    protected int pieceRank;  // 1 through 8
    protected boolean isWhite;
    protected String pieceType;

    public Piece(ReturnPiece.PieceFile pieceFile, int pieceRank, boolean isWhite, String pieceType) {
        this.pieceFile = pieceFile;
        this.pieceRank = pieceRank;
        this.isWhite = isWhite;
        this.pieceType = pieceType;
    }

    public abstract boolean isValidMove(ReturnPiece.PieceFile destFile, int destRank, Piece[][] board);

    public ReturnPiece getReturnPiece() {
        ReturnPiece rp = new ReturnPiece();
        rp.pieceFile = this.pieceFile;
        rp.pieceRank = this.pieceRank;

        // Set piece type correctly using the ReturnPiece.PieceType enum
        String prefix = isWhite ? "W" : "B";
        rp.pieceType = ReturnPiece.PieceType.valueOf(prefix + pieceType);

        return rp;
    }

    // Getters and setters
    public ReturnPiece.PieceFile getPieceFile() { return pieceFile; }
    public int getPieceRank() { return pieceRank; }
    public boolean isWhite() { return isWhite; }

    public void move(ReturnPiece.PieceFile destFile, int destRank) {
        this.pieceFile = destFile;
        this.pieceRank = destRank;
    }
}