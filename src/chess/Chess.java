package chess;

import java.util.ArrayList;

public class Chess {
    enum Player { white, black }
    private static Player currentPlayer = Player.white;
    private static Piece[][] boardInternal;

    // Added for en passant support:
    private static int[] enPassantTargetSquare = null;
    private static Pawn enPassantPawn = null;

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
        System.out.println("Moving piece at " + src + " (" + srcRow + ", " + srcCol + "): " +
                (movingPiece != null ? movingPiece.getClass().getSimpleName() : "null"));

        if (movingPiece == null) {
            return illegalMove();
        }

        boolean isWhiteTurn = (currentPlayer == Player.white);

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

        // Detect en passant (only possible for pawn diagonal moves into an empty square)
        boolean isEnPassant = false;
        if (movingPiece instanceof Pawn) {
            int direction = movingPiece.isWhite() ? -1 : 1;
            int rowDiff = destRow - srcRow;
            if (Math.abs(srcCol - destCol) == 1 && rowDiff == direction && boardInternal[destRow][destCol] == null) {
                if (enPassantTargetSquare != null &&
                        enPassantTargetSquare[0] == destRow &&
                        enPassantTargetSquare[1] == destCol) {
                    isEnPassant = true;
                }
            }
        }

        // Validate piece movement (bypass check for en passant)
        if (!isEnPassant && !movingPiece.isMoveValid(destRow, destCol, boardInternal)) {
            return illegalMove();
        }

        // Check that the move does not leave the player's king in check
        if (!simulateAndCheck(movingPiece, srcRow, srcCol, destRow, destCol, isPromotion, tokens)) {
            return illegalMove();
        }


        if (isCastling) {
            if (isKingInCheck(movingPiece.isWhite())) {
                return illegalMove();
            }
            int step = (destCol > srcCol) ? 1 : -1;
            for (int c = srcCol; c != destCol + step; c += step) {
                if (isSquareUnderAttack(srcRow, c, !movingPiece.isWhite())) {
                    return illegalMove();
                }
            }
        }


        if (isCastling) {
            movingPiece.move(destRow, destCol);
            boardInternal[destRow][destCol] = movingPiece;
            boardInternal[srcRow][srcCol] = null;
            if (destCol > srcCol) {
                Piece rook = boardInternal[srcRow][7];
                rook.move(srcRow, destCol - 1);
                boardInternal[srcRow][destCol - 1] = rook;
                boardInternal[srcRow][7] = null;
            } else {
                Piece rook = boardInternal[srcRow][0];
                rook.move(srcRow, destCol + 1);
                boardInternal[srcRow][destCol + 1] = rook;
                boardInternal[srcRow][0] = null;
            }
        } else if (isPromotion) {
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
        } else if (isEnPassant) {

            boardInternal[destRow][destCol] = movingPiece;
            boardInternal[srcRow][srcCol] = null;
            movingPiece.move(destRow, destCol);
            int capturedRow = movingPiece.isWhite() ? destRow + 1 : destRow - 1;
            boardInternal[capturedRow][destCol] = null;
        } else {
            boardInternal[destRow][destCol] = movingPiece;
            boardInternal[srcRow][srcCol] = null;
            movingPiece.move(destRow, destCol);
        }


        if (movingPiece instanceof Pawn) {
            // If pawn moved two squares forward, mark the en passant target square
            if (Math.abs(srcRow - destRow) == 2) {
                int targetRow = (srcRow + destRow) / 2;
                enPassantTargetSquare = new int[] { targetRow, srcCol };
                enPassantPawn = (Pawn) movingPiece;
            } else {
                enPassantTargetSquare = null;
                enPassantPawn = null;
            }
        } else {
            enPassantTargetSquare = null;
            enPassantPawn = null;
        }


        boolean opponentIsWhite = !isWhiteTurn;
        boolean opponentInCheck = isKingInCheck(opponentIsWhite);
        boolean opponentCheckmate = opponentInCheck && isCheckmate(opponentIsWhite);


        currentPlayer = (currentPlayer == Player.white) ? Player.black : Player.white;


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

    private static boolean simulateAndCheck(
            Piece movingPiece, int srcRow, int srcCol,
            int destRow, int destCol,
            boolean isPromotion, String[] tokens
    ) {
        Piece originalDest = boardInternal[destRow][destCol];
        int origRow = movingPiece.row;
        int origCol = movingPiece.col;
        boolean origHasMoved = movingPiece.hasMoved();

        // Detect en passant
        boolean isEnPassant = false;
        if (movingPiece instanceof Pawn) {
            int direction = movingPiece.isWhite() ? -1 : 1;
            int rowDiff = destRow - srcRow;
            if (Math.abs(srcCol - destCol) == 1 && rowDiff == direction && boardInternal[destRow][destCol] == null) {
                if (enPassantTargetSquare != null &&
                        enPassantTargetSquare[0] == destRow &&
                        enPassantTargetSquare[1] == destCol) {
                    isEnPassant = true;
                }
            }
        }
        Piece originalCaptured = null;
        int capturedRow = -1;
        if (isEnPassant) {
            capturedRow = movingPiece.isWhite() ? destRow + 1 : destRow - 1;
            originalCaptured = boardInternal[capturedRow][destCol];
            boardInternal[capturedRow][destCol] = null;
        }

        boardInternal[destRow][destCol] = movingPiece;
        boardInternal[srcRow][srcCol] = null;
        movingPiece.row = destRow;
        movingPiece.col = destCol;
        movingPiece.setHasMoved(true);

        Piece tempPromoted = null;
        if (isPromotion) {
            tempPromoted = new Queen(destRow, destCol, movingPiece.isWhite());
            boardInternal[destRow][destCol] = tempPromoted;
        }

        boolean inCheck = isKingInCheck(movingPiece.isWhite());


        boardInternal[srcRow][srcCol] = movingPiece;
        boardInternal[destRow][destCol] = originalDest;
        movingPiece.row = origRow;
        movingPiece.col = origCol;
        movingPiece.setHasMoved(origHasMoved);
        if (isEnPassant) {
            boardInternal[capturedRow][destCol] = originalCaptured;
        }

        return !inCheck;
    }

    private static boolean isKingInCheck(boolean whiteKing) {
        int kingRow = -1, kingCol = -1;
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
        if (kingRow == -1) return true;
        return isSquareUnderAttack(kingRow, kingCol, !whiteKing);
    }

    private static boolean isCheckmate(boolean whitePlayer) {
        if (!isKingInCheck(whitePlayer)) {
            return false;
        }
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = boardInternal[r][c];
                if (p != null && p.isWhite() == whitePlayer) {
                    for (int dr = 0; dr < 8; dr++) {
                        for (int dc = 0; dc < 8; dc++) {
                            if (p.isMoveValid(dr, dc, boardInternal)) {
                                Piece origDest = boardInternal[dr][dc];
                                int origRow = p.row, origCol = p.col;
                                boolean origMoved = p.hasMoved();

                                boardInternal[dr][dc] = p;
                                boardInternal[r][c] = null;
                                p.row = dr; p.col = dc; p.setHasMoved(true);

                                boolean stillInCheck = isKingInCheck(whitePlayer);

                                boardInternal[r][c] = p;
                                boardInternal[dr][dc] = origDest;
                                p.row = origRow; p.col = origCol; p.setHasMoved(origMoved);

                                if (!stillInCheck) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private static boolean isSquareUnderAttack(int row, int col, boolean attackerIsWhite) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = boardInternal[r][c];
                if (p != null && p.isWhite() == attackerIsWhite) {
                    if (p.isMoveValid(row, col, boardInternal)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static ReturnPlay illegalMove() {
        ReturnPlay ret = new ReturnPlay();
        ret.piecesOnBoard = convertBoard();
        ret.message = ReturnPlay.Message.ILLEGAL_MOVE;
        return ret;
    }

    private static boolean inBounds(int r, int c) {
        return (r >= 0 && r < 8 && c >= 0 && c < 8);
    }

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
