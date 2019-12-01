package com.report.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 日志对象
 * @author Charles Wesley
 * @date 2019/11/30 19:44
 */

@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class LogPO implements Serializable {
    private static final long serialVersionUID = 8148817463997595381L;

    /**
     * 任务日志id
     */
    private String taskLogId;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 日志描述
     */
    private String remark;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务类型编码
     */
    private String taskType;

    /**
     * 任务类型名称
     */
    private String taskTypeName;

    /**
     * 创建日志的用户id
     */
    private String createUserId;

    /**
     * 创建日志的用户名称
     */
    private String createUserName;

    /**
     * 项目组名称
     */
    private String department;

    /**
     * 	消耗工时（小时）
     */
    private Double lostTime;

    /**
     * 项目经理PM
     */
    private String pm;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 日志创建时间
     */
    private Long createTime;

    /**
     * 日志更新时间
     */
    private Long updateTime;
}
