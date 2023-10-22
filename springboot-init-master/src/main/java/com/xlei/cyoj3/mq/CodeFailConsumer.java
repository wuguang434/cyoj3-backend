package com.xlei.cyoj3.mq;

import com.rabbitmq.client.Channel;
import com.xlei.cyoj3.common.ErrorCode;
import com.xlei.cyoj3.exception.BusinessException;
import com.xlei.cyoj3.model.entity.QuestionSubmit;
import com.xlei.cyoj3.model.enums.QuestionSubmitStatusEnum;
import com.xlei.cyoj3.service.QuestionSubmitService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.xlei.cyoj3.constant.MqConstant.CODE_DLX_QUEUE;

/**
 * 判题的死信队列
 */
@Component
@Slf4j
public class CodeFailConsumer {

    @Resource
    private QuestionSubmitService questionSubmitService;

    @SneakyThrows
    @RabbitListener(queues = {CODE_DLX_QUEUE}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long delivery) {
        // 接收到失败的消息
        log.info("死信队列接收到的消息: ", message);
        if (StringUtils.isBlank(message)) {
            channel.basicNack(delivery, false, false);
            throw new BusinessException(ErrorCode.ERROR_NULL, "消息为空");
        }

        long questionSubmitId = Long.parseLong(message);
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
        if (questionSubmit == null) {
            channel.basicNack(delivery, false, false);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "提交的题目不存在");
        }

        //标记为失败
        questionSubmit.setStatus(QuestionSubmitStatusEnum.FAILED.getValue());

        boolean update = questionSubmitService.updateById(questionSubmit);
        if (!update) {
            log.info("死信队列处理消息失败,对应提交的题目idwield: ", questionSubmit.getId());
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "处理消息失败-死信队列");
        }

        channel.basicAck(delivery, false);
    }
}
