package chess;

public class Chess {
    enum Player { white, black }
    private static ReturnPiece[][] board = new ReturnPiece[8][8]; // 8x8 grid
    private static Player currentPlayer = Player.white; // Track whose turn it is





    /**
     * Plays the next move for whichever player has the turn.
     *
     * @param move String for next move, e.g. "a2 a3"
     *
     * @return A ReturnPlay instance that contains the result of the move.
     *         See the section "The Chess class" in the assignment description for details of
     *         the contents of the returned ReturnPlay instance.
     */
    public static ReturnPlay play(String move) {

        /* FILL IN THIS METHOD */

        /* FOLLOWING LINE IS A PLACEHOLDER TO MAKE COMPILER HAPPY */
        /* WHEN YOU FILL IN THIS METHOD, YOU NEED TO RETURN A ReturnPlay OBJECT */
        return null;
    }


    /**
     * This method should reset the game, and start from scratch.
     */
    public static void start() {
        // Reset board and set current player to white
        board = new ReturnPiece[8][8];
        currentPlayer = Player.white;

        ReturnPiece piece;  // temporary variable for creating pieces

        // ---------------------------
        // Set up White Pieces
        // ---------------------------
        // White back rank (Rank 1). Array index for rank 1 is 8 - 1 = 7.
        piece = new ReturnPiece();
        piece.pieceType = ReturnPiece.PieceType.WR;
        piece.pieceFile = ReturnPiece.PieceFile.a;
        piece.pieceRank = 1;
        board[7][0] = piece;

        piece = new ReturnPiece();
        piece.pieceType = ReturnPiece.PieceType.WN;
        piece.pieceFile = ReturnPiece.PieceFile.b;
        piece.pieceRank = 1;
        board[7][1] = piece;

        piece = new ReturnPiece();
        piece.pieceType = ReturnPiece.PieceType.WB;
        piece.pieceFile = ReturnPiece.PieceFile.c;
        piece.pieceRank = 1;
        board[7][2] = piece;

        piece = new ReturnPiece();
        piece.pieceType = ReturnPiece.PieceType.WQ;
        piece.pieceFile = ReturnPiece.PieceFile.d;
        piece.pieceRank = 1;
        board[7][3] = piece;

        piece = new ReturnPiece();
        piece.pieceType = ReturnPiece.PieceType.WK;
        piece.pieceFile = ReturnPiece.PieceFile.e;
        piece.pieceRank = 1;
        board[7][4] = piece;

        piece = new ReturnPiece();
        piece.pieceType = ReturnPiece.PieceType.WB;
        piece.pieceFile = ReturnPiece.PieceFile.f;
        piece.pieceRank = 1;
        board[7][5] = piece;

        piece = new ReturnPiece();
        piece.pieceType = ReturnPiece.PieceType.WN;
        piece.pieceFile = ReturnPiece.PieceFile.g;
        piece.pieceRank = 1;
        board[7][6] = piece;

        piece = new ReturnPiece();
        piece.pieceType = ReturnPiece.PieceType.WR;
        piece.pieceFile = ReturnPiece.PieceFile.h;
        piece.pieceRank = 1;
        board[7][7] = piece;

        // White pawns on Rank 2. Array index for rank 2 is 8 - 2 = 6.
        for (int i = 0; i < 8; i++) {
            piece = new ReturnPiece();
            piece.pieceType = ReturnPiece.PieceType.WP;
            piece.pieceFile = ReturnPiece.PieceFile.values()[i];  // Files a-h
            piece.pieceRank = 2;
            board[6][i] = piece;
        }

        // ---------------------------
        // Set up Black Pieces
        // ---------------------------
        // Black back rank (Rank 8). Array index for rank 8 is 8 - 8 = 0.
        piece = new ReturnPiece();
        piece.pieceType = ReturnPiece.PieceType.BR;
        piece.pieceFile = ReturnPiece.PieceFile.a;
        piece.pieceRank = 8;
        board[0][0] = piece;

        piece = new ReturnPiece();
        piece.pieceType = ReturnPiece.PieceType.BN;
        piece.pieceFile = ReturnPiece.PieceFile.b;
        piece.pieceRank = 8;
        board[0][1] = piece;

        piece = new ReturnPiece();
        piece.pieceType = ReturnPiece.PieceType.BB;
        piece.pieceFile = ReturnPiece.PieceFile.c;
        piece.pieceRank = 8;
        board[0][2] = piece;

        piece = new ReturnPiece();
        piece.pieceType = ReturnPiece.PieceType.BQ;
        piece.pieceFile = ReturnPiece.PieceFile.d;
        piece.pieceRank = 8;
        board[0][3] = piece;

        piece = new ReturnPiece();
        piece.pieceType = ReturnPiece.PieceType.BK;
        piece.pieceFile = ReturnPiece.PieceFile.e;
        piece.pieceRank = 8;
        board[0][4] = piece;

        piece = new ReturnPiece();
        piece.pieceType = ReturnPiece.PieceType.BB;
        piece.pieceFile = ReturnPiece.PieceFile.f;
        piece.pieceRank = 8;
        board[0][5] = piece;

        piece = new ReturnPiece();
        piece.pieceType = ReturnPiece.PieceType.BN;
        piece.pieceFile = ReturnPiece.PieceFile.g;
        piece.pieceRank = 8;
        board[0][6] = piece;

        piece = new ReturnPiece();
        piece.pieceType = ReturnPiece.PieceType.BR;
        piece.pieceFile = ReturnPiece.PieceFile.h;
        piece.pieceRank = 8;
        board[0][7] = piece;

        // Black pawns on Rank 7. Array index for rank 7 is 8 - 7 = 1.
        for (int i = 0; i < 8; i++) {
            piece = new ReturnPiece();
            piece.pieceType = ReturnPiece.PieceType.BP;
            piece.pieceFile = ReturnPiece.PieceFile.values()[i];  // Files a-h
            piece.pieceRank = 7;
            board[1][i] = piece;
        }
    }
}
