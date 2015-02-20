var fs = require('fs');

var kafka = require('kafka-node'),
    Consumer = kafka.Consumer,
    client = new kafka.Client("localhost:2181/kafka"),
    consumer = new Consumer(
        client,
        [{ topic: 'chat_in', partition: 0 }],
        {autoCommit: true, fromBeginning: false, fetchMaxWaitMs: 1000, fetchMaxBytes: 1024*1024}
    );

console.log('Connect to kafka');

consumer.on('message', function (message) {
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
});
