const ActionMode = Object.freeze({
   STROKE:   Symbol("stroke"),
   TEXT:  Symbol("text"),
   SHAPE: Symbol("shape")
});

const IsProduction = window.location.hostname !== "localhost";

let currentActionMode = ActionMode.STROKE;
let stompConnected = false;
let mouseDownId = -1;
let typingActive = false;
let mouseX = 0;
let mouseY = 0;
let canvasObject = $("#drawing-board")[0];
let canvasContext = canvasObject.getContext('2d');

const stompClient = new StompJs.Client({
   brokerURL: IsProduction ? 'ws://4.158.114.105/draw-websocket' : 'ws://localhost:8080/draw-websocket'
});

let strokes = {}

let drawStroke = (mX, mY, strokeWidth, colour) => {
   if (strokes[mX.toString() + ":" + mY.toString()]) return; // dont bother drawing if already stroke here

   canvasContext.fillStyle = colour;
   canvasContext.beginPath();
   canvasContext.arc(mX, mY, strokeWidth, 0, 2 * Math.PI);
   canvasContext.fill();

   strokes[mX.toString() + ":" + mY.toString()] = true;
}

let whileMouseDown = (event) => {
   if (currentActionMode === ActionMode.STROKE) {
      // draw stroke
      drawStroke(mouseX, mouseY, 5, "black");
   }
}

let onMouseUp = (event) => {
   if (event.button !== 0) return;

   if (mouseDownId !== -1) {
      clearInterval(mouseDownId);
      mouseDownId = -1;
   }
}

let onMouseDown = (event) => {
   if (event.button !== 0) return;

   if (mouseDownId === -1) mouseDownId = setInterval(whileMouseDown, 50);

   if (currentActionMode === ActionMode.STROKE) drawStroke(mouseX, mouseY, 5, "black");
}

let onMouseMoveInCanvas = (event) => {
   let cRect = canvasObject.getBoundingClientRect();

   mouseX = Math.round(event.clientX - cRect.left);
   mouseY = Math.round(event.clientY - cRect.top);
}

stompClient.onConnect = (frame) => {
   stompConnected = true;
   console.log("websocket connected");

   stompClient.subscribe("/topic/board-state", (inbound) => {
      console.log(inbound.body);
   })

   stompClient.subscribe("/topic/connected-users", (inbound) => {
      $("#user-count").html("Connected users: " + inbound.body);
   })

   stompClient.publish({destination: "/app/get-num-users"})
}

document.body.onmousedown = onMouseDown;
document.body.onmouseup = onMouseUp;
document.body.onmouseout = onMouseUp;
canvasObject.onmousemove = onMouseMoveInCanvas;

stompClient.activate();