package com.xlei.cyoj3.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xlei.cyoj3.common.ErrorCode;
import com.xlei.cyoj3.constant.CommonConstant;
import com.xlei.cyoj3.exception.BusinessException;
import com.xlei.cyoj3.judge.JudgeService;
import com.xlei.cyoj3.mapper.QuestionSubmitMapper;
import com.xlei.cyoj3.model.dto.question.QuestionQueryRequest;
import com.xlei.cyoj3.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.xlei.cyoj3.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.xlei.cyoj3.model.entity.Question;
import com.xlei.cyoj3.model.entity.QuestionSubmit;
import com.xlei.cyoj3.model.entity.QuestionSubmit;
import com.xlei.cyoj3.model.entity.User;
import com.xlei.cyoj3.model.enums.QuestionSubmitLanguageEnum;
import com.xlei.cyoj3.model.enums.QuestionSubmitStatusEnum;
import com.xlei.cyoj3.model.vo.QuestionSubmitVO;
import com.xlei.cyoj3.model.vo.QuestionVO;
import com.xlei.cyoj3.model.vo.UserVO;
import com.xlei.cyoj3.mq.CodeMyProducer;
import com.xlei.cyoj3.service.QuestionService;
import com.xlei.cyoj3.service.QuestionSubmitService;
import com.xlei.cyoj3.service.QuestionSubmitService;
import com.xlei.cyoj3.service.UserService;
import com.xlei.cyoj3.utils.SqlUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.xlei.cyoj3.constant.MqConstant.CODE_EXCHANGE_NAME;
import static com.xlei.cyoj3.constant.MqConstant.CODE_ROUTING_KEY;

/**
 * @author 12100
 * @description 针对表【question_submit(题目提交)】的数据库操作Service实现
 * @createDate 2023-09-09 15:40:34
 */
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
        implements QuestionSubmitService {

    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private JudgeService judgeService;

    @Resource
    private CodeMyProducer codeMyProducer;

    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest
     * @param loginUser
     * @return
     */
    @Override
    public long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {
        //校验编程语言是否合法
        String language = questionSubmitAddRequest.getLanguage();
        //根据字符串找到这个枚举值
        QuestionSubmitLanguageEnum languageEnum = QuestionSubmitLanguageEnum.getEnumByValue(language);
        if (languageEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "编程语言错误");
        }
        long questionId = questionSubmitAddRequest.getQuestionId();
        // 判断实体是否存在，根据类别获取实体
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        //设置提交数
        Question updateQuestions = questionService.getById(questionId);
        synchronized (question.getSubmitNum()) {
            int submitNumber = question.getSubmitNum() + 1;
            updateQuestions.setSubmitNum(submitNumber);
            boolean save = questionService.updateById(updateQuestions);
            if (!save) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "数据保存失败");
            }
        }
        // 是否已提交题目
        long userId = loginUser.getId();
        // 每个用户串行提交题目
        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setUserId(userId);
        questionSubmit.setQuestionId(questionId);
        questionSubmit.setCode(questionSubmitAddRequest.getCode());
        questionSubmit.setLanguage(language);
        //设置初始状态
        questionSubmit.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());
        questionSubmit.setJudgeInfo("{}");
        boolean save = this.save(questionSubmit);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据保存失败");
        }
        Long questionSubmitId = questionSubmit.getId();
        codeMyProducer.sendMessage(CODE_EXCHANGE_NAME, CODE_ROUTING_KEY, String.valueOf(questionSubmitId));

//        //执行判题服务
//        CompletableFuture.runAsync(() -> {
//            judgeService.doJudge(questionSubmitId);
//        });
        return questionSubmitId;
    }

    /**
     * 获取查询包装类（用户根据哪些字段查询，根据前端传来的请求对象，得到 mybatis 框架支持的查询 QueryWrapper 类）
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        if (questionSubmitQueryRequest == null) {
            return queryWrapper;
        }
        String language = questionSubmitQueryRequest.getLanguage();
        Integer status = questionSubmitQueryRequest.getStatus();
        Long questionId = questionSubmitQueryRequest.getQuestionId();
        Long userId = questionSubmitQueryRequest.getUserId();
        String sortField = questionSubmitQueryRequest.getSortField();
        String sortOrder = questionSubmitQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.eq(StringUtils.isNotBlank(language), "language", language);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq(QuestionSubmitStatusEnum.getEnumByValue(status) != null, "status", status);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser) {
        QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
        // 脱敏:仅本人及管理员能看见自己(提交 userId 和登陆用户id不同)提交的代码
        Long userId = loginUser.getId();
        //处理脱敏
        if (userId != questionSubmit.getUserId() && !userService.isAdmin(loginUser)) {
            questionSubmitVO.setCode(null);
        }
        return questionSubmitVO;
    }

    @Override
    public Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser) {
        List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
        Page<QuestionSubmitVO> questionSubmitVOPage = new Page<>(questionSubmitPage.getCurrent(), questionSubmitPage.getSize(), questionSubmitPage.getTotal());
        if (CollectionUtils.isEmpty(questionSubmitList)) {
            return questionSubmitVOPage;
        }
        // 1. 关联查询用户信息
        //把每个题目提交信息中的用户id放到一个集合中
        //根据多条id去查用户表得到一个用户的集合,再根据id进行分组,得到每个id对应的用户信息
        //再根据这个id去跟原有的用户id信息进行匹配,再为对应的题目提交信息填充对应的用户信息
        //ps:对执行多条get操作的优化
        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList.stream()
                .map(questionSubmit -> getQuestionSubmitVO(questionSubmit, loginUser))
                .collect(Collectors.toList());
        questionSubmitVOPage.setRecords(questionSubmitVOList);
        return questionSubmitVOPage;
    }
}




