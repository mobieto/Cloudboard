const ActionMode = Object.freeze({
   STROKE: "stroke",
   TEXT: "text",
   SHAPE: "shape"
});

const Shapes = Object.freeze({
   SQUARE: "square",
})

const IsProduction = window.location.hostname !== "localhost";
const SEND_STROKE_EVERY = 5;

let sessionId = "";
let currentTypedText = "";
let currentActionMode = ActionMode.STROKE;
let stompConnected = false;
let typingActive = false;
let mouseInCanvas = false;
let drawingShape = false;
let startMX = -1, startMY = -1;
let mouseDownId = -1;
let typingId = -1;
let mouseX = 0, mouseY = 0;
let strokeIdx = 0;
let prevStrokeMX = -1, prevStrokeMY = -1;
let strokes = {};

let canvasReady = true;

const canvasObject = $("#canvas-main")[0];
const canvasTempObject = $("#canvas-temp")[0];
const canvasContext = canvasObject.getContext('2d');
const canvasTempContext = canvasTempObject.getContext('2d');

const stompClient = new StompJs.Client({
   brokerURL: IsProduction ? `ws://${window.location.hostname}/draw-websocket` : 'ws://localhost:8080/draw-websocket'
});

let getRectProps = () => {
   let nowMX = mouseX;
   let nowMY = mouseY;
   let width = Math.abs(nowMX - startMX);
   let height = Math.abs(nowMY - startMY);

   let originX = startMX;
   let originY = startMY;

   if (nowMX < startMX) originX = startMX - width;
   if (nowMY < startMY) originY = startMY - height;

   return [originX, originY, width, height];
}

let sendStrokeData = (x, y, strokeWidth, colour) => {
   try {
      stompClient.publish({
         destination: "/app/draw-stroke",
         body: JSON.stringify({
            x: x,
            y: y,
            action: 'stroke,' + strokeWidth.toString() + ',' + colour + ',' + prevStrokeMX.toString() + ',' + prevStrokeMY.toString()
         })
      })

      prevStrokeMX = x;
      prevStrokeMY = y;
   } catch (exception) {
      stompConnected = false;
   }
}

let sendShapeData = (x, y, shape, width, height) => {
   try {
      stompClient.publish({
         destination: "/app/draw-shape",
         body: JSON.stringify({
            x: x,
            y: y,
            action: 'shape,' + shape + ',' + width.toString() + ',' + height.toString()
         })
      })
   } catch (exception) {
      stompConnected = false;
   }
}

let sendTextData = (x, y, text) => {
   try {
      stompClient.publish({
         destination: "/app/draw-text",
         body: JSON.stringify({
            x: x,
            y: y,
            action: 'text,' + text
         })
      })
   } catch (exception) {
      stompConnected = false;
   }
}

let thisPrevX = -1, thisPrevY = -1;

let connectLine = (x1, y1, x2, y2, strokeWidth) => {
   canvasContext.beginPath();
   canvasContext.lineWidth = strokeWidth * 2;
   canvasContext.moveTo(x1, y1);
   canvasContext.lineTo(x2, y2);
   canvasContext.stroke();
   canvasContext.closePath();
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
      connectLine(mX, mY, prevX, prevY, strokeWidth);
   }

   if (!doSave) return;

   if (thisPrevX !== -1 && thisPrevY !== -1) {
      connectLine(mX, mY, thisPrevX, thisPrevY, strokeWidth);
   }

   thisPrevX = mX;
   thisPrevY = mY;

   if (strokeIdx === 0 || strokeIdx % SEND_STROKE_EVERY === 0) // send stroke data
      sendStrokeData(mX, mY, strokeWidth, colour);

   strokeIdx++;
}

let drawShape = (x, y, shape, width, height, doSave) => {
   canvasContext.beginPath();
   canvasContext.lineWidth = 5;
   canvasContext.rect(x, y, width, height);
   canvasContext.stroke();
   canvasContext.closePath();

   if (!doSave) return;

   sendShapeData(x, y, shape, width, height);
}

let drawText = (x, y, text, doSave) => {
   if (!text) return; // dont draw empty text

   canvasContext.beginPath();
   canvasContext.font = "20px Arial";
   canvasContext.fillText(text, x, y);
   canvasContext.closePath();

   if (!doSave) return;

   sendTextData(x, y, text);
}

let drawShapePreview = (x, y, shape, width, height) => {
   if (shape === Shapes.SQUARE) {
      canvasTempContext.clearRect(0, 0, canvasTempObject.width, canvasTempObject.height);

      canvasTempContext.beginPath();
      canvasTempContext.lineWidth = 5;
      canvasTempContext.rect(x, y, width, height);
      canvasTempContext.stroke();
      canvasTempContext.closePath();
   }
}

let typingHintCounter = 0;
let drawTextPreview = (x, y, text) => {
   canvasTempContext.clearRect(0, 0, canvasTempObject.width, canvasTempObject.height);

   canvasTempContext.beginPath();
   canvasTempContext.font = "20px Arial";
   canvasTempContext.fillText(text + (typingHintCounter < 100 ? "" : "|"), x, y);
   canvasTempContext.closePath();

   typingHintCounter++
   if (typingHintCounter > 200) typingHintCounter = 0;
}

let whileMouseDown = () => {
   if (currentActionMode === ActionMode.STROKE) {
      // draw stroke
      drawStroke(mouseX, mouseY, 5, "black", true);
   } else if (currentActionMode === ActionMode.SHAPE) {
      const [originX, originY, width, height] = getRectProps();
      drawShapePreview(originX, originY, Shapes.SQUARE, width, height);
   }
}

let whileTyping = () => {
   drawTextPreview(startMX, startMY, currentTypedText);
}

let stopTyping = (x, y) => {
   typingActive = false;
   drawText(x, y, currentTypedText, true);
   currentTypedText = "";
   canvasTempContext.clearRect(0, 0, canvasTempObject.width, canvasTempObject.height);

   if (typingId !== -1) {
      clearInterval(typingId);
      typingId = -1;
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
         thisPrevX = -1;
         thisPrevY = -1;
      } else if (currentActionMode === ActionMode.SHAPE && drawingShape) {
         const [originX, originY, width, height] = getRectProps();

         drawShape(originX, originY, Shapes.SQUARE, width, height, true);

         drawingShape = false;
         canvasTempContext.clearRect(0, 0, canvasTempObject.width, canvasTempObject.height);
      }
   }
}

let onMouseDown = (event) => {
   if (!canvasReady) return;
   if (event.button !== 0 || !mouseInCanvas) return;

   let oldMX = startMX;
   let oldMY = startMY;

   startMX = mouseX;
   startMY = mouseY;

   if (mouseDownId === -1) {
      mouseDownId = setInterval(whileMouseDown, 1);

      if (currentActionMode === ActionMode.STROKE) {
         prevStrokeMY = -1;
         prevStrokeMX = -1;
         drawStroke(mouseX, mouseY, 5, "black", true);
      } else if (currentActionMode === ActionMode.SHAPE) {
         drawingShape = true;
      } else if (currentActionMode === ActionMode.TEXT) {
         if (typingActive) {
            stopTyping(oldMX, oldMY);
         } else {
            typingActive = true;
            currentTypedText = "";

            if (typingId === -1) typingId = setInterval(whileTyping, 1);
         }
      }
   }
}

let onKeyDown = (event) => {
   if (!canvasReady) return;
   if (!typingActive) return;

   const key = event.key;
   const isAlphanumericOrSpecial = /^[a-zA-Z0-9!@#$£%^&*()_+¬\-=[\]{};':"\\|,.<>/?~` ]$/.test(key);

   if (!isAlphanumericOrSpecial && key !== "Backspace" && key !== "Enter") return;

   if (key === "Backspace") {
      currentTypedText = currentTypedText.substring(0, currentTypedText.length - 1);
   } else if (key === "Enter") {
      if (typingActive) stopTyping(startMX, startMY);
   } else {
      currentTypedText += key;
   }
}

let onMouseMoveInCanvas = (event) => {
   let cRect = canvasObject.getBoundingClientRect();

   mouseX = Math.round(event.clientX - cRect.left);
   mouseY = Math.round(event.clientY - cRect.top);
}

stompClient.onConnect = () => {
   console.log("websocket connected");

   stompClient.subscribe("/user/topic/board-state", (inbound) => {
      const actions = JSON.parse(inbound.body);

      for (let actionObj of actions) {
         actionObj = JSON.parse(actionObj);

         let actionData = actionObj.action.split(',');
         let action = actionData[0];

         if (action === ActionMode.STROKE) {
            let [, strokeWidth, colour, prevX, prevY] = actionData;

            drawStroke(actionObj.x, actionObj.y, strokeWidth, colour, false, parseInt(prevX), parseInt(prevY));
         } else if (action === ActionMode.SHAPE) {
            let [, shape, width, height] = actionData;

            drawShape(actionObj.x, actionObj.y, shape, width, height, false);
         } else if (action === ActionMode.TEXT) {
            let [, ...text] = actionData;
            text = text.join(',');

            drawText(actionObj.x, actionObj.y, text, false);
         }
      }
   });

   stompClient.subscribe("/topic/connected-users", (inbound) => {
      $("#user-count").html("Connected users: " + inbound.body);
   });

   stompClient.subscribe("/user/topic/session", (inbound) => {
       let [ sessionId , hostId ] = JSON.parse(inbound.body);
       stompConnected = true;
       console.log("Session ID: " + sessionId + " | Host ID: " + hostId);
   });

   stompClient.subscribe("/topic/new-stroke", (inbound) => {
      const payload = JSON.parse(inbound.body);

      if (payload.excludedSessionId !== sessionId) {
         let [, strokeWidth, colour, prevX, prevY] = payload.data.action.split(',');

         drawStroke(payload.data.x, payload.data.y, strokeWidth, colour, false, parseInt(prevX), parseInt(prevY));
      }
   });

   stompClient.subscribe("/topic/new-shape", (inbound) => {
      const payload = JSON.parse(inbound.body);

      if (payload.excludedSessionId !== sessionId) {
         let [, shape, width, height] = payload.data.action.split(',');

         drawShape(payload.data.x, payload.data.y, shape, width, height, false);
      }
   });

   stompClient.subscribe("/topic/new-text", (inbound) => {
      const payload = JSON.parse(inbound.body);

      if (payload.excludedSessionId !== sessionId) {
         let [, ...text] = payload.data.action.split(',');
         text = text.join(',');

         drawText(payload.data.x, payload.data.y, text, false);
      }
   });

   stompClient.subscribe("/topic/clear-board", (inbound) => {
      const payload = JSON.parse(inbound.body);

      if (payload === true) {
         canvasContext.clearRect(0, 0, canvasObject.width, canvasObject.height)
         canvasReady = true;
         $("#clear-canvas-text").addClass("invisible");
      } else {
         canvasReady = false;
         $("#clear-canvas-text").removeClass("invisible");
      }
   });

   stompClient.publish({destination: "/app/get-session"});
   stompClient.publish({destination: "/app/get-num-users"});
   stompClient.publish({destination: "/app/get-board-state"});
}

document.body.onmousemove = (event) => {
   let cRect = canvasObject.getBoundingClientRect();

   mouseInCanvas = (event.clientX >= cRect.left && event.clientX <= cRect.right &&
       event.clientY >= cRect.top && event.clientY <= cRect.bottom);
}

document.body.onmousedown = onMouseDown;
document.body.onmouseup = onMouseUp;
document.body.onmouseout = onMouseUp;
document.body.onkeydown = onKeyDown;
canvasObject.onmousemove = onMouseMoveInCanvas;

$("#stroke-button").click(() => {
   if (typingActive || drawingShape) return;

   currentActionMode = ActionMode.STROKE;
   $("#current-action").html("Brush");
});

$("#shape-button").click(() => {
   if (typingActive || drawingShape) return;

   currentActionMode = ActionMode.SHAPE;
   $("#current-action").html("Shape");
});

$("#text-button").click(() => {
   if (typingActive || drawingShape) return;

   currentActionMode = ActionMode.TEXT;
   $("#current-action").html("Text");
});

$("#clear-button").click(() => {
   if (!canvasReady) return;

   stompClient.publish({destination: "/app/clear-board"});
});

stompClient.activate();