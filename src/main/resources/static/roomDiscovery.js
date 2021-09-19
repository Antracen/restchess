let roomVersion = -1;

function createRoom() {
    $.post("/room");
}

function pollRooms() {
    $.get( "/roomsVersion", function(data) {
        if(roomVersion != data) {
            roomVersion = data;
            $.get( "/rooms", function(data) {
                populateRoomList(data);
            });
        }
    });
}

function populateRoomList(roomIds) {
    let roomsTable = $("#roomsTable");
    roomsTable.children().not(':first').remove()
    for(let id of roomIds) {
        let whiteLink = "<a href=\"/play?roomId=" + id + "&color=white\">Play!</a>";
        let blackLink = "<a href=\"/play?roomId=" + id + "&color=black\">Play!</a>";
        let deleteLink = "<button onClick=deleteRoom(\"" + id + "\");>Delete</button>";
        roomsTable.append("<tr><td>" + id + "</td><td>" + whiteLink + "</td><td>" + blackLink + "</td><td>" + deleteLink + "</td></tr>");
    }
}

function deleteRoom(room) {
    $.ajax({
        url: "/room/" + room,
        type: "DELETE"
    });
}

window.setInterval(pollRooms, 50);
