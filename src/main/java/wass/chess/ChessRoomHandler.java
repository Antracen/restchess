package wass.chess;

import java.util.*;

public class ChessRoomHandler {

    long version = 0; // Whenever rooms changes we increment the version
    Map<UUID, ChessRoom> rooms;

    public ChessRoomHandler() {
        rooms = new HashMap<>();
    }

    public UUID createRoom() {
        UUID roomId = UUID.randomUUID();

        if(rooms.containsKey(roomId)) {
            // This should basically be impossible, but fuck it you never know
            return createRoom();
        }
        rooms.put(roomId, new ChessRoom());
        version++;

        return roomId;
    }

    public ChessRoom getRoom(UUID roomId) {
        return rooms.get(roomId);
    }

    public Set<UUID> getRooms() {
        return rooms.keySet();
    }

    public int getNumRooms() {
        return rooms.size();
    }

    public long getVersion() {
        return version;
    }

    public void deleteRoom(UUID roomId) {
        version++;
        rooms.remove(roomId);
    }

    public ChessBoard getBoard(UUID roomId) {
        ChessRoom room = getRoom(roomId);
        if(room != null) return getRoom(roomId).getChessBoard();
        return null;
    }

    public Long getBoardVersion(UUID roomId) {
        ChessBoard board = getBoard(roomId);
        if(board != null) return getBoard(roomId).getVersion();
        return null;
    }

    public void move(UUID roomId, int x1, int y1, int x2, int y2) {
        ChessBoard board = getBoard(roomId);
        if(board != null) {
            board.move(x1, y1, x2, y2);
        }
    }

    public void undoMove(UUID roomId) {
        ChessBoard board = getBoard(roomId);
        if(board != null) {
            board.undoMove();
        }
    }

    public void resign(UUID roomId, String color) {
		ChessBoard board = getBoard(roomId);
        if(board != null) {
            board.resign(color);
        }
	}
}
