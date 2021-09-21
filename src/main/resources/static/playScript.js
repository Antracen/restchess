let boardVersion = -1;
let color;
let roomId;

let canvas;
let ctx;
let boardImage;
let gameFinished = false;
let pieceImages = {};
let move = [];

function init() {
    canvas = document.getElementById('canvas');
    ctx = canvas.getContext('2d');
    result = document.getElementById('result');

    canvas.addEventListener('mousedown', function(e) {
        handleMouseClick(canvas, e)
    })

    color = $("#color").attr("value");
    roomId = $("#roomId").attr("value");
    window.setInterval(pollBoard, 50);

    boardImage = new Image();
    boardImage.src = "board.svg";

    pieceImages['P'] = new Image();
    pieceImages['P'].src = "whitepawn.svg";
    pieceImages['p'] = new Image();
    pieceImages['p'].src = "blackpawn.svg";
    pieceImages['R'] = new Image();
    pieceImages['R'].src = "whiterook.svg";
    pieceImages['r'] = new Image();
    pieceImages['r'].src = "blackrook.svg";
    pieceImages['N'] = new Image();
    pieceImages['N'].src = "whiteknight.svg";
    pieceImages['n'] = new Image();
    pieceImages['n'].src = "blackknight.svg";
    pieceImages['B'] = new Image();
    pieceImages['B'].src = "whitebishop.svg";
    pieceImages['b'] = new Image();
    pieceImages['b'].src = "blackbishop.svg";
    pieceImages['Q'] = new Image();
    pieceImages['Q'].src = "whitequeen.svg";
    pieceImages['q'] = new Image();
    pieceImages['q'].src = "blackqueen.svg";
    pieceImages['K'] = new Image();
    pieceImages['K'].src = "whiteking.svg";
    pieceImages['k'] = new Image();
    pieceImages['k'].src = "blackking.svg";

}

function pollBoard() {
    $.get( "/boardVersion/" + roomId, function(data) {
        if(boardVersion != data) {
            boardVersion = data;
            $.get( "/board/" + roomId, function(data) {
                move = [];
                ctx.clearRect(0, 0, canvas.width, canvas.height);
                ctx.drawImage(boardImage, 0, 0, 800, 800);
                let board = data.board;
                let row = 0;
                let col = 0;
                for(let i = 0; i < board.length; i++) {
                    row = Math.floor(i / 8);
                    col = i % 8;

                    drawPiece(board.charAt(i), row, col);
                }
                
               
               	if (data.result != null) {
            		result.textContent = "Result: " + data.result + ", cause: " + data.resultComment;
            		gameFinished = true;
            	}
	        });
        }
    });
}

function drawPiece(piece, row, col) {
    if(piece == '-') return;

    if(color == "black") {
        row = 7 - row;
        col = 7 - col;
    }

    ctx.drawImage(pieceImages[piece], 0 + 100*col, canvas.height - 100 - 100*row, 100, 100);
}

function handleMouseClick(canvas, event) {
	if (gameFinished) {
		return;
	}
		
    const rect = canvas.getBoundingClientRect()
    const x = event.clientX - rect.left
    const y = event.clientY - rect.top

    let row = Math.floor((800-y) / 100);
    let col = Math.floor(x / 100);

    ctx.beginPath();
    ctx.lineWidth = "4";
    ctx.strokeStyle = "green";
    ctx.rect(0 + 100*col, canvas.height - 100 - 100*row, 100, 100);
    ctx.stroke();

    if(color == "black") {
        row = 7 - row;
        col = 7 - col;
    }

    if(move.length == 0) {
        move.push([row, col]);
    }
    else {
        move.push([row, col]);
        makeMove();
    }
}

function makeMove() {
    $.ajax({
        url: "/move/" + roomId + "/" + move[0][0] + "/" + move[0][1] + "/" + move[1][0] + "/" + move[1][1],
        type: "POST"
    });

    move = [];
}

function resign() {
	$.ajax({
        url: "/resign/" + roomId + "/" + color,
        type: "POST"
    });
}