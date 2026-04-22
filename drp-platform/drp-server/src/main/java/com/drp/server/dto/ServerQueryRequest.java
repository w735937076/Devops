package com.drp.server.dto;

import lombok.Data;

@Data
public class ServerQueryRequest {
    private String keyword;
    private String group;
    private Integer status;
    private Integer page = 1;
    private Integer pageSize = 10;
}
