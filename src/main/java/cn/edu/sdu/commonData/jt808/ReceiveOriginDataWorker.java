package cn.edu.sdu.commonData.jt808;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;

import static cn.edu.sdu.commonData.MQUtil.*;
import static cn.edu.sdu.commonData.MQUtil.Password;

public class ReceiveOriginDataWorker implements Runnable {

    private static final String QUEUE_NAME = "jt808_origin_data";

    public void run() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(IP);
            factory.setVirtualHost(VirtualHost);
            factory.setUsername(Username);
            factory.setPassword(Password);
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            channel.basicQos(1);

            System.out.println(" [*] Origin Worker 正在等待消息");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                System.out.println("[Origin Worker] 收到消息");
                try {
                    File data = new File("jt808OriginData/" + LocalDate.now() + ".bytes");
                    DataOutputStream dos = new DataOutputStream(new FileOutputStream(data, true));
                    dos.write(delivery.getBody());
                    dos.close();
                } finally {
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                }
            };
            channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
