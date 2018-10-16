var callMap = new Map();

function updateCallStatus(data) {
    if (data.params.CallStatus == "ringing" || data.params.CallStatus == "in-progress" || data.params.CallStatus == "answered") {
        callMap.set(data.params.callSid, data.params.CallStatus);
        insertCall("callStatusTable", data.params);
    }
    if (data.params.CallStatus == "completed" || data.params.CallStatus == "failed") {
        callMap.set(data.params.callSid, data.params.CallStatus);
        removeCall("callStatusTable", data.params);
    }
}

function loadAllCalls(data) {
    for (var i = 0; i < data.allCalls.length; i++) {
        callMap.set(data.allCalls[i].params.callSid, data.allCalls[i].params.CallStatus);
        insertCall("callStatusTable", data.allCalls[i].params);
    }
}

function loadCalls() {
    sendObject({"messageType": "Call", "command": "Load"});
}

function insertCall(id, data) {
    var table = $("#callStatusTable");
    var hangupButton = '<a class="btn btn-info" id ="hangup_' + data.CallSid + '">Hangup</a> ';
    var existingRow = $("#row_" + String(data.CallSid));
    if (existingRow.length == 0) {
        var rowData = '<tr id ="row_' + data.CallSid + '"><td>' + data.CallSid + '</td><td id ="callstatus_' + data.CallSid + '">' + data.CallStatus + '</td><td>' + data.From + '</td><td>' + data.To + '</td><td>' + hangupButton + '</td>';
        table.append(rowData);
        var hangup = $("#hangup_" + String(data.CallSid));
        hangup.click(function (obj) {
            sendObject({"messageType": "Call", "command": "Hangup", "CallSid": obj.target.id.toString()});
        });
    } else {
        var status = $("#callstatus_" + String(data.CallSid));
        status[0].textContent = String(data.CallStatus);

    }
}

function removeCall(id, data) {
    var hangup = $("#hangup_" + String(data.CallSid));
    hangup.addClass("disabled");
    var status = $("#callstatus_" + String(data.CallSid));
    status[0].textContent = String(data.CallStatus);
}
