console.log('connecting ...')
var socket = io.connect('http://localhost:15141');
console.log('OK connected')

//apres connection
socket.on('init', function(initialJson) {
    console.log('init')
    $('#actionsList ul').prepend(
        $('<ui>').append(
            initialJson +'</br>'
            ));

    var engineJson = '{"part":"Engine", "data":' + initialJson +'}';
    var toVis = JSON.parse(engineJson);
    start(toVis)
});



// Lorsqu'on clique sur le bouton, on envoie un "message" au serveur
$(function() {
$('#poke').click(function () {
    console.log('sending action');

    var message = $('#result').val();

    console.log(message);

    sendAction(message);
});});
function sendAction(action) {

    socket.emit('action', action,function (data) {
        $('#actionsList ul').prepend(
            $('<ui>').append(
                action
                + data +'</br>'
            ));

        console.log(data);

        var explorerJson = '{ "part":"Explorer", "data":' + action +'}';
        var toVis = JSON.parse(explorerJson);
        console.log(toVis);
        handleJson(toVis);

        var engineJson = '{ "part":"Engine", "data":' + data +'}';
        var toVis2 = JSON.parse(engineJson);
        console.log(toVis2);
        handleJson(toVis2);
    });
};



function createAction(action)
{
    var baseAction = '{"action":"'+action.name +'"'
    //action sans param
    if(action.params!= undefined)
    {
        baseAction+=', "parameters":{'
        for(var i=0;i<action.params.length;i+=2) {
            if(i!=0)
                baseAction+=', ';
            baseAction+='"'+action.params[i]+'": "'+action.params[i+1]+'"'
        }
        baseAction+='}'
    }
    baseAction+='}';
    return baseAction
}

function getLeft(dir)
{
    switch (dir)
    {
        case "E":
            return "N";
        case "N":
            return "W";
        case "S":
            return "E";
        case "W":
            return "S";
    }
}

function getRight(dir)
{
    switch (dir)
    {
        case "E":
            return "S";
        case "N":
            return "E";
        case "S":
            return "W";
        case "W":
            return "N";
    }
}

var Action = function(){};
Action.prototype = {
    name: null,
    params: []
}

$(document).keydown(function(e) {
    var actionTodo = new Action();
    switch(e.which) {
        case 90: // z, emmit fly action
            if(e.shiftKey)
            {
                actionTodo.name="echo";
                actionTodo.params = [];
                actionTodo.params.push("direction");
                actionTodo.params.push(direction);
            }
            else
            {
                actionTodo.name="fly";
            }
            var jsonAction = createAction(actionTodo);
            sendAction(jsonAction);
            break;

        case 68: // d : turn right
            if(e.shiftKey)
            {
                actionTodo.name="echo";
            }
            else {
                actionTodo.name="heading";
            }
            actionTodo.params = [];
            actionTodo.params.push("direction");
            actionTodo.params.push(getRight(direction));

            var jsonAction = createAction(actionTodo);
            sendAction(jsonAction);
            break;

        case 81: // q
            if(e.shiftKey)
            {
                actionTodo.name="echo";
            }
            else {
                actionTodo.name="heading";
            }
            actionTodo.params = [];
            actionTodo.params.push("direction");
            actionTodo.params.push(getLeft(direction));
            var jsonAction = createAction(actionTodo);
            sendAction(jsonAction);
            break;

        case 83: // s
            actionTodo.name="scan";
            actionTodo.params = [];
            actionTodo.params.push("direction");
            actionTodo.params.push(getLeft(direction));
            var jsonAction = createAction(actionTodo);
            sendAction(jsonAction);

            break;


        default: return; // exit this handler for other keys
    }
    e.preventDefault(); // prevent the default action (scroll / move caret)
});

$(function() {
    $('form').submit(function() {
        var jsonaction = $('form#sendaction').serializeJSON();
        console.log('k')
        $('#result').val(jsonaction);
        return false;
    });
});
