//Establish the WebSocket connection and set up event handlers
var webSocket = new WebSocket("ws://" + location.hostname + ":" + location.port + "/websocket");
webSocket.onmessage = function (msg) {
    dispatchCallback(msg);
};

webSocket.onclose = function () {
    alert("WebSocket connection closed")
};

webSocket.onopen = function () {
    sendInit()
    loadCalls();
    loadConferences();
    loadParticipants();
};

function sendObject(objectToSend) {
    webSocket.send(JSON.stringify(objectToSend));
}

function sendInit() {
    var init = {"messageType": "init"};
    sendObject(init);
}

function dispatchCallback(msg) {
    var data = JSON.parse(msg.data);
    if (data.callbackType === "gatherCallback") {
        updateGatherCallback(data);
    }
    if (data.callbackType === "payCallback") {
        updatePayCallback(data);
    }
    if (data.callbackType === "callStatusCallback") {
        updateCallStatus(data);
    }
    if (data.callbackType === "allCalls") {
        loadAllCalls(data);
    }
    if (data.callbackType === "conferenceCallback") {
        updateConferenceStatus(data);
    }
    if (data.callbackType === "allConferences") {
        loadAllConferences(data);
    }
    if (data.callbackType === "allParticipants") {
        loadAllParticipants(data);
    }
    if (data.callbackType === "participantCallback") {
        updateParticipantStatus(data);
    }
}

function insertCallback(targetId, params) {
    id(targetId).insertAdjacentHTML("afterbegin", getHTMLForParams(params));
}

function getHTMLForParams(params) {
    var result = "";
    for (var name in params) {
        result += makeListItem(name, params[name]);
    }
    return result;
}

function makeListItem(key, value) {
    var result = "";
    result += "<tr><td>" + key + "</td><td>" + value + "</td></tr>";
    return result;
}

//Helper function for selecting element by id
function id(id) {
    return document.getElementById(id);
}