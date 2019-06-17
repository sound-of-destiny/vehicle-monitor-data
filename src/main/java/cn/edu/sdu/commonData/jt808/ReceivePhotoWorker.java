package cn.edu.sdu.commonData.jt808;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Statement;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import static cn.edu.sdu.commonData.MQUtil.*;

public class ReceivePhotoWorker implements Runnable {
	
	private static final String QUEUE_NAME = "jt808_photo";

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
			System.out.println(" [*] Photo Worker 正在等待消息");

	        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
				System.out.println("[Photo Worker] 收到消息");
				try {
					ServerData.Protocol protocol = ServerData.Protocol.parseFrom(delivery.getBody());
					String terminalPhone = protocol.getTerminalPhone();
					ServerData.MediaData mediaData = protocol.getMediaData();
					File dir = new File("photos/" + terminalPhone);
					if (!dir.exists()) {
						dir.mkdirs();
					}
					File data = new File("photos/" + terminalPhone + "/" + mediaData.getLocation().getTime() + ".jpg");
					try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(data, true))) {
						dos.write(mediaData.getMediaData().toByteArray());
						dos.flush();
					}
					java.sql.Connection conn = JT808Mysql.connect_145();
					Statement stm = conn.createStatement();
					String sql = String.format("insert into vehicle_photo_info (TerminalPhone, seat, name) values (%s, %d, %s)", terminalPhone, mediaData.getChannelId(), mediaData.getLocation().getTime());
					stm.execute(sql);
					stm.close();
					conn.close();

				} catch(Exception e) {
					e.printStackTrace();
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
