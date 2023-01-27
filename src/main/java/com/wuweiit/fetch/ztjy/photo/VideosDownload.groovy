package com.wuweiit.fetch.ztjy.photo

import cn.hutool.http.HttpUtil
import cn.hutool.json.JSONArray
import org.apache.tools.ant.util.DateUtils

import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService

@groovy.util.logging.Slf4j
public class VideosDownload {
    ExecutorService threadPool

    JSONArray videos

    VideosDownload(ExecutorService threadPool,JSONArray videos){
        this.videos = videos;
        this.threadPool = threadPool
    }

    void download() {
        CountDownLatch countDownLatch = new CountDownLatch(videos.size());
        videos.forEach(obj -> {
            String timePath = DateUtils.format(new Date(),"yyyyMMdd");
            Runnable run = {
                String filePath = "D://test/" + timePath + "/" + obj.width + '-' + obj.height +'_'+obj.duration+ ".mp4"
                if(!new File(filePath).exists()){
                    log.info("正在下载video：{}", obj.videoUrl);
                    HttpUtil.downloadFile(obj.videoUrl, filePath)
                }
                countDownLatch.countDown()
            }
            threadPool.submit(run)
        })

        countDownLatch.await()
    }
}
