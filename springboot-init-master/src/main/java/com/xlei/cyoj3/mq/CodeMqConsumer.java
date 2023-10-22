package com.xlei.cyoj3.mq;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.xlei.cyoj3.common.ErrorCode;
import com.xlei.cyoj3.exception.BusinessException;
import com.xlei.cyoj3.judge.JudgeService;
import com.xlei.cyoj3.judge.codesandbox.model.JudgeInfo;
import com.xlei.cyoj3.model.entity.Question;
import com.xlei.cyoj3.model.entity.QuestionSubmit;
import com.xlei.cyoj3.model.enums.JudgeInfoMessageEnum;
import com.xlei.cyoj3.service.QuestionService;
import com.xlei.cyoj3.service.QuestionSubmitService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


import static com.xlei.cyoj3.constant.MqConstant.CODE_QUEUE;

/**
 * 消息队列-消费者
 */
@Component
@Slf4j
public class CodeMqConsumer {

    @Resource
    private JudgeService judgeService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private QuestionService questionService;

    /**
     * 指定监听的消息队列及确认机制
     *
     * @param message
     * @param channel
     * @param deliverTag
     */
    @SneakyThrows
    @RabbitListener(queues = CODE_QUEUE, ackMode = "MANUAL")
    private void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliverTag) {
        long questionSubmitId = Long.parseLong(message);
        log.info("消息队列接收到消息:", message);

        if (message == null) {
            //不重试,进入死信队列
            channel.basicNack(deliverTag, false, false);
            throw new BusinessException(ErrorCode.ERROR_NULL);
        }

        try {
            judgeService.doJudge(questionSubmitId);
            QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
            //获取到判题JudgeInfo信息结果
            String getJudgeInfo = questionSubmit.getJudgeInfo();
            //转换成JudgeInfo格式获取到是否成功的message
            Gson gson = new Gson();
            JudgeInfo judgeInfo = gson.fromJson(getJudgeInfo, JudgeInfo.class);
            String judgeInfoMessage = judgeInfo.getMessage();

            //判断是否成功
            if (!JudgeInfoMessageEnum.ACCEPTED.getValue().equals(judgeInfoMessage)) {
                //失败则返回拒收的消息
                channel.basicNack(deliverTag, false, false);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "操作失败,请查看代码是否正确");
            }

            //成功则通过数+1
            Long questionId = questionSubmit.getQuestionId();
            log.info("题目id: ", questionId);
            Question question = questionService.getById(questionId);
            synchronized (question.getAcceptedNum()) {
                int acceptedNum = question.getAcceptedNum();
                question.setAcceptedNum(acceptedNum + 1);
                boolean save = questionService.updateById(question);
                if (!save) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "保存数据失败");
                }
            }
            channel.basicAck(deliverTag, false);
        } catch (Exception e) {
            //返回的信息为空,则拒收消息,并放入死信队列
            channel.basicNack(deliverTag, false, false);
            e.printStackTrace();
        }
    }
}
