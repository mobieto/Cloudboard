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
let canvasMouseX = 0;
let canvasMouseY = 0;
let canvasObject = $("#drawing-board")[0];
let canvasContext = canvasObject.getContext('2d');

const stompClient = new StompJs.Client({
   brokerURL: IsProduction ? 'ws://4.158.114.105/draw-websocket' : 'ws://localhost:8080/draw-websocket'
});

let whileMouseDown = (event) => {
   if (currentActionMode === ActionMode.STROKE) {
      // draw stroke
      canvasContext.fillStyle = "black";
      canvasContext.beginPath();
      canvasContext.arc(canvasMouseX, canvasMouseY, 5, 0, 2 * Math.PI);
      canvasContext.fill();
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
}

let onMouseMoveInCanvas = (event) => {
   let cRect = canvasObject.getBoundingClientRect();

   canvasMouseX = Math.round(event.clientX - cRect.left);
   canvasMouseY = Math.round(event.clientY - cRect.top);
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