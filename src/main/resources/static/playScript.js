let boardVersion = -1;
let color;
let roomId;

let canvas = [];
let ctx = [];
let boardImage;
let gameFinished = false;
let pieceImages = {};
let pieces = [];
let moveHistoryParagraph = [];
let lastMove = [0,0];

class MouseMovementTracker {
    constructor() {
        this.from = [];
        this.to = [];
        this.intermediate = [];
        this.selectedPiece = null;
    }

    clear() {
        this.from = [];
        this.to = [];
        this.intermediate = [];
        this.selectedPiece = null;
    }

    setFrom(from) {
        this.from = from;
        this.to = [];
        this.intermediate = [];
        this.selectedPiece = pieces[from[0]*8 + from[1]];
    }

    setTo(to) {
        this.to = to;
    }

    setIntermediate(intermediate) {
        if(this.from.length == 0) return false;
        if(this.intermediate[0] != intermediate[0] || this.intermediate[1] != intermediate[1]) {
            this.intermediate = intermediate;
            return true;
        }
        return false;
    }
}

let mouseMovementTracker = new MouseMovementTracker();

function init() {
    canvas = [
        document.getElementById('canvas'),
        document.getElementById('canvas2'),
        document.getElementById('canvas3'),
        document.getElementById('canvas4')
    ];
    for(let i = 0; i < canvas.length; i++) {
        ctx.push(canvas[i].getContext('2d'));
        if(i == canvas.length-1) {
            canvas[i].addEventListener('mousedown', function(e) {
                handleMouseClick(canvas[2], e)
            })

            canvas[i].addEventListener('mouseup', function(e) {
                handleMouseUp(canvas[2], e)
            })

            canvas[i].addEventListener('mousemove', function(e) {
                handleMouseMove(canvas[2], e)
            })
        }
    }

    result = document.getElementById('result');

    window.addEventListener('keydown', function(event) {
        if(event.key == 'z') {

            $.get( "/undoMove/" + roomId, function(data) {});
        }
    });

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
            let wasUndo = boardVersion > data;
            console.log(data);
            boardVersion = data;
            $.get( "/board/" + roomId, function(data) {
                mouseMovementTracker.clear();
                ctx[0].clearRect(0, 0, canvas[0].width, canvas[0].height);
                ctx[1].clearRect(0, 0, canvas[0].width, canvas[0].height);
                ctx[3].clearRect(0, 0, canvas[0].width, canvas[0].height);
                ctx[0].drawImage(boardImage, 0, 0, 800, 800);
                let board = data.board;
                let row = 0;
                let col = 0;
                for(let i = 0; i < board.length; i++) {
                    pieces[i] = board.charAt(i);
                }

                if (data.result != null) {
                    result.textContent = "Result: " + data.result + ", cause: " + data.resultComment;
                    gameFinished = true;
                }
                else {
                    lastMove = data.lastMove;
                    if(!(lastMove[0] == 0 && lastMove[1] == 0 && lastMove[2] == 0 && lastMove[3] == 0)) {
                        let algebraicLastMove = data.algebraicLastMove;
                        if(wasUndo) moveHistoryParagraph.pop();
                        else moveHistoryParagraph.push(algebraicLastMove);

                        let movehistory = document.getElementById("movehistory");

                        let moveHistoryText = "";
                        let white = true;
                        for(let historicMove of moveHistoryParagraph) {
                            if(white) historicMove = historicMove.padEnd(10);
                            else historicMove += "\n";
                            moveHistoryText += historicMove;
                            white = !white;
                        }
                        movehistory.textContent = moveHistoryText;
                    }
                    else {
                        moveHistoryParagraph.length = 0;
                        document.getElementById("movehistory").textContent = "";
                    }
                }

                drawAllPiecesAndLastMove();
            });
        }
    });
}

function drawAllPiecesAndLastMove() {
    ctx[2].clearRect(0, 0, canvas[0].width, canvas[0].height);
    for(let i = 0; i < pieces.length; i++) {
        row = Math.floor(i / 8);
        col = i % 8;

        drawPiece(pieces[i], row, col, 2);
    }

    let x1 = lastMove[1];
    let y1 = lastMove[0];
    let x2 = lastMove[3];
    let y2 = lastMove[2];

    if(!(x1 == 0 && x2 == 0 && y1 == 0 && y2 == 0)) {
        if(color == "black") {
            x1 = 7 - x1;
            y1 = 7 - y1;
            x2 = 7 - x2;
            y2 = 7 - y2;
        }
        drawSquare(x1, y1);
        drawSquare(x2, y2);
    }
}

function drawSquare(x, y) {
    ctx[1].lineWidth = "4";
    ctx[1].fillStyle = "rgba(96, 206, 40, 0.5)";
    ctx[1].beginPath();
    ctx[1].rect(0 + 100 * x, canvas[0].height - 100 - 100 * y, 100, 100);
    ctx[1].fill();
}

function clearPiece(y, x) {
    ctx[2].clearRect(0 + 100 * x, canvas[0].height - 100 - 100 * y, 100, 100);
}

function drawPiece(piece, row, col, layer) {
    if(piece == '-') return;

    if(color == "black") {
        row = 7 - row;
        col = 7 - col;
    }

    ctx[layer].drawImage(pieceImages[piece], 100*col, canvas[0].height - 100 - 100*row, 100, 100);
}

function drawIntermediaryPiece(piece, x, y) {
    ctx[3].drawImage(pieceImages[piece], x-50, y-50, 100, 100);
}

function getMouseCoords(event) {
    const rect = canvas[0].getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;

    return [x,y];
}

function getMouseRowAndCol(event) {
    const [x,y] = getMouseCoords(event);

    let row = Math.floor((800-y) / 100);
    let col = Math.floor(x / 100);

    return [row, col];
}

function handleMouseClick(canvas, event) {
	if (gameFinished) {
		return;
	}

    [row, col] = getMouseRowAndCol(event);

    if(color == "black") {
        row = 7 - row;
        col = 7 - col;
    }

    mouseMovementTracker.setFrom([row, col]);
}

function handleMouseMove(canvas, event) {
    if(gameFinished) return;

    if(mouseMovementTracker.from.length == 0) return;


    [row, col] = getMouseRowAndCol(event);
    [fromRow, fromCol] = mouseMovementTracker.from;

    if(color == "black") {
        row = 7 - row;
        col = 7 - col;
        fromRow = 7 - fromRow;
        fromCol = 7 - fromCol;
    }

    clearPiece(fromRow, fromCol);
    mouseMovementTracker.setIntermediate([row, col]);
    ctx[3].clearRect(0, 0, canvas.width, canvas.height);
    drawIntermediaryPiece(mouseMovementTracker.selectedPiece, ...getMouseCoords(event));
}

function handleMouseUp(canvas, event) {
    if(gameFinished) return;

    ctx[1].clearRect(0, 0, canvas.width, canvas.height);
    ctx[3].clearRect(0, 0, canvas.width, canvas.height);
    [row, col] = getMouseRowAndCol(event);

    if(color == "black") {
        row = 7 - row;
        col = 7 - col;
    }

    mouseMovementTracker.setTo([row, col])
    makeMove();
}

function makeMove() {
    $.ajax({
        url: "/move/" + roomId + "/" + mouseMovementTracker.from[0] + "/" + mouseMovementTracker.from[1] + "/" + mouseMovementTracker.to[0] + "/" + mouseMovementTracker.to[1],
        type: "POST"
    });

    mouseMovementTracker.clear();
    drawAllPiecesAndLastMove();
}

function resign() {
	$.ajax({
        url: "/resign/" + roomId + "/" + color,
        type: "POST"
    });
}