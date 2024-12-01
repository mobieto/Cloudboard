const ActionModes = Object.freeze({
   STROKE:   Symbol("stroke"),
   TEXT:  Symbol("text"),
   SHAPE: Symbol("shape")
});

let currentActionMode = ActionModes.STROKE;

const stompClient = new StompJs.Client({
   brokerURL: 'ws://localhost:8080/draw-websocket'
});

stompClient.onConnect = (frame) => {
   console.log("Connected");

   stompClient.subscribe("/topic/board-state", (inbound) => {
      console.log(inbound.body);
   })
}

document.body.onmousedown = (event) => {
   if (event.button !== 0) return;

   console.log("Start stroke");
}

document.body.onmouseup = (event) => {
   if (event.button !== 0) return;

   console.log("End stroke");

   stompClient.publish({
      destination: "/app/draw-stroke",
      body: JSON.stringify({})
   });
}

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