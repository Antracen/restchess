package wass.chess;

import java.util.Stack;

public class ChessBoard {

    long version = 0; // Increment when board changes
    ChessPiece[][] board = new ChessPiece[8][8];
    Color turn = Color.WHITE;
    int[] lastMove = new int[4];
    GameResult result;
    String resultComment;
    Stack<CachedBoard> history = new Stack<>();

    ChessBoard() {
        for(int i = 0; i < 8; i++) {
            board[1][i] = new ChessPiece(Color.WHITE, Piece.PAWN);
            board[6][i] = new ChessPiece(Color.BLACK, Piece.PAWN);
        }

        board[0][0] = new ChessPiece(Color.WHITE, Piece.ROOK);
        board[0][7] = new ChessPiece(Color.WHITE, Piece.ROOK);
        board[7][0] = new ChessPiece(Color.BLACK, Piece.ROOK);
        board[7][7] = new ChessPiece(Color.BLACK, Piece.ROOK);

        board[0][1] = new ChessPiece(Color.WHITE, Piece.KNIGHT);
        board[0][6] = new ChessPiece(Color.WHITE, Piece.KNIGHT);
        board[7][1] = new ChessPiece(Color.BLACK, Piece.KNIGHT);
        board[7][6] = new ChessPiece(Color.BLACK, Piece.KNIGHT);

        board[0][2] = new ChessPiece(Color.WHITE, Piece.BISHOP);
        board[0][5] = new ChessPiece(Color.WHITE, Piece.BISHOP);
        board[7][2] = new ChessPiece(Color.BLACK, Piece.BISHOP);
        board[7][5] = new ChessPiece(Color.BLACK, Piece.BISHOP);

        board[0][3] = new ChessPiece(Color.WHITE, Piece.QUEEN);
        board[7][3] = new ChessPiece(Color.BLACK, Piece.QUEEN);

        board[0][4] = new ChessPiece(Color.WHITE, Piece.KING);
        board[7][4] = new ChessPiece(Color.BLACK, Piece.KING);

        cacheMove(board, turn, lastMove);
    }

    private void cacheMove(ChessPiece[][] board, Color turn, int[] lastMove) {
        CachedBoard cachedBoard = new CachedBoard(board, turn, lastMove);
    }

    public String getBoard() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                sb.append(pieceLetter(board[i][j]));
            }
        }
        return sb.toString();
    }

    public GameResult getResult() {
		return result;
	}

    public String getResultComment() {
		return resultComment;
	}

    public String getTurn() {
        if(turn == Color.WHITE) return "White";
        else return "Black";
    }

    public int[] getLastMove() {
        return lastMove;
    }

    private char pieceLetter(ChessPiece chessPiece) {
        char letter = '-';
        if(chessPiece != null) {
            switch (chessPiece.piece) {
                case PAWN:
                    letter = 'p';
                    break;
                case ROOK:
                    letter = 'r';
                    break;
                case KNIGHT:
                    letter = 'n';
                    break;
                case BISHOP:
                    letter = 'b';
                    break;
                case QUEEN:
                    letter = 'q';
                    break;
                case KING:
                    letter = 'k';
                    break;
            }
            if (chessPiece.color == Color.WHITE) letter = Character.toUpperCase(letter);
        }

        return letter;
    }

    public long getVersion() {
        return version;
    }

    public void undoMove() {
        if(history.empty()) return;
        version++;
        CachedBoard cachedBoard = history.pop();

        for (int r = 0; r < board.length; r++) {
            board[r] = cachedBoard.board[r].clone();
        }

        turn = cachedBoard.turn;
        lastMove = cachedBoard.lastMove.clone();
    }

    public void move(int row1, int col1, int row2, int col2) {
    	if (isGameEnded()) {
    		return;
    	}

        version++;
        if(board[row1][col1] == null || (row1 == row2 && col1 == col2)) return;

        if(legalMove(row1, col1, row2, col2)) {
            makeTheMove(row1, col1, row2, col2);
        }
        else if(enPassant(row1, col1, row2, col2)) {
            makeTheMove(row1, col1, row2, col2);

            // Captured en passant piece
            board[row1][col2] = null;
        }
        else if(castling(row1, col1, row2, col2)) {
            makeTheMove(row1, col1, row2, col2);

            // Move the rook
            int colStep = (col2-col1) < 0 ? -1 : 1;
            if(colStep == 1) {
                board[row1][col2-colStep] = board[row1][7];
                board[row1][7] = null;
            }
            else {
                board[row1][col2-colStep] = board[row1][0];
                board[row1][0] = null;
            }
        }
    }

    private void makeTheMove(int row1, int col1, int row2, int col2) {
        history.push(new CachedBoard(board, turn, lastMove));

        ChessPiece oldPiece = board[row2][col2];
        board[row2][col2] = board[row1][col1];
        board[row1][col1] = null;

        if (IsInCheck(turn)) {
        	System.out.println("Illegal move, " + turn + " king would be in check");
        	board[row1][col1] = board[row2][col2];
        	board[row2][col2] = oldPiece;
        	return;
        }

        if (board[row2][col2].piece == Piece.PAWN && (row2 == 7 || row2 == 0)) {
        	board[row2][col2] = new ChessPiece(board[row2][col2].color, Piece.QUEEN);
        }

        lastMove[0] = row1;
        lastMove[1] = col1;
        lastMove[2] = row2;
        lastMove[3] = col2;

        turn = turn == Color.WHITE ? Color.BLACK : Color.WHITE;
    }

    private boolean IsInCheck(Color kingColor) {

    	Color originalToMove = turn;
    	try {
	    	Color opponentColor = kingColor.opposite();

	    	turn = opponentColor; // Otherwise legalMove will not allow it

	    	for (int i = 0; i < board.length; ++i) {
	    		for (int j = 0; j < board.length; ++j) {
	    			ChessPiece piece = board[i][j];
	    			if (piece == null || piece.color != opponentColor) {
	    				continue;
	    			}

	    			// get all the valid capture moves for this piece.
	    			// This could of course be improved massively
	    			for (int newRow = 0; newRow < board.length; ++newRow) {
	    				for (int newCol = 0; newCol < board.length; ++newCol) {
	    					if (legalMove(i, j, newRow, newCol)) {
	    						ChessPiece capturePiece = board[i][j];
	    						ChessPiece destPiece = board[newRow][newCol];
	    						if (destPiece != null && destPiece.piece == Piece.KING) {
	    							System.out.println(destPiece + " in check by " + capturePiece);
	    							return true;
	    						}
	    					}
	    				}
	    			}
	    		}
	    	}

			return false;
    	}
    	finally {
    		turn = originalToMove;
    	}
	}

    private boolean legalMove(int row1, int col1, int row2, int col2) {
        ChessPiece pieceToMove = board[row1][col1];

        if(pieceToMove.color != turn) return false;

        if(pieceToMove.piece == Piece.PAWN) return legalPawnMove(pieceToMove.color, row1, col1, row2, col2);
        if(pieceToMove.piece == Piece.ROOK) return legalRookMove(pieceToMove.color, row1, col1, row2, col2);
        if(pieceToMove.piece == Piece.KNIGHT) return legalKnightMove(pieceToMove.color, row1, col1, row2, col2);
        if(pieceToMove.piece == Piece.BISHOP) return legalBishopMove(pieceToMove.color, row1, col1, row2, col2);
        if(pieceToMove.piece == Piece.QUEEN) return legalQueenMove(pieceToMove.color, row1, col1, row2, col2);
        if(pieceToMove.piece == Piece.KING) return legalKingMove(pieceToMove.color, row1, col1, row2, col2);

        // Don't validate king not in check here since the IsInCheck calls this method..

        return false;
    }

    private boolean legalPawnMove(Color color, int row1, int col1, int row2, int col2) {
        if(color == Color.WHITE) {
            return
                    (col1 == col2 && row1 + 1 == row2 && board[row2][col2] == null)
                            || (row1 == 1 && col1 == col2 && row1 + 2 == row2 && board[row2][col2] == null)
                            || (abs(col1 - col2) == 1 && row1 + 1 == row2 && board[row2][col2] != null && board[row2][col2].color != color);
        }
        else {
            return
                    (col1 == col2 && row1 - 1 == row2 && board[row2][col2] == null)
                            || (row1 == 6 && col1 == col2 && row1 - 2 == row2 && board[row2][col2] == null)
                            || (abs(col1 - col2) == 1 && row1 - 1 == row2 && board[row2][col2] != null && board[row2][col2].color != color);
        }
    }

    private boolean legalRookMove(Color color, int row1, int col1, int row2, int col2) {
        if(row1 != row2 && col1 != col2) return false; // Can't move in both directions

        int rowDirection = 0;
        int colDirection = 0;

        if(row1 != row2) rowDirection = (row2-row1) < 0 ? -1 : 1;
        if(col1 != col2) colDirection = (col2-col1) < 0 ? -1 : 1;

        for(int row = row1 + rowDirection; row != row2; row += rowDirection) {
            if(board[row][col1] != null) return false;
        }
        for(int col = col1 + colDirection; col != col2; col += colDirection) {
            if(board[row1][col] != null) return false;
        }

        if(board[row2][col2] == null) return true;
        else {
            return board[row2][col2].color != color;
        }
    }

    private boolean legalKnightMove(Color color, int row1, int col1, int row2, int col2) {
        int rowStep = abs(row1 - row2);
        int colStep = abs(col1 - col2);

        boolean valid = (rowStep == 1 && colStep == 2) || (rowStep == 2 && colStep == 1);
        if (!valid) return false;

        ChessPiece target = board[row2][col2];
        return target == null || target.color != color;
    }

    private boolean legalBishopMove(Color color, int row1, int col1, int row2, int col2) {
        int rowStep = abs(row1 - row2);
        int colStep = abs(col1 - col2);
        if(rowStep != colStep) return false;

        int rowDir = (row2-row1) < 0 ? -1 : 1;
        int colDir = (col2-col1) < 0 ? -1 : 1;

        for(int i = 1; i < rowStep; i++) {
            if(board[row1 + rowDir*i][col1 + colDir*i] != null) return false;
        }

        ChessPiece target = board[row2][col2];
        return target == null || target.color != color;
    }

    private boolean legalQueenMove(Color color, int row1, int col1, int row2, int col2) {
        return legalRookMove(color, row1, col1, row2, col2) || legalBishopMove(color, row1, col1, row2, col2);
    }

    private boolean legalKingMove(Color color, int row1, int col1, int row2, int col2) {
        int rowStep = abs(row1 - row2);
        int colStep = abs(col1 - col2);

        if(rowStep > 1 || colStep > 1) return false;

        ChessPiece target = board[row2][col2];
        return target == null || target.color != color;
    }

    private boolean enPassant(int row1, int col1, int row2, int col2) {
        ChessPiece pieceToMove = board[row1][col1];
        Color color = pieceToMove.color;

        if(pieceToMove.piece != Piece.PAWN) return false;

        if(color == Color.WHITE) {
            if(row1 != 4) return false;
            if(row2 != row1 + 1) return false;
            ChessPiece enPassantTarget = board[row1][col2];

            if(enPassantTarget != null && enPassantTarget.piece == Piece.PAWN && enPassantTarget.color != color) {
                return lastMove[0] == row1+2 && lastMove[1] == col2 && lastMove[2] == row1 && lastMove[3] == col2;
            }
        }
        else {
            if(row1 != 3) return false;
            if(row2 != row1 - 1) return false;
            ChessPiece enPassantTarget = board[row1][col2];

            if(enPassantTarget != null && enPassantTarget.piece == Piece.PAWN && enPassantTarget.color != color) {
                return lastMove[0] == row1-2 && lastMove[1] == col2 && lastMove[2] == row1 && lastMove[3] == col2;
            }
        }

        return false;
    }

    /*
     *   TODO: This is not complete, needs rules for when king is in check / moves through check / has moved
     * */
    private boolean castling(int row1, int col1, int row2, int col2) {
        ChessPiece pieceToMove = board[row1][col1];
        Color color = pieceToMove.color;

        if(pieceToMove.piece != Piece.KING) return false;

        int rowStep = abs(row1 - row2);
        int colStep = abs(col1 - col2);
        if(colStep != 2) return false;
        if(rowStep != 0) return false;

        int colDir = (col2-col1) < 0 ? -1 : 1;

        // Empty squares
        if(board[row1][col1+colDir] != null) return false;
        if(board[row1][col1+2*colDir] != null) return false;
        if(colDir == -1 && board[row1][col1+3*colDir] != null) return false;

        // Rooks
        if(colDir == -1 && board[row1][0] == null || !(board[row1][0].color == color && board[row1][0].piece == Piece.ROOK)) return false;
        if(colDir == 1 && board[row1][7] == null || !(board[row1][7].color == color && board[row1][7].piece == Piece.ROOK)) return false;

        return true;
    }

    private int abs(int i) {
        return i < 0 ? -i : i;
    }

    public void resign(String colorStr) {
		if (isGameEnded()) {
			return;
		}
		version++;

		Color resigner = Color.valueOf(colorStr.toUpperCase());
		if (resigner != turn) {
			return;
		}
		result = resigner == Color.WHITE ? GameResult.BLACK_WINS : GameResult.WHITE_WINS;
		resultComment = resigner + " resigns";
	}

	private boolean isGameEnded() {
		return result != null;
	}

    static class ChessPiece {
        private final Color color;
        private final Piece piece;

        public ChessPiece(Color color, Piece piece) {
            this.color = color;
            this.piece = piece;
        }

        @Override
        public String toString() {
        	return String.format("%s %s", color, piece);
        }
    }

    enum Color {
        WHITE,
        BLACK;

		Color opposite() {
			return this == WHITE ? BLACK : WHITE;
		}
    }
    enum Piece {
        PAWN,
        ROOK,
        KNIGHT,
        BISHOP,
        QUEEN,
        KING
    }

    enum GameResult {
    	DRAW("Draw"),
    	STALE_MATE("Stalemate"),
    	WHITE_WINS("White wins"),
    	BLACK_WINS("Black wins");

    	private String desc;

		private GameResult(String desc) {
			this.desc = desc;
		}

		@Override
		public String toString() {
			return desc;
		}
    }

    private class CachedBoard {
        private ChessPiece[][] board;
        private Color turn;
        private int[] lastMove;

        public CachedBoard(ChessPiece[][] board, Color turn, int[] lastMove) {
            // Clone board
            this.board = new ChessPiece[board.length][];
            for(int r = 0; r < board.length; r++) {
                this.board[r] = board[r].clone();
            }

            this.turn = turn;
            this.lastMove = lastMove.clone();
        }
    }
}