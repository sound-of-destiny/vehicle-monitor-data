package cn.edu.sdu.commonData.indoorLocation;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.bson.Document;

import java.nio.charset.StandardCharsets;

import static cn.edu.sdu.commonData.MQUtil.*;
import static cn.edu.sdu.commonData.MQUtil.Password;

public class ReceiveIndoorLocationWorker implements Runnable {
    private static final String EXCHANGE_NAME = "indoor-location";

    public void run() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(IP);
            factory.setVirtualHost(VirtualHost);
            factory.setUsername(Username);
            factory.setPassword(Password);
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, "topic");
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, EXCHANGE_NAME, "location");

            System.out.println(" [*] Indoor-Location Worker 正在等待消息");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                System.out.println("[Indoor-Location Worker] 收到消息");
                try (MongoClient mongoClient = MongoClients.create("mongodb://127.0.0.1:27017")) {
                    Document document = Document.parse(new String(delivery.getBody(), StandardCharsets.UTF_8));
                    Document newDocument = (Document) document.get("data");
                    MongoDatabase mongoDatabase = mongoClient.getDatabase("indoor");
                    MongoCollection<Document> collection = mongoDatabase.getCollection("indoor_location");
                    collection.insertOne(newDocument);
                }
            };
            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
