package wass.chess;

import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
public class ChessController {

    private ChessRoomHandler chessRoomHandler = new ChessRoomHandler();

    @GetMapping("/ping")
    String ping() {
        return "pong";
    }

    @PostMapping("/room")
    UUID createRoom() {
        return chessRoomHandler.createRoom();
    }

    @GetMapping("/rooms")
    Set<UUID> getRooms() {
        return chessRoomHandler.getRooms();
    }

    @GetMapping("/roomsVersion")
    long getRoomsVersion() {
        return chessRoomHandler.getVersion();
    }

    @GetMapping("/room/{id}")
    ChessRoom getRoom(@PathVariable("id") UUID roomId) {
        return chessRoomHandler.getRoom(roomId);
    }

    @DeleteMapping("/room/{id}")
    void deleteRoom(@PathVariable("id") UUID roomId) {
        chessRoomHandler.deleteRoom(roomId);
    }

    @GetMapping("/roomCount")
    int numRooms() {
        return chessRoomHandler.getNumRooms();
    }

    @GetMapping("/boardVersion/{id}")
    Long getBoardVersion(@PathVariable("id") UUID roomId) {
        return chessRoomHandler.getBoardVersion(roomId);
    }

    @GetMapping("/board/{id}")
    ChessBoard getBoard(@PathVariable("id") UUID roomId) {
        return chessRoomHandler.getBoard(roomId);
    }

    @PostMapping("/move/{roomId}/{x1}/{y1}/{x2}/{y2}")
    void makeMove(@PathVariable("roomId") UUID roomId, @PathVariable("x1") int x1, @PathVariable("y1") int y1, @PathVariable("x2") int x2, @PathVariable("y2") int y2) {
        chessRoomHandler.move(roomId, x1, y1, x2, y2);
    }

}
