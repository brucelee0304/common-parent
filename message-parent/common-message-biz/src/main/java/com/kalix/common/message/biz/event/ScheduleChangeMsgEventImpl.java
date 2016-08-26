package com.kalix.common.message.biz.event;

import com.google.gson.Gson;
import com.kalix.admin.core.entities.UserBean;
import com.kalix.common.message.api.BaseMessageEvent;
import com.kalix.common.message.api.Const;
import com.kalix.common.message.entities.MessageBean;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * 计划任务中的消息进行监听处理类,负责把任务的状态修改发送给任务的布置人。
 * Created by sunlf on 2016/8/26.
 */
public class ScheduleChangeMsgEventImpl extends BaseMessageEvent implements EventHandler {
    public static final String MSG_CONTENT = "%s,您好！\r\n  您布置的任务[%s]状态已经由[%s]修改,状态从[%s]修改为:[%s]，请查看！";
    public static final String MSG_TITLE = "计划任务状态修改提醒";

    @Override
    public void handleEvent(Event event) {
        //布置人
        String userName = (String) event.getProperty("userName");
        //任务名称
        String taskName = (String) event.getProperty("taskName");
        //负责人
        Long head = (Long) event.getProperty("head");
        UserBean userBean = userBeanService.getEntity(head);
        String headName = userBean.getName();
        //任务状态
        String state = (String) event.getProperty("state");
        //任务旧状态
        String oldState = (String) event.getProperty("oldState");

        String content = String.format(MSG_CONTENT, userName, taskName, headName, oldState, state);

        MessageBean messageBean = createMessageBean(userBean.getId(), content, MSG_TITLE);
        dao.save(messageBean);
        //add msg to stack
        Gson gson = new Gson();
        stackService.publish(String.format(Const.POLLING_MESSAGE_TOPIC_FORMAT,
                String.valueOf(userBean.getId())), gson.toJson(messageBean), day);
    }
}
