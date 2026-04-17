package com.drp.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 分页请求封装
 *
 * @author Nick
 */
public class PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 默认页码
     */
    public static final int DEFAULT_PAGE = 1;

    /**
     * 默认每页大小
     */
    public static final int DEFAULT_SIZE = 10;

    /**
     * 最大每页大小（防止查询过多数据）
     */
    public static final int MAX_PAGE_SIZE = 100;

    /**
     * 页码
     */
    private Integer page = DEFAULT_PAGE;

    /**
     * 每页大小
     */
    private Integer size = DEFAULT_SIZE;

    /**
     * 排序字段
     */
    private String sortBy;

    /**
     * 排序方向：asc/desc
     */
    private String sortOrder = "desc";

    /**
     * 开始时间（用于时间范围查询）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    /**
     * 结束时间（用于时间范围查询）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    // ==================== 构造函数 ====================

    public PageRequest() {
    }

    // ==================== Getter & Setter ====================

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    // ==================== 辅助方法 ====================

    /**
     * 计算分页偏移量
     */
    public int getOffset() {
        return (page - 1) * size;
    }

    /**
     * 计算分页偏移量（使用long类型，防止大偏移量溢出）
     */
    public long getOffsetAsLong() {
        return ((long) page - 1) * size;
    }

    /**
     * 是否升序排列
     */
    public boolean isAscending() {
        return "asc".equalsIgnoreCase(sortOrder);
    }

    /**
     * 校验并修复参数
     */
    public void validate() {
        if (page == null || page < 1) {
            page = DEFAULT_PAGE;
        }
        if (size == null || size < 1) {
            size = DEFAULT_SIZE;
        }
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }
        if (sortOrder == null) {
            sortOrder = "desc";
        }
    }
}
