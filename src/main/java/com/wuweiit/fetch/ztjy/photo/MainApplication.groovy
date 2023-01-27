import cn.hutool.http.HttpRequest
import cn.hutool.http.HttpResponse
import cn.hutool.http.HttpUtil
import cn.hutool.json.JSONArray
import cn.hutool.json.JSONObject
import cn.hutool.json.JSONUtil
import com.wuweiit.fetch.ztjy.photo.ImagesDownload
import com.wuweiit.fetch.ztjy.photo.VideosDownload
import groovy.util.logging.Slf4j

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Slf4j
class MainApplication {

    static String getToken() {
        return System.getenv("ZTJY_TOKEN");
    }

    static String getJSESSIONID() {
        return System.getenv("ZTJY_JSESSIONID");
    }

    public static ExecutorService THREAD_POOL = Executors.newFixedThreadPool(50);

    public static JSONObject userMap;


    /**
     * 获取用户信息
     * @return
     */
    static JSONObject getUserStudentInfo(){
        HttpRequest httpRequest = HttpUtil.createPost("https://zths.szy.cn/parent/userinfo/v2.0");
        httpRequest.body("{}", "application/json");
        HttpCookie httpCookie = new HttpCookie("JSESSIONID", getJSESSIONID())
        httpRequest.cookie(httpCookie)

        HttpResponse httpResponse = httpRequest.execute();
        JSONObject userInfo = new JSONObject()
        if (httpResponse.getStatus() == 200) {
            String json = httpResponse.body();
            JSONObject jsonObject = JSONUtil.parseObj(json);
            userInfo = jsonObject.body
        }
        httpRequest = HttpUtil.createPost("https://zths.szy.cn/school/parent/student/studentList/v2.0");
        httpRequest.body("{}", "application/json");
        httpRequest.header("Authorization", getToken())
        httpResponse = httpRequest.execute();
        if (httpResponse.getStatus() == 200) {
            String json = httpResponse.body();
            JSONObject jsonObject = JSONUtil.parseObj(json);
            JSONObject studentInfo = jsonObject.body.studentList[0]
            userInfo.putAll(studentInfo);
        }
        return userInfo;
    }

    /**
     * 下拉刷新
     * @return
     */
    static String downRefresh(){
        HttpRequest httpRequest = HttpUtil.createPost("https://api.szy.cn/growthproxy/gardentime/downrefresh/v1.0");
        httpRequest.body("""
            { 
                "schoolType": 2,
                "count": 20,
                "roleType": "1",
                "userId": "${userMap.userInfo.userId}",
                "platform": 1, 
                "studentId": "${userMap.studentInfo.studentId}",
                "babyId": "${userMap.childId}",
                "classId": "${userMap.studentInfo.classId}",
                "leaveSchoolTime": "",
                "classInfos": [
                    {
                        "classId": "${userMap.studentInfo.classId}",
                        "joinClassTime": "${userMap.studentInfo.startTime}"
                    }
                ],
                "industryType": "A",
                "joinSchoolTime": "${userMap.studentInfo.startTime}",
                "feedId": "",
                "schoolId": "${userMap.schoolInfo.schoolId}",
                "graduated": "0",
                "adParams": {}
            }
            """, "application/json");
        httpRequest.header("Authorization", getToken())

        HttpResponse httpResponse = httpRequest.execute();
        if (httpResponse.getStatus() == 200) {
            String json = httpResponse.body();
            JSONObject jsonObject = JSONUtil.parseObj(json);

            JSONArray feeds = jsonObject.body.feeds;
            feeds.forEach(item -> {
                if (item.contentType == 8) {
                    JSONArray images = item.content.images;
                    new ImagesDownload(THREAD_POOL, images).download();

                } else if(item.contentType == 10){// video
                    JSONArray videos = item.content.videos;
                    new VideosDownload(THREAD_POOL, videos).download();
                } else {
                    System.out.println(item);
                }
            })
            String lastFeedId = feeds[feeds.size()-1].feedId
            return lastFeedId
        }
    }


    /**
     * 上拉加载
     */
    static String upRefresh(String feedId){
        HttpRequest httpRequest = HttpUtil.createPost("https://api.szy.cn/growthproxy/gardentime/uprefresh/v1.0");
        httpRequest.body("""{
            "zipCode": "510100",
            "appVersion": "6.75.1",
            "moduleType": 2,
            "os": 1,
            "schoolType": 2,
            "count": 20,
            "roleType": "1",
            "userId": "${userMap.userInfo.userId}",
            "platform": 1, 
            "studentId": "${userMap.studentInfo.studentId}",
            "babyId": "${userMap.childId}",
            "classId": "${userMap.studentInfo.classId}",
            "leaveSchoolTime": "",
            "classInfos": [
                {
                    "classId": "${userMap.studentInfo.classId}",
                    "joinClassTime": "${userMap.studentInfo.startTime}"
                }
            ],
            "industryType": "A",
            "joinSchoolTime": "${userMap.studentInfo.startTime}",
            "feedId": "$feedId",
            "schoolId": "${userMap.schoolInfo.schoolId}",
            "graduated": "0",
            "adParams": {}
        }
        """, "application/json");
        httpRequest.header("Authorization", getToken())

        HttpResponse httpResponse = httpRequest.execute();
        if (httpResponse.getStatus() == 200) {
            String json = httpResponse.body();
            JSONObject jsonObject = JSONUtil.parseObj(json);

            JSONArray feeds = jsonObject.body.feeds;
            feeds.forEach(item -> {
                if (item.contentType == 8) {
                    JSONArray images = item.content.images;
                    new ImagesDownload(THREAD_POOL, images).download();

                } else if(item.contentType == 10){// video
                    JSONArray videos = item.content.videos;
                    new VideosDownload(THREAD_POOL, videos).download();
                } else {
                    log.info(JSONUtil.toJsonStr(item))
                }
            })
            if( feeds.size()-1 ==0){
                return null
            }
            String lastFeedId = feeds[feeds.size()-1].feedId
            return lastFeedId
        }
    }

    static void main(String[] args) {

        userMap = getUserStudentInfo();

        log.info("download start!")
        String lastFeedId = downRefresh();
//        String lastFeedId = "xxx"; // 如果到特定的feedId中断了，可以从这里开始
        boolean next = true;
        while (next){
            if (Objects.nonNull(lastFeedId)){
                log.info("upRefresh lastFeedId={}", lastFeedId)
                lastFeedId = upRefresh(lastFeedId)
            }else{
                next = false
            }
        }

        log.info("download end!")
    }
}