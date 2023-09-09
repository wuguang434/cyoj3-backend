package com.xlei.cyoj3.common;

import java.io.Serializable;
import lombok.Data;

/**
 * 删除请求
 *
 # @author <a href="https://github.com/wuguang434">Coding boy:xlei</a>
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}