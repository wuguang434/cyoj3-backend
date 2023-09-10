package com.xlei.cyoj3.model.dto.questionsubmit;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 创建请求
 *
 # @author <a href="https://github.com/wuguang434">Coding boy:xlei</a>
 */
@Data
public class QuestionSubmitAddRequest implements Serializable {
    /**
     * 编程语言
     */
    private String language;

    /**
     * 用户代码
     */
    private String code;
    /**
     * 题目 id
     */
    private Long questionId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}