var conferenceMap = new Map();

function updateConferenceStatus(data) {
    if (data.params.Status == "start") {
        conferenceMap.set(data.params.ConferenceSid, data.params.Status);
        insertConference(data.params);
    }
    if (data.params.Status == "stop") {
        conferenceMap.set(data.params.ConferenceSid, data.params.Status);
        removeConference(data.params);
    }
}

//Update the chat-panel, and the list of connected users
function updateParticipantStatus(data) {
    if (data.params.Status == "join") {
        insertParticipant(data.params);
    }
    if (data.params.Status == "leave") {
        removeParticipant(data.params);
    }
}

function insertConference(data) {
    var table = $("#conferenceStatusTable");
    var hangupButton = '<a class="btn btn-info" id ="hangup_' + data.ConferenceSid + '">Hangup</a> ';
    var existingRow = $("#row_" + String(data.ConferenceSid));
    if (existingRow.length == 0) {
        var rowData = '<tr id ="row_' + data.ConferenceSid + '"><td>' + data.FriendlyName + '</td><td>' + data.ConferenceSid + '</td><td id ="conferencestatus_' + data.ConferenceSid + '">' + data.Status + '</td><td>' + hangupButton + '</td>';
        table.append(rowData);
        var hangup = $("#hangup_" + String(data.ConferenceSid));
        hangup.click(function (obj) {
            sendObject({"messageType": "Conference", "command": "Hangup", "Conference": obj.target.id.toString()});
        });
    } else {
        var status = $("#conferencestatus_" + String(data.ConferenceSid));
        status[0].textContent = String(data.Status);
    }
}


function insertParticipant(data) {
    var table = $("#participantStatusTable");
    var hangupButton = '<a class="btn btn-info" id ="hangupparticipant_' + data.CallSid + '">Hangup</a> ';
    var existingRow = $("#rowparticipant_" + String(data.CallSid));
    if (existingRow.length == 0) {
        var rowData = '<tr id ="rowparticipant_' + data.CallSid + '"><td>' + data.ConferenceSid + '</td><td>' + data.CallSid + '</td><td>' + data.DisplayName + '</td><td id ="participantstatus_' + data.CallSid + '">' + data.Status + '</td><td>' + hangupButton + '</td>';
        table.append(rowData);
        var hangup = $("#hangupparticipant_" + String(data.CallSid));
        hangup.click(function (obj) {
            sendObject({"messageType": "Conference", "command": "KickParticipant", "Call": obj.target.id.toString()});
        });
    } else {
        var status = $("#participantstatus_" + String(data.CallSid));
        status[0].textContent = String(data.Status);
    }
}

function removeParticipant(data) {
    var hangup = $("#hangupparticipant_" + String(data.CallSid));
    hangup.addClass("disabled");
    var status = $("#participantstatus_" + String(data.CallSid));
    status[0].textContent = String(data.Status);
}


function removeConference(data) {
    var hangup = $("#hangup_" + String(data.ConferenceSid));
    hangup.addClass("disabled");
    var status = $("#conferencestatus_" + String(data.ConferenceSid));
    status[0].textContent = String(data.Status);
}


function loadAllConferences(data) {
    for (var i = 0; i < data.allConferences.length; i++) {
        insertConference(data.allConferences[i].params);
    }
}

function loadAllParticipants(data) {
    for (var i = 0; i < data.allParticipants.length; i++) {
        insertParticipant(data.allParticipants[i].params);
    }
}

function loadConferences() {
    sendObject({"messageType": "Conference", "command": "LoadConferences"});
}

function loadParticipants() {
    sendObject({"messageType": "Conference", "command": "LoadParticipants"});
}

id("sendThanks").addEventListener("click", function () {
    sendObject({"messageType": "Conference", "command": "sendThanks"});
});