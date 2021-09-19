package wass.chess;

public class ChessRoom {
    int movesMade = 0;
    String currentColor = "white";
    ChessBoard board = new ChessBoard();

    public String getCurrentColor() {
        return currentColor;
    }

    public int getMovesMade() {
        return movesMade;
    }

    public ChessBoard getChessBoard() {
        return board;
    }
}
