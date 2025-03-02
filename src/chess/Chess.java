package chess;

import java.util.ArrayList;

public class Chess {
    enum Player { white, black }
    private static Player currentPlayer = Player.white;
    // Our internal board using our Piece hierarchy.
    // Row 0 is rank 8; row 7 is rank 1.
    private static Piece[][] boardInternal;

    /**
     * Plays the next move for the current player.
     * Handles normal moves, castling, pawn promotion, resign, and draw.
     * Also prevents moves that leave the player's king in check.
     *
     * @param move The move string (e.g., "e2 e4", "g7 g8 N", "resign", or with "draw?" appended)
     * @return A ReturnPlay instance representing the updated board and game status.
     */
    public static ReturnPlay play(String move) {
        move = move.trim();

        // Handle resign command.
        if (move.equalsIgnoreCase("resign")) {
            ReturnPlay ret = new ReturnPlay();
            ret.piecesOnBoard = convertBoard();
            ret.message = (currentPlayer == Player.white) ?
                    ReturnPlay.Message.RESIGN_BLACK_WINS : ReturnPlay.Message.RESIGN_WHITE_WINS;
            return ret;
        }

        // Split the move into tokens.
        String[] tokens = move.split("\\s+");
        boolean drawRequested = false;
        if (tokens[tokens.length - 1].equals("draw?")) {
            drawRequested = true;
            String[] newTokens = new String[tokens.length - 1];
            System.arraycopy(tokens, 0, newTokens, 0, tokens.length - 1);
            tokens = newTokens;
        }

        // A valid move (other than resign) must have at least 2 tokens.
        if (tokens.length < 2) {
            ReturnPlay ret = new ReturnPlay();
            ret.piecesOnBoard = convertBoard();
            ret.message = ReturnPlay.Message.ILLEGAL_MOVE;
            return ret;
        }

        // Parse source and destination squares.
        String src = tokens[0];
        String dest = tokens[1];
        if (src.length() != 2 || dest.length() != 2) {
            ReturnPlay ret = new ReturnPlay();
            ret.piecesOnBoard = convertBoard();
            ret.message = ReturnPlay.Message.ILLEGAL_MOVE;
            return ret;
        }
        char srcFile = src.charAt(0);
        char destFile = dest.charAt(0);
        int srcRank = src.charAt(1) - '0';
        int destRank = dest.charAt(1) - '0';
        int srcCol = srcFile - 'a';
        int srcRow = 8 - srcRank;
        int destCol = destFile - 'a';
        int destRow = 8 - destRank;

        // Verify indices are within bounds.
        if (srcRow < 0 || srcRow > 7 || srcCol < 0 || srcCol > 7 ||
                destRow < 0 || destRow > 7 || destCol < 0 || destCol > 7) {
            ReturnPlay ret = new ReturnPlay();
            ret.piecesOnBoard = convertBoard();
            ret.message = ReturnPlay.Message.ILLEGAL_MOVE;
            return ret;
        }

        Piece movingPiece = boardInternal[srcRow][srcCol];
        if (movingPiece == null) {
            ReturnPlay ret = new ReturnPlay();
            ret.piecesOnBoard = convertBoard();
            ret.message = ReturnPlay.Message.ILLEGAL_MOVE;
            return ret;
        }
        // Verify the piece belongs to the current player.
        boolean isWhiteTurn = (currentPlayer == Player.white);
        if (movingPiece.isWhite() != isWhiteTurn) {
            ReturnPlay ret = new ReturnPlay();
            ret.piecesOnBoard = convertBoard();
            ret.message = ReturnPlay.Message.ILLEGAL_MOVE;
            return ret;
        }

        // Detect special moves.
        boolean isPromotion = false;
        if (movingPiece instanceof Pawn) {
            if ((movingPiece.isWhite() && destRow == 0) ||
                    (!movingPiece.isWhite() && destRow == 7)) {
                isPromotion = true;
            }
        }
        boolean isCastling = false;
        if (movingPiece instanceof King && srcRow == destRow && Math.abs(srcCol - destCol) == 2) {
            isCastling = true;
        }

        // Basic move validation by the piece.
        if (!movingPiece.isMoveValid(destRow, destCol, boardInternal)) {
            ReturnPlay ret = new ReturnPlay();
            ret.piecesOnBoard = convertBoard();
            ret.message = ReturnPlay.Message.ILLEGAL_MOVE;
            return ret;
        }

        // Ensure the move does not leave the player's king in check.
        if (!simulateAndCheck(movingPiece, srcRow, srcCol, destRow, destCol, isPromotion, tokens)) {
            ReturnPlay ret = new ReturnPlay();
            ret.piecesOnBoard = convertBoard();
            ret.message = ReturnPlay.Message.ILLEGAL_MOVE;
            return ret;
        }

        // Execute the move.
        if (isCastling) {
            // Move king.
            movingPiece.move(destRow, destCol);
            boardInternal[destRow][destCol] = movingPiece;
            boardInternal[srcRow][srcCol] = null;
            // Move rook accordingly.
            if (destCol > srcCol) { // kingside castling
                Piece rook = boardInternal[srcRow][7];
                rook.move(srcRow, destCol - 1);
                boardInternal[srcRow][destCol - 1] = rook;
                boardInternal[srcRow][7] = null;
            } else { // queenside castling
                Piece rook = boardInternal[srcRow][0];
                rook.move(srcRow, destCol + 1);
                boardInternal[srcRow][destCol + 1] = rook;
                boardInternal[srcRow][0] = null;
            }
        } else if (isPromotion) {
            // Handle pawn promotion.
            char promoChar = 'Q';
            if (tokens.length == 3) {
                promoChar = tokens[2].charAt(0);
            }
            Piece promoted;
            if (promoChar == 'N' || promoChar == 'n') {
                promoted = new Knight(destRow, destCol, movingPiece.isWhite());
            } else if (promoChar == 'R' || promoChar == 'r') {
                promoted = new Rook(destRow, destCol, movingPiece.isWhite());
            } else if (promoChar == 'B' || promoChar == 'b') {
                promoted = new Bishop(destRow, destCol, movingPiece.isWhite());
            } else {
                promoted = new Queen(destRow, destCol, movingPiece.isWhite());
            }
            boardInternal[destRow][destCol] = promoted;
            boardInternal[srcRow][srcCol] = null;
        } else {
            // Normal move.
            boardInternal[destRow][destCol] = movingPiece;
            boardInternal[srcRow][srcCol] = null;
            movingPiece.move(destRow, destCol);
        }

        // After the move, check for check and checkmate on the opponent.
        boolean opponentIsWhite = !isWhiteTurn;
        boolean opponentInCheck = isKingInCheck(opponentIsWhite);
        boolean opponentCheckmate = opponentInCheck && isCheckmate(opponentIsWhite);

        // Switch turn.
        currentPlayer = (currentPlayer == Player.white) ? Player.black : Player.white;

        // Build and return the ReturnPlay object.
        ReturnPlay ret = new ReturnPlay();
        ret.piecesOnBoard = convertBoard();
        if (opponentCheckmate) {
            ret.message = opponentIsWhite ?
                    ReturnPlay.Message.CHECKMATE_BLACK_WINS : ReturnPlay.Message.CHECKMATE_WHITE_WINS;
        } else if (opponentInCheck) {
            ret.message = ReturnPlay.Message.CHECK;
        } else if (drawRequested) {
            ret.message = ReturnPlay.Message.DRAW;
        } else {
            ret.message = null;
        }
        return ret;
    }

    /**
     * Resets the game and sets up the board to the initial configuration.
     */
    public static void start() {
        currentPlayer = Player.white;
        boardInternal = new Piece[8][8];

        // Setup Black pieces.
        boardInternal[0][0] = new Rook(0, 0, false);
        boardInternal[0][1] = new Knight(0, 1, false);
        boardInternal[0][2] = new Bishop(0, 2, false);
        boardInternal[0][3] = new Queen(0, 3, false);
        boardInternal[0][4] = new King(0, 4, false);
        boardInternal[0][5] = new Bishop(0, 5, false);
        boardInternal[0][6] = new Knight(0, 6, false);
        boardInternal[0][7] = new Rook(0, 7, false);
        for (int col = 0; col < 8; col++) {
            boardInternal[1][col] = new Pawn(1, col, false);
        }

        // Setup White pieces.
        boardInternal[7][0] = new Rook(7, 0, true);
        boardInternal[7][1] = new Knight(7, 1, true);
        boardInternal[7][2] = new Bishop(7, 2, true);
        boardInternal[7][3] = new Queen(7, 3, true);
        boardInternal[7][4] = new King(7, 4, true);
        boardInternal[7][5] = new Bishop(7, 5, true);
        boardInternal[7][6] = new Knight(7, 6, true);
        boardInternal[7][7] = new Rook(7, 7, true);
        for (int col = 0; col < 8; col++) {
            boardInternal[6][col] = new Pawn(6, col, true);
        }
    }

    /**
     * Simulates a move (or promotion) and checks if it leaves the moving side's king in check.
     * The move is executed temporarily, then reverted.
     * Returns true if the move does NOT leave the king in check.
     */
    private static boolean simulateAndCheck(Piece movingPiece, int srcRow, int srcCol, int destRow, int destCol, boolean isPromotion, String[] tokens) {
        // Save original state.
        Piece originalDest = boardInternal[destRow][destCol];
        int origRow = movingPiece.row;
        int origCol = movingPiece.col;
        boolean origHasMoved = movingPiece.hasMoved();

        // Execute move temporarily.
        boardInternal[destRow][destCol] = movingPiece;
        boardInternal[srcRow][srcCol] = null;
        movingPiece.row = destRow;
        movingPiece.col = destCol;
        movingPiece.hasMoved = true;

        // For promotion, simulate as if promoted to Queen.
        Piece tempPromoted = null;
        if (isPromotion) {
            tempPromoted = new Queen(destRow, destCol, movingPiece.isWhite());
            boardInternal[destRow][destCol] = tempPromoted;
        }

        boolean inCheck = isKingInCheck(movingPiece.isWhite());

        // Revert move.
        boardInternal[srcRow][srcCol] = movingPiece;
        boardInternal[destRow][destCol] = originalDest;
        movingPiece.row = origRow;
        movingPiece.col = origCol;
        movingPiece.hasMoved = origHasMoved;
        return !inCheck;
    }

    /**
     * Checks if the king of the given color is in check.
     * @param whiteKing True if checking for white king; false for black king.
     * @return True if the king is in check.
     */
    private static boolean isKingInCheck(boolean whiteKing) {
        int kingRow = -1, kingCol = -1;
        // Locate the king.
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = boardInternal[r][c];
                if (p != null && p instanceof King && p.isWhite() == whiteKing) {
                    kingRow = r;
                    kingCol = c;
                    break;
                }
            }
            if (kingRow != -1) break;
        }
        if (kingRow == -1) return true; // Should not occur.

        // Check all opponent pieces.
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = boardInternal[r][c];
                if (p != null && p.isWhite() != whiteKing) {
                    if (p.isMoveValid(kingRow, kingCol, boardInternal)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if the player of the given color is in checkmate.
     * @param whitePlayer True for white; false for black.
     * @return True if the player is in checkmate.
     */
    private static boolean isCheckmate(boolean whitePlayer) {
        if (!isKingInCheck(whitePlayer)) return false;
        // For each piece of the given color, try every possible move.
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = boardInternal[r][c];
                if (p != null && p.isWhite() == whitePlayer) {
                    for (int dr = 0; dr < 8; dr++) {
                        for (int dc = 0; dc < 8; dc++) {
                            if (p.isMoveValid(dr, dc, boardInternal)) {
                                // Simulate move.
                                Piece originalDest = boardInternal[dr][dc];
                                int origRow = p.row;
                                int origCol = p.col;
                                boolean origHasMoved = p.hasMoved();

                                boardInternal[dr][dc] = p;
                                boardInternal[r][c] = null;
                                p.row = dr;
                                p.col = dc;
                                p.hasMoved = true;

                                boolean stillInCheck = isKingInCheck(whitePlayer);

                                // Revert move.
                                boardInternal[r][c] = p;
                                boardInternal[dr][dc] = originalDest;
                                p.row = origRow;
                                p.col = origCol;
                                p.hasMoved = origHasMoved;

                                if (!stillInCheck) return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Converts the internal board (Piece[][]) into an ArrayList of ReturnPiece objects.
     */
    private static ArrayList<ReturnPiece> convertBoard() {
        ArrayList<ReturnPiece> list = new ArrayList<>();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = boardInternal[r][c];
                if (p != null) {
                    ReturnPiece rp = new ReturnPiece();
                    String code = p.getPieceCode();
                    if (code.equals("WP")) rp.pieceType = ReturnPiece.PieceType.WP;
                    else if (code.equals("WR")) rp.pieceType = ReturnPiece.PieceType.WR;
                    else if (code.equals("WN")) rp.pieceType = ReturnPiece.PieceType.WN;
                    else if (code.equals("WB")) rp.pieceType = ReturnPiece.PieceType.WB;
                    else if (code.equals("WQ")) rp.pieceType = ReturnPiece.PieceType.WQ;
                    else if (code.equals("WK")) rp.pieceType = ReturnPiece.PieceType.WK;
                    else if (code.equals("BP")) rp.pieceType = ReturnPiece.PieceType.BP;
                    else if (code.equals("BR")) rp.pieceType = ReturnPiece.PieceType.BR;
                    else if (code.equals("BN")) rp.pieceType = ReturnPiece.PieceType.BN;
                    else if (code.equals("BB")) rp.pieceType = ReturnPiece.PieceType.BB;
                    else if (code.equals("BQ")) rp.pieceType = ReturnPiece.PieceType.BQ;
                    else if (code.equals("BK")) rp.pieceType = ReturnPiece.PieceType.BK;

                    rp.pieceFile = ReturnPiece.PieceFile.values()[c];
                    rp.pieceRank = 8 - r;
                    list.add(rp);
                }
            }
        }
        return list;
    }
}