id("gatherCall").addEventListener("click", function () {
    sendObject({"messageType": "Gather", "command": "Call"});
});

function updateGatherCallback(data) {
    insertCallback("gatherCallbackTable", data.params);
}
