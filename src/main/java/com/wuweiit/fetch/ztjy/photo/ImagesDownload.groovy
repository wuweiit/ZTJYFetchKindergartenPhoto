package com.wuweiit.fetch.ztjy.photo

import cn.hutool.http.HttpUtil
import cn.hutool.json.JSONArray
import org.apache.tools.ant.util.DateUtils

import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService

@groovy.util.logging.Slf4j
public class ImagesDownload {

    ExecutorService threadPool

    JSONArray images

    ImagesDownload(ExecutorService threadPool, JSONArray images){
        this.images = images;
        this.threadPool = threadPool
    }

    void download() {
        CountDownLatch countDownLatch = new CountDownLatch(images.size());
        images.forEach(imgObj -> {
            String imgUrl = imgObj.imageUrl
            String orgImgUrl = imgUrl.split("\\?")[0];
            if(imgObj.photoTime == null){
                imgObj.photoTime = new Date().getTime();
            }
            String timePath = DateUtils.format(new Date(imgObj.photoTime),"yyyyMMdd");

            Runnable run = {
                String filePath = "D://test/" + timePath + "/" + imgObj.width +'-'+imgObj.height+''+ '-' + imgObj.size + ".png";
                if(!new File(filePath).exists()){
                    log.info("正在下载image：{}", imgObj.imageUrl);
                    HttpUtil.downloadFile(orgImgUrl, filePath)
                }
                countDownLatch.countDown()
            }
            threadPool.submit(run)
        })
        countDownLatch.await();
    }
}
