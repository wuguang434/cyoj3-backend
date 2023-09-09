package com.xlei.cyoj3.service;

import com.xlei.cyoj3.model.entity.PostThumb;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xlei.cyoj3.model.entity.User;

/**
 * 帖子点赞服务
 *
 # @author <a href="https://github.com/wuguang434">Coding boy:xlei</a>
 */
public interface PostThumbService extends IService<PostThumb> {

    /**
     * 点赞
     *
     * @param postId
     * @param loginUser
     * @return
     */
    int doPostThumb(long postId, User loginUser);

    /**
     * 帖子点赞（内部服务）
     *
     * @param userId
     * @param postId
     * @return
     */
    int doPostThumbInner(long userId, long postId);
}
