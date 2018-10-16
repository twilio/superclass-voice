id("payCall").addEventListener("click", function () {
    sendObject({"messageType": "Pay", "command": "Call"});
});

function updatePayCallback(data) {
    insertCallback("payCallbackTable", data.params);
}
