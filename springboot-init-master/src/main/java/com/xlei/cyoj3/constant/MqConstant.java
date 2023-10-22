package com.xlei.cyoj3.constant;

/**
 * 交换机常量
 */
public interface MqConstant {

    /**
     * 普通交换机
     */
    String CODE_EXCHANGE_NAME="code_exchange";
    String CODE_QUEUE="code_queue";
    String CODE_ROUTING_KEY="code_routingKey";
    String CODE_DIRECT_EXCHANGE="direct";

    /**
     * 死信队列交换机
     */
    String CODE_DLX_EXCHANGE="code-deadlx-exchange";
    /**
     * 死信队列交换机
     */
    String CODE_DLX_QUEUE="code_deadlx_queue";
    /**
     * 死信队列路由键
     */
    String CODE_DLX_ROUTING_KEY="code_deadlx_routingKey";
}
