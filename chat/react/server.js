var fs = require('fs');
var path = require('path');
var bodyParser = require('body-parser');
var express = require('express');
//var kafkaconsumer = require('./kafkaconsumer');
var mqttconsumer = require('./mqttconsumer');
//var redis = require("redis"),
//    db = redis.createClient("6379", "localhost");

var app = express();

var kafka = require('kafka-node'),
    Producer = kafka.Producer,
    client = new kafka.Client("localhost:2181/kafka"),
    producer = new Producer(client);

producer.on('error', function (err) {
    console.log('error', err)
})

producer.on('error', function (err) {})

client.on("error", function (err) {
        console.log("Error " + err);
    });

app.use('/', express.static(path.join(__dirname, 'public')));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: true}));

app.get('/chat.json', function(req, res) {
  fs.readFile('_chat.json', function(err, data) {
    res.setHeader('Content-Type', 'application/json');
    res.send(data);
  });
});

app.post('/chat.json', function(req, res) {
  j = req.body
  str = JSON.stringify(j)
  console.log("Send to Kafka: " + str)
  producer.send([{topic: 'chat_out', messages: str, partition: 0}], function (err, data) {
        console.log(data);
    });

  /*db.select(1, function(err,res){
    var arr = []

    db.get("browser", function(err, reply) {
      if (reply == null) {
        arr.push(str)
        db.set(j.author, arr)

      } else {
        try {
          var json = JSON.parse(reply);
          console.log("json: "+ json.length)
          for (var i = 0; i < json.length; i++) {
            arr.push(json[i])
          }

          //json.push(str)
          //db.set(j.author, json)
          console.log("arr:" + arr)
        } catch (e) {
          console.log(e)
        }
      }
    });
//    v = []
//    v[0] = str
//    v[1] = "11111"
//    db.set(j.author, v);
  });*/

  fs.readFile('_chat.json', function(err, data) {
    var chats = JSON.parse(data);
    chats.push(req.body);
    fs.writeFile('_chat.json', JSON.stringify(chats, null, 4), function(err) {
      res.setHeader('Content-Type', 'application/json');
      res.send(JSON.stringify(chats));
    });
  });
});

app.listen(3000);

console.log('Server started: http://localhost:3000/');
