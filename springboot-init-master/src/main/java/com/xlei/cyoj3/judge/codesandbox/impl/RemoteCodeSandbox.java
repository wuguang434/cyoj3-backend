package com.xlei.cyoj3.judge.codesandbox.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.xlei.cyoj3.common.ErrorCode;
import com.xlei.cyoj3.exception.BusinessException;
import com.xlei.cyoj3.judge.codesandbox.CodeSandbox;
import com.xlei.cyoj3.judge.codesandbox.model.ExecuteCodeRequest;
import com.xlei.cyoj3.judge.codesandbox.model.ExecuteCodeResponse;

/**
 * 远程代码沙箱(实际调用接口的沙箱)
 */
public class RemoteCodeSandbox implements CodeSandbox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("==============================远程代码沙箱启动==============================");
        String url = "http://localhost:8090/executeCode";
        String json = JSONUtil.toJsonStr(executeCodeRequest);//把请求参数转化为JSON格式使用
        String responseStr = HttpUtil.createPost(url)
                .body(json)
                .execute()
                .body();
        if (StrUtil.isBlank(responseStr)) {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "executeCode remoteSandbox error,message =" + responseStr);
        }
        return JSONUtil.toBean(responseStr, ExecuteCodeResponse.class);//把字符串转化为需要的响应类
    }
}
