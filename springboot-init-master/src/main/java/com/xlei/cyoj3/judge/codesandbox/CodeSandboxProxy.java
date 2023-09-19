package com.xlei.cyoj3.judge.codesandbox;

import com.xlei.cyoj3.judge.codesandbox.model.ExecuteCodeRequest;
import com.xlei.cyoj3.judge.codesandbox.model.ExecuteCodeResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class CodeSandboxProxy implements CodeSandbox {
    private final CodeSandbox codeSandbox;

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        log.info("代码沙箱请求信息:" + executeCodeRequest.toString());
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        log.info("代码沙箱相应信息:" + executeCodeResponse.toString());
        return executeCodeResponse;
    }
}
