package com.xlei.cyoj3.model.dto.question;


import com.xlei.cyoj3.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 查询请求
 *
 # @author <a href="https://github.com/wuguang434">Coding boy:xlei</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */

    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表（json 数组）
     */
    private List<String> tags;

    /**
     * 题目答案
     */
    private String answer;


//    /**
//     * 点赞数
//     */
//    private Integer thumbNum;
//
//    /**
//     * 收藏数
//     */
//    private Integer favourNum;

    /**
     * 创建用户 id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}