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
const SEND_STROKE_EVERY = 7;

let sendStrokeData = (mX, mY, strokeWidth, colour) => {
   try {
      stompClient.publish({
         destination: "/app/draw-stroke",
         body: JSON.stringify({
            x: mX,
            y: mY,
            action: 'stroke,' + strokeWidth.toString() + ',' + colour + ',' + prevStrokeMX.toString() + ',' + prevStrokeMY.toString()
         })
      })

      prevStrokeMX = mX;
      prevStrokeMY = mY;
   } catch (exception) {
      stompConnected = false;
   }
}

let drawStroke = (mX, mY, strokeWidth, colour, doSave, prevX, prevY) => {
   if (!stompConnected) return;
   if (strokes[mX.toString() + ":" + mY.toString()]) return; // dont bother drawing if already stroke here

   canvasContext.fillStyle = colour;
   canvasContext.beginPath();
   canvasContext.arc(mX, mY, strokeWidth, 0, 2 * Math.PI);
   canvasContext.fill();
   canvasContext.closePath();

   strokes[mX.toString() + ":" + mY.toString()] = true;

   if (prevX && prevY && prevX !== -1 && prevY !== -1) { // draw line to connect to previous point
      canvasContext.beginPath();
      canvasContext.lineWidth = strokeWidth * 2;
      canvasContext.moveTo(mX, mY);
      canvasContext.lineTo(prevX, prevY);
      canvasContext.stroke();
      canvasContext.closePath();
   }

   if (!doSave) return;

   if (strokeIdx === 0 || strokeIdx % SEND_STROKE_EVERY === 0) // send stroke data
      sendStrokeData(mX, mY, strokeWidth, colour);

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
      if (currentActionMode === ActionMode.STROKE) {
         prevStrokeMY = -1;
         prevStrokeMX = -1;
         drawStroke(mouseX, mouseY, 5, "black", true);
      }
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
      const actions = JSON.parse(inbound.body);

      for (let action of actions) {
         action = JSON.parse(action);

         let actionData = action.action.split(',');
         let strokeWidth = actionData[1];
         let colour = actionData[2];
         let prevX = parseInt(actionData[3]);
         let prevY = parseInt(actionData[4]);

         drawStroke(action.x, action.y, strokeWidth, colour, false, prevX, prevY);
      }
   });

   stompClient.subscribe("/topic/connected-users", (inbound) => {
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
         let actionData = payload.data.action.split(',');
         let strokeWidth = actionData[1];
         let colour = actionData[2];
         let prevX = parseInt(actionData[3]);
         let prevY = parseInt(actionData[4]);

         drawStroke(payload.data.x, payload.data.y, strokeWidth, colour, false, prevX, prevY);
      }
   });

   stompClient.publish({destination: "/app/get-session"});
   stompClient.publish({destination: "/app/get-num-users"});
   stompClient.publish({destination: "/app/get-board-state"});
}

document.body.onmousedown = onMouseDown;
document.body.onmouseup = onMouseUp;
document.body.onmouseout = onMouseUp;
canvasObject.onmousemove = onMouseMoveInCanvas;

stompClient.activate();