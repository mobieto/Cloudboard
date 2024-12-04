const ActionMode = Object.freeze({
   STROKE:   Symbol("stroke"),
   TEXT:  Symbol("text"),
   SHAPE: Symbol("shape")
});

const IsProduction = window.location.hostname !== "localhost";

let sessionId = "";

let currentActionMode = ActionMode.STROKE;
let stompConnected = false;
let mouseDownId = -1;
let typingActive = false;
let mouseX = 0;
let mouseY = 0;
let canvasObject = $("#drawing-board")[0];
let canvasContext = canvasObject.getContext('2d');

const stompClient = new StompJs.Client({
   brokerURL: IsProduction ? `ws://${window.location.hostname}/draw-websocket` : 'ws://localhost:8080/draw-websocket'
});

let strokes = {};
let strokeIdx = 0;
let prevStrokeMX = -1, prevStrokeMY = -1;
const SEND_STROKE_EVERY = 10;

let sendStrokeData = (mX, mY, strokeWidth, colour) => {
   try {
      stompClient.publish({
         destination: "/app/draw-stroke",
         body: JSON.stringify({
            coordX: mX,
            coordY: mY,
            action: 'stroke,' + strokeWidth.toString() + ',' + colour + ',' + prevStrokeMX.toString() + ',' + prevStrokeMY.toString()
         })
      })

      prevStrokeMX = mX;
      prevStrokeMY = mY;
   } catch (exception) {
      stompConnected = false;
   }
}

let drawStroke = (mX, mY, strokeWidth, colour, doSave) => {
   if (!stompConnected) return;
   if (strokes[mX.toString() + ":" + mY.toString()]) return; // dont bother drawing if already stroke here

   canvasContext.fillStyle = colour;
   canvasContext.beginPath();
   canvasContext.arc(mX, mY, strokeWidth, 0, 2 * Math.PI);
   canvasContext.fill();

   strokes[mX.toString() + ":" + mY.toString()] = true;

   if (!doSave) return;

   if (strokeIdx === 0 || strokeIdx % SEND_STROKE_EVERY === 0) {
      // send stroke data
      console.log(strokeIdx);
      sendStrokeData(mX, mY, strokeWidth, colour);
   }

   strokeIdx++;
}

let whileMouseDown = (event) => {
   if (currentActionMode === ActionMode.STROKE) {
      // draw stroke
      drawStroke(mouseX, mouseY, 5, "black", true);
   }
}

let onMouseUp = (event) => {
   if (event.button !== 0) return;

   if (mouseDownId !== -1) {
      clearInterval(mouseDownId);
      mouseDownId = -1;
      if (currentActionMode === ActionMode.STROKE) {
         strokeIdx = 0;
         drawStroke(mouseX, mouseY, 5, "black", true);
         strokeIdx = 0;
      }
   }
}

let onMouseDown = (event) => {
   if (event.button !== 0) return;

   if (mouseDownId === -1) {
      mouseDownId = setInterval(whileMouseDown, 1);
      if (currentActionMode === ActionMode.STROKE) drawStroke(mouseX, mouseY, 5, "black", true);
   }
}

let onMouseMoveInCanvas = (event) => {
   let cRect = canvasObject.getBoundingClientRect();

   mouseX = Math.round(event.clientX - cRect.left);
   mouseY = Math.round(event.clientY - cRect.top);
}

stompClient.onConnect = (frame) => {
   console.log("websocket connected");

   stompClient.subscribe("/user/topic/board-state", (inbound) => {
      const data = JSON.parse(inbound.body);
      //console.log(data);
   });

   stompClient.subscribe("/user/topic/connected-users", (inbound) => {
      $("#user-count").html("Connected users: " + inbound.body);
   });

   stompClient.subscribe("/user/topic/session", (inbound) => {
      sessionId = inbound.body;
      stompConnected = true;

      console.log(sessionId);
   });

   stompClient.subscribe("/topic/new-stroke", (inbound) => {
      const payload = JSON.parse(inbound.body);

      if (payload.excludedSessionId !== sessionId) {
         drawStroke(payload.data.coordX, payload.data.coordY, 5, "black", false);
      }
   });

   stompClient.publish({destination: "/app/get-num-users"});
   stompClient.publish({destination: "/app/get-board-state"});
   stompClient.publish({destination: "/app/get-session"});
}

document.body.onmousedown = onMouseDown;
document.body.onmouseup = onMouseUp;
document.body.onmouseout = onMouseUp;
canvasObject.onmousemove = onMouseMoveInCanvas;

stompClient.activate();