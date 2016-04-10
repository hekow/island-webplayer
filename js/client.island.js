console.log('connecting ...')
var socket = io.connect('http://localhost:15141');
console.log('OK connected')

//apres connection
socket.on('init', function(message) {
    console.log('server connected')
    $('#actionsList ul').append(
        $('<ui>').append(
            message
            ));

    var engineJson = '{ "part":"engine", "data":' + message +'}';
    var toVis = JSON.parse(engineJson);
    start(toVis)
});



// Lorsqu'on clique sur le bouton, on envoie un "message" au serveur
$(function() {
$('#poke').click(function () {
    console.log('clicked');

    var message = $('#result').val();

    console.log(message);
    socket.emit('action', message,function (data) {
        $('#actionsList ul').append(
            $('<ui>').append(
                message +'</br>'
                + data
            ));

        console.log(data);

        var explorerJson = '{ "part":"Explorer", "data":' + message +'}';
        var toVis = JSON.parse(explorerJson);
        console.log(toVis);
        handleJson(toVis);

        var engineJson = '{ "part":"Engine", "data":' + data +'}';
        var toVis2 = JSON.parse(engineJson);
        console.log(toVis2);
        handleJson(toVis2);
    });
});});



$(function() {
    $('form').submit(function() {
        var jsonaction = $('form#sendaction').serializeJSON();
        console.log('k')
        $('#result').val(jsonaction);
        return false;
    });
});
