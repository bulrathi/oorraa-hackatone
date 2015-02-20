var fs = require('fs');

var mqtt    = require('mqtt');
var client  = mqtt.connect('mqtt://188.166.32.82');

client.subscribe('chat/out1');

console.log('Connect to mqtt');

client.on('message', function (topic, message) {
 try {
        fs.readFile('_chat.json', function(err, data) {
            var chats = JSON.parse(data);
            console.log("Get from MQTT: " + message)
            try {
                var json = JSON.parse(message)

                chats.push(json);
                fs.writeFile('_chat.json', JSON.stringify(chats, null, 4), function(err) {});
            } catch (e) {
                console.log(e)
            }
        });

    } catch (e) {
        console.log(e)
    }
});

/*consumer.on('message', function (message) {
    try {
        fs.readFile('_chat.json', function(err, data) {
            var chats = JSON.parse(data);
            console.log("Get from Kafka: " + message.value)
            try {
                var json = JSON.parse(message.value)

                chats.push(json);
                fs.writeFile('_chat.json', JSON.stringify(chats, null, 4), function(err) {});
            } catch (e) {
                console.log(e)
            }
        });

        consumer.commit(function(err, data) {
            console.log(data)
        });
    } catch (e) {
        console.log(e)
    }
});*/
