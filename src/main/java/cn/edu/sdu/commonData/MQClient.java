package cn.edu.sdu.commonData;

import cn.edu.sdu.commonData.indoorLocation.ReceiveIndoorLocationWorker;
import cn.edu.sdu.commonData.jt808.ReceiveLocationDataWorker;
import cn.edu.sdu.commonData.jt808.ReceiveOriginDataWorker;
import cn.edu.sdu.commonData.jt808.ReceivePhotoWorker;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MQClient {
    public static void main(String[] args) {
        try {
            File file = new File("jt808OriginData");
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    System.out.print("【创建JT808OriginData文件夹失败】");
                }
            }

            ExecutorService executorService = Executors.newFixedThreadPool(4);
            executorService.execute(new ReceiveOriginDataWorker());
            executorService.execute(new ReceiveLocationDataWorker());
            executorService.execute(new ReceivePhotoWorker());
            executorService.execute(new ReceiveIndoorLocationWorker());

        } catch (Exception e) {
            System.out.println("【程序退出】");
        }
    }
}
