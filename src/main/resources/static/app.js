const ActionModes = Object.freeze({
   STROKE:   Symbol("stroke"),
   TEXT:  Symbol("text"),
   SHAPE: Symbol("shape")
});

const IsProduction = window.location.hostname !== "localhost";

let currentActionMode = ActionModes.STROKE;
let stompConnected = false;
let mouseDownId = -1;
let typingActive = false;
let canvasMouseX = 0;
let canvasMouseY = 0;
let canvasObject = $("#drawing-board");
let canvasContext = canvasObject.getContext('2d');

const stompClient = new StompJs.Client({
   brokerURL: IsProduction ? 'ws://4.158.114.105/draw-websocket' : 'ws://localhost:8080/draw-websocket'
});

let whileMouseDown = (event) => {
   console.log(canvasMouseX, canvasMouseY);
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

   if (mouseDownId === -1) setInterval(whileMouseDown, 100);
}

let onMouseMoveInCanvas = (event) => {
   let cRect = canvasObject.getBoundingClientRect();

   canvasMouseX = Math.round(event.clientX - cRect.left);
   canvasMouseY = Math.round(event.clientY - cRect.top);
}

stompClient.onConnect = (frame) => {
   stompConnected = true;
   console.log("Connected");

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

onkeydown = (event) => {
   console.log(event.key);

   if (event.key === "f") {
      console.log("Texting");

      stompClient.publish({
         destination: "/app/draw-text",
         body: JSON.stringify({})
      })
   } else if (event.key === "r") {
      stompClient.publish({
         destination: "/app/get-board-state",
         body: JSON.stringify({})
      })
   }
}

stompClient.activate();