package chess;

import java.util.ArrayList;

public class Chess {
    enum Player { white, black }
    private static Player currentPlayer = Player.white;
    private static Piece[][] boardInternal; // Row 0=rank 8, row 7=rank 1

    public static ReturnPlay play(String move) {
        move = move.trim();

        // Handle resign
        if (move.equalsIgnoreCase("resign")) {
            ReturnPlay ret = new ReturnPlay();
            ret.piecesOnBoard = convertBoard();
            ret.message = (currentPlayer == Player.white)
                    ? ReturnPlay.Message.RESIGN_BLACK_WINS
                    : ReturnPlay.Message.RESIGN_WHITE_WINS;
            return ret;
        }

        // Parse draw request (e.g. "g1 f3 draw?")
        String[] tokens = move.split("\\s+");
        boolean drawRequested = false;
        if (tokens[tokens.length - 1].equals("draw?")) {
            drawRequested = true;
            String[] newTokens = new String[tokens.length - 1];
            System.arraycopy(tokens, 0, newTokens, 0, tokens.length - 1);
            tokens = newTokens;
        }

        if (tokens.length < 2) {
            return illegalMove();
        }

        // Parse source + destination (e.g. "e2 e4")
        String src = tokens[0];
        String dest = tokens[1];
        if (src.length() != 2 || dest.length() != 2) {
            return illegalMove();
        }

        char srcFile = src.charAt(0), destFile = dest.charAt(0);
        int srcRank = src.charAt(1) - '0';
        int destRank = dest.charAt(1) - '0';
        int srcCol = srcFile - 'a', srcRow = 8 - srcRank;
        int destCol = destFile - 'a', destRow = 8 - destRank;

        // Board bounds check
        if (!inBounds(srcRow, srcCol) || !inBounds(destRow, destCol)) {
            return illegalMove();
        }

        Piece movingPiece = boardInternal[srcRow][srcCol];
        if (movingPiece == null) {
            return illegalMove();
        }

        boolean isWhiteTurn = (currentPlayer == Player.white);
        // Must move your own piece
        if (movingPiece.isWhite() != isWhiteTurn) {
            return illegalMove();
        }

        // Detect promotion + castling
        boolean isPromotion = false;
        if (movingPiece instanceof Pawn) {
            boolean whitePromotion = (movingPiece.isWhite() && destRow == 0);
            boolean blackPromotion = (!movingPiece.isWhite() && destRow == 7);
            isPromotion = (whitePromotion || blackPromotion);
        }
        boolean isCastling = false;
        if (movingPiece instanceof King && srcRow == destRow && Math.abs(srcCol - destCol) == 2) {
            isCastling = true;
        }

        // Validate piece movement
        if (!movingPiece.isMoveValid(destRow, destCol, boardInternal)) {
            return illegalMove();
        }

        // Check that the move does not leave the player's king in check
        if (!simulateAndCheck(movingPiece, srcRow, srcCol, destRow, destCol, isPromotion, tokens)) {
            return illegalMove();
        }

        // If castling, check advanced Wikipedia rules: not in check, no passing through attack
        if (isCastling) {
            // 1) King must NOT be in check currently
            if (isKingInCheck(movingPiece.isWhite())) {
                return illegalMove();
            }
            // 2) The squares the king crosses (including final square) must NOT be under attack
            int step = (destCol > srcCol) ? 1 : -1;
            // e.g. if going from e1 to g1 => col from 4 to 6 => check squares col=5 and col=6
            for (int c = srcCol; c != destCol + step; c += step) {
                // skip the initial square if you want (but if the king is in check on initial, we fail above)
                if (isSquareUnderAttack(srcRow, c, !movingPiece.isWhite())) {
                    return illegalMove();
                }
            }
        }

        // Execute the move
        if (isCastling) {
            // Move king
            movingPiece.move(destRow, destCol);
            boardInternal[destRow][destCol] = movingPiece;
            boardInternal[srcRow][srcCol] = null;

            // Move rook
            if (destCol > srcCol) { // kingside
                Piece rook = boardInternal[srcRow][7];
                rook.move(srcRow, destCol - 1);
                boardInternal[srcRow][destCol - 1] = rook;
                boardInternal[srcRow][7] = null;
            } else { // queenside
                Piece rook = boardInternal[srcRow][0];
                rook.move(srcRow, destCol + 1);
                boardInternal[srcRow][destCol + 1] = rook;
                boardInternal[srcRow][0] = null;
            }
        } else if (isPromotion) {
            // Promotion
            char promoChar = (tokens.length == 3) ? tokens[2].charAt(0) : 'Q';
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
            // Normal move
            boardInternal[destRow][destCol] = movingPiece;
            boardInternal[srcRow][srcCol] = null;
            movingPiece.move(destRow, destCol);
        }

        // Post-move: check if opponent is in check or checkmate
        boolean opponentIsWhite = !isWhiteTurn;
        boolean opponentInCheck = isKingInCheck(opponentIsWhite);
        boolean opponentCheckmate = opponentInCheck && isCheckmate(opponentIsWhite);

        // Switch turn
        currentPlayer = (currentPlayer == Player.white) ? Player.black : Player.white;

        // Build ReturnPlay
        ReturnPlay ret = new ReturnPlay();
        ret.piecesOnBoard = convertBoard();
        if (opponentCheckmate) {
            ret.message = opponentIsWhite
                    ? ReturnPlay.Message.CHECKMATE_BLACK_WINS
                    : ReturnPlay.Message.CHECKMATE_WHITE_WINS;
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

        // Black pieces
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

        // White pieces
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
     * Temporarily executes a move and checks if it leaves the mover's king in check.
     */
    private static boolean simulateAndCheck(
            Piece movingPiece, int srcRow, int srcCol,
            int destRow, int destCol,
            boolean isPromotion, String[] tokens
    ) {
        Piece originalDest = boardInternal[destRow][destCol];
        int origRow = movingPiece.row;
        int origCol = movingPiece.col;
        boolean origHasMoved = movingPiece.hasMoved();

        // Temporarily move
        boardInternal[destRow][destCol] = movingPiece;
        boardInternal[srcRow][srcCol] = null;
        movingPiece.row = destRow;
        movingPiece.col = destCol;
        movingPiece.setHasMoved(true);

        // If it's a promotion, pretend it's a queen
        Piece tempPromoted = null;
        if (isPromotion) {
            tempPromoted = new Queen(destRow, destCol, movingPiece.isWhite());
            boardInternal[destRow][destCol] = tempPromoted;
        }

        boolean inCheck = isKingInCheck(movingPiece.isWhite());

        // Revert
        boardInternal[srcRow][srcCol] = movingPiece;
        boardInternal[destRow][destCol] = originalDest;
        movingPiece.row = origRow;
        movingPiece.col = origCol;
        movingPiece.setHasMoved(origHasMoved);

        return !inCheck;
    }

    /**
     * Checks if the king of the given color is in check.
     * i.e., is that king's square attacked by any opponent piece?
     */
    private static boolean isKingInCheck(boolean whiteKing) {
        int kingRow = -1, kingCol = -1;
        // Find the king
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = boardInternal[r][c];
                if (p instanceof King && p.isWhite() == whiteKing) {
                    kingRow = r;
                    kingCol = c;
                    break;
                }
            }
            if (kingRow != -1) break;
        }
        // If we never found a king, just say it's in check to be safe
        if (kingRow == -1) return true;

        // If any opponent piece can move onto king's square, king is in check
        return isSquareUnderAttack(kingRow, kingCol, !whiteKing);
    }

    /**
     * Checks if the player of the given color is in checkmate:
     * 1) The king is in check,
     * 2) No move can remove the check.
     */
    private static boolean isCheckmate(boolean whitePlayer) {
        if (!isKingInCheck(whitePlayer)) {
            return false;
        }
        // If every possible move still leaves the king in check, it's checkmate
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = boardInternal[r][c];
                if (p != null && p.isWhite() == whitePlayer) {
                    for (int dr = 0; dr < 8; dr++) {
                        for (int dc = 0; dc < 8; dc++) {
                            if (p.isMoveValid(dr, dc, boardInternal)) {
                                // Simulate
                                Piece origDest = boardInternal[dr][dc];
                                int origRow = p.row, origCol = p.col;
                                boolean origMoved = p.hasMoved();

                                boardInternal[dr][dc] = p;
                                boardInternal[r][c] = null;
                                p.row = dr; p.col = dc; p.setHasMoved(true);

                                boolean stillInCheck = isKingInCheck(whitePlayer);

                                // revert
                                boardInternal[r][c] = p;
                                boardInternal[dr][dc] = origDest;
                                p.row = origRow; p.col = origCol; p.setHasMoved(origMoved);

                                if (!stillInCheck) {
                                    return false; // Found a move that escapes check
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Returns true if the square (row,col) is under attack by the color "attackerIsWhite."
     */
    private static boolean isSquareUnderAttack(int row, int col, boolean attackerIsWhite) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = boardInternal[r][c];
                if (p != null && p.isWhite() == attackerIsWhite) {
                    // If that piece can validly move onto (row,col), it's under attack
                    if (p.isMoveValid(row, col, boardInternal)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** Returns a standard ILLEGAL_MOVE ReturnPlay result */
    private static ReturnPlay illegalMove() {
        ReturnPlay ret = new ReturnPlay();
        ret.piecesOnBoard = convertBoard();
        ret.message = ReturnPlay.Message.ILLEGAL_MOVE;
        return ret;
    }

    /** Quick helper to check board boundaries */
    private static boolean inBounds(int r, int c) {
        return (r >= 0 && r < 8 && c >= 0 && c < 8);
    }

    /**
     * Convert internal board to ArrayList<ReturnPiece> for the autograder.
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