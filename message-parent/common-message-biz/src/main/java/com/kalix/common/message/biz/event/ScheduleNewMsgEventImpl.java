package com.kalix.common.message.biz.event;

import com.google.gson.Gson;
import com.kalix.admin.core.entities.UserBean;
import com.kalix.common.message.api.BaseMessageEvent;
import com.kalix.common.message.api.Const;
import com.kalix.common.message.entities.MessageBean;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * 计划任务中的消息进行监听处理类,负责把新建的任务发送给任务的执行人。
 * Created by sunlf on 2016/8/26.
 */
public class ScheduleNewMsgEventImpl extends BaseMessageEvent implements EventHandler {
    public static final String MSG_CONTENT = "%s,您好！\r\n  您收到了一条由%s布置的新任务，任务名称为:%s，请查看！";
    public static final String MSG_TITLE = "计划任务新任务提醒";

    @Override
    public void handleEvent(Event event) {
        //布置人
        String userName = (String) event.getProperty("userName");
        //任务名称
        String taskName = (String) event.getProperty("taskName");
        //负责人
        Long head = (Long) event.getProperty("head");
        //参与人
        String participant = (String) event.getProperty("participant");

        //根据用户id获取实体，进而获取到用户名称
        UserBean userBean = userBeanService.getEntity(head);
        String headName = userBean.getName();
        String content = String.format(MSG_CONTENT, headName, userName, taskName);

        MessageBean messageBean = createMessageBean(userBean.getId(), content, MSG_TITLE);
        dao.save(messageBean);
        //add msg to stack
        Gson gson = new Gson();
        stackService.publish(String.format(Const.POLLING_MESSAGE_TOPIC_FORMAT, String.valueOf(userBean.getId())), gson.toJson(messageBean), day);

        String participants[] = participant.split(",");
        if(participants.length > 0){
            for(int i = 0; i < participants.length; i++) {
                userBean = userBeanService.getEntity(Long.parseLong(participants[i]));
                String participantName = userBean.getName();
                content = String.format(MSG_CONTENT,participantName,userName,taskName);
                messageBean = createMessageBean(userBean.getId(),content,MSG_TITLE);
                dao.save(messageBean);

                stackService.publish(String.format(Const.POLLING_MESSAGE_TOPIC_FORMAT, String.valueOf(userBean.getId())), gson.toJson(messageBean), day);
            }

        }
    }
}
