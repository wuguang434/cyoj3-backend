package com.xlei.cyoj3.judge.codesandbox;

import com.xlei.cyoj3.judge.codesandbox.model.ExecuteCodeRequest;
import com.xlei.cyoj3.judge.codesandbox.model.ExecuteCodeResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 代理代码沙箱接口
 */
@Slf4j
@AllArgsConstructor
public class CodeSandboxProxy implements CodeSandbox {
    private final CodeSandbox codeSandbox;

    /**
     * 代码沙箱执行接口
     * @param executeCodeRequest
     * @return
     */
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        log.info("\n\n代码沙箱请求信息:" + executeCodeRequest.toString());
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        log.info("\n\n代码沙箱响应信息:" + executeCodeResponse.toString());
        return executeCodeResponse;
    }
}
