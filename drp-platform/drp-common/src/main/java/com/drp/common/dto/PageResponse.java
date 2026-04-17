package com.drp.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 分页响应封装
 *
 * @author Nick
 */
public class PageResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录列表
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 当前页码
     */
    private int page;

    /**
     * 每页大小
     */
    private int size;

    /**
     * 总页数
     */
    private int totalPages;

    /**
     * 是否有下一页
     */
    private boolean hasNext;

    /**
     * 是否有上一页
     */
    private boolean hasPrevious;

    /**
     * 是否为第一页
     */
    private boolean isFirst;

    /**
     * 是否为最后一页
     */
    private boolean isLast;

    /**
     * 当前页的第一条记录的索引（从0开始）
     */
    private long fromIndex;

    /**
     * 当前页的最后一条记录的索引
     */
    private long toIndex;

    /**
     * 查询耗时（毫秒）
     */
    private Long costTime;

    /**
     * 当前时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime timestamp;

    // ==================== 构造函数 ====================

    public PageResponse() {
    }

    // ==================== Getter & Setter ====================

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setFirst(boolean first) {
        isFirst = first;
    }

    public boolean isLast() {
        return isLast;
    }

    public void setLast(boolean last) {
        isLast = last;
    }

    public long getFromIndex() {
        return fromIndex;
    }

    public void setFromIndex(long fromIndex) {
        this.fromIndex = fromIndex;
    }

    public long getToIndex() {
        return toIndex;
    }

    public void setToIndex(long toIndex) {
        this.toIndex = toIndex;
    }

    public Long getCostTime() {
        return costTime;
    }

    public void setCostTime(Long costTime) {
        this.costTime = costTime;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 创建空的分页响应
     */
    public static <T> PageResponse<T> empty(int page, int size) {
        return of(List.of(), 0, page, size);
    }

    /**
     * 创建分页响应
     */
    public static <T> PageResponse<T> of(List<T> records, long total, int page, int size) {
        PageResponse<T> response = new PageResponse<>();
        response.setRecords(records);
        response.setTotal(total);
        response.setPage(page);
        response.setSize(size);
        response.setTotalPages(calculateTotalPages(total, size));
        response.setHasNext(page < response.getTotalPages());
        response.setHasPrevious(page > 1);
        response.setFirst(page == 1);
        response.setLast(page >= response.getTotalPages());
        response.setFromIndex(total == 0 ? 0 : (page - 1) * size + 1);
        response.setToIndex(Math.min(page * size, (int) total));
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    /**
     * 创建分页响应（带耗时）
     */
    public static <T> PageResponse<T> of(List<T> records, long total, int page, int size, long costTime) {
        PageResponse<T> response = of(records, total, page, size);
        response.setCostTime(costTime);
        return response;
    }

    // ==================== 辅助方法 ====================

    /**
     * 计算总页数
     */
    private static int calculateTotalPages(long total, int size) {
        if (total <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) total / size);
    }

    /**
     * 判断是否为空（无数据）
     */
    public boolean isEmpty() {
        return records == null || records.isEmpty();
    }

    /**
     * 判断是否有数据
     */
    public boolean isNotEmpty() {
        return !isEmpty();
    }
}
