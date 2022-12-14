package com.dlzx.system.service.impl.PushMessage;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.push.model.v20160801.PushRequest;
import com.aliyuncs.push.model.v20160801.PushResponse;
import com.aliyuncs.utils.ParameterHelper;
import com.dlzx.common.core.utils.StringUtils;
import com.dlzx.system.api.domain.SysUser;
import com.dlzx.system.client.PersonClient;
import com.dlzx.system.domain.DDriver;
import com.dlzx.system.service.impl.SysUserServiceImpl;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author ylh
 * @program:dlzx
 * @description：阿里云消息推送
 * @create:2021-02-21 15-49
 */
@Service
public class PushMessage extends BaseMessage{
    @Autowired
    private SysUserServiceImpl sysUserService;
    @Autowired
    private PersonClient personClient;

    private static final Long androidAppKey =333388194L;
    private static final Long iosAppKey = 333403460L;

    public void testAdvancedPush(String title, String content,String id,int flag) throws Exception { //flag =1 管理员 flag= 2 驾驶员

        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", "LTAI4GDnTwo6nB63QBegTn1T", "CYx9tcfTUtDeB0DHjPlfWn0B6oDRPy");
        IAcsClient client = new DefaultAcsClient(profile);
        int messageUser = 2; //ios =1 adroid =2
        String token = null;
        //判断驾驶员还是管理员
        if (flag == 1){
            SysUser sysUser = sysUserService.selectUserById(Long.valueOf(id));
            if (sysUser != null){
                if (StringUtils.isNotEmpty(sysUser.getToken())){
                    token = sysUser.getToken();
                }
                if (StringUtils.isNotEmpty(sysUser.getDeviceType())&&sysUser.getDeviceType().equals("ios")){
                    messageUser = 1;
                }
                if (StringUtils.isNotEmpty(sysUser.getDeviceType())&&sysUser.getDeviceType().equals("android")){
                    messageUser = 2;
                }
            }
        }
        else {
            DDriver dDriver = personClient.DriverInfo(id);
            if (dDriver != null) {
                if (StringUtils.isNotEmpty(dDriver.getToken())){
                    token = dDriver.getToken();
                }
                if (StringUtils.isNotEmpty(dDriver.getDeviceType())&&dDriver.getDeviceType().equals("ios")){
                    messageUser = 1;
                }
                if (StringUtils.isNotEmpty(dDriver.getDeviceType())&&dDriver.getDeviceType().equals("android")){
                    messageUser = 2;
                }
            }
        }

        PushRequest pushRequest = new PushRequest();
        //安全性比较高的内容建议使用HTTPS
        pushRequest.setProtocol(ProtocolType.HTTPS);
        //内容较大的请求，使用POST请求
        pushRequest.setMethod(MethodType.POST);
        pushRequest.setTarget("DEVICE"); //推送目标: DEVICE:按设备推送 ALIAS : 按别名推送 ACCOUNT:按帐号推送  TAG:按标签推送; ALL: 广播推送
        pushRequest.setTargetValue(token); //根据Target来设定，如Target=DEVICE, 则对应的值为 设备id1,设备id2. 多个值使用逗号分隔.(帐号与设备有一次最多100个的限制)
//        pushRequest.setTarget("ALL"); //推送目标: device:推送给设备; account:推送给指定帐号,tag:推送给自定义标签; all: 推送给全部
//        pushRequest.setTargetValue("ALL"); //根据Target来设定，如Target=device, 则对应的值为 设备id1,设备id2. 多个值使用逗号分隔.(帐号与设备有一次最多100个的限制)
        pushRequest.setPushType("NOTICE"); // 消息类型 MESSAGE NOTICE
        pushRequest.setDeviceType("ALL"); // 设备类型 ANDROID iOS ALL.
        pushRequest.setRegionId("cn-hangzhou");


        // 推送配置
        pushRequest.setTitle(title); // 消息的标题
        pushRequest.setBody("您有新的审批，请尽快处理！"); // 消息的内容


        if (messageUser == 1) {
            //推送目标 ios
            pushRequest.setAppKey(iosAppKey);
            // 推送配置: iOS
            pushRequest.setIOSBadge(5); // iOS应用图标右上角角标
            pushRequest.setIOSSilentNotification(false);//开启静默通知
            pushRequest.setIOSMusic("default"); // iOS通知声音
            pushRequest.setIOSSubtitle("iOS10 subtitle");//iOS10通知副标题的内容
            pushRequest.setIOSNotificationCategory("iOS10 Notification Category");//指定iOS10通知Category
            pushRequest.setIOSMutableContent(true);//是否允许扩展iOS通知内容
            pushRequest.setIOSApnsEnv("PRODUCT");//iOS的通知是通过APNs中心来发送的，需要填写对应的环境信息。"DEV" : 表示开发环境 "PRODUCT" : 表示生产环境
            pushRequest.setIOSRemind(true); // 消息推送时设备不在线（既与移动推送的服务端的长连接通道不通），则这条推送会做为通知，通过苹果的APNs通道送达一次。注意：离线消息转通知仅适用于生产环境
            pushRequest.setIOSRemindBody("iOSRemindBody");//iOS消息转通知时使用的iOS通知内容，仅当iOSApnsEnv=PRODUCT && iOSRemind为true时有效
            pushRequest.setIOSExtParameters("{\"_ENV_\":\"DEV\",\"k2\":\"v2\"}"); //通知的扩展属性(注意 : 该参数要以json map的格式传入,否则会解析出错)
        }
        if (messageUser == 2){
            //推送目标 android
            pushRequest.setAppKey(androidAppKey);
            // 推送配置: Android
            pushRequest.setAndroidNotifyType("NONE");//通知的提醒方式 "VIBRATE" : 震动 "SOUND" : 声音 "BOTH" : 声音和震动 NONE : 静音
            pushRequest.setAndroidNotificationBarType(1);//通知栏自定义样式0-100
            pushRequest.setAndroidNotificationBarPriority(1);//通知栏自定义样式0-100
            pushRequest.setAndroidOpenType("APPLICATION"); //点击通知后动作 "APPLICATION" : 打开应用 "ACTIVITY" : 打开AndroidActivity "URL" : 打开URL "NONE" : 无跳转
//            pushRequest.setAndroidOpenUrl("http://www.aliyun.com"); //Android收到推送后打开对应的url,仅当AndroidOpenType="URL"有效
            pushRequest.setAndroidActivity("com.sti.yiqi.MainActivity"); // 设定通知打开的activity，仅当AndroidOpenType="Activity"有效
            pushRequest.setAndroidMusic("default"); // Android通知音乐
            pushRequest.setAndroidXiaoMiActivity("com.sti.yiqi.MainActivity");//设置该参数后启动小米托管弹窗功能, 此处指定通知点击后跳转的Activity（托管弹窗的前提条件：1. 集成小米辅助通道；2. StoreOffline参数设为true）
            pushRequest.setAndroidXiaoMiNotifyTitle(title);
            pushRequest.setAndroidXiaoMiNotifyBody("您有新的审批，请尽快处理！");
            pushRequest.setAndroidExtParameters("{\"k1\":\"android\",\"k2\":\"v2\"}"); //设定通知的扩展属性。(注意 : 该参数要以 json map 的格式传入,否则会解析出错)
            pushRequest.setAndroidNotificationChannel("1");

            // 厂商通道
            pushRequest.setAndroidActivity("com.sti.yiqi.MainActivity");
            pushRequest.setAndroidPopupTitle(title);
            pushRequest.setAndroidPopupBody("您有新的审批，请尽快处理！");

        }

//        // 推送控制

        pushRequest.setStoreOffline(true); // 离线消息是否保存,若保存, 在推送时候，用户即使不在线，下一次上线则会收到
        final String expireTime = ParameterHelper.getISO8601Time(new Date(System.currentTimeMillis() + 12 * 3600 * 1000)); // 12小时后消息失效, 不会再发送
        pushRequest.setExpireTime(expireTime);



        PushResponse pushResponse = client.getAcsResponse(pushRequest);
        System.out.printf("RequestId: %s, MessageID: %s\n",
                pushResponse.getRequestId(), pushResponse.getMessageId());


    }
}
