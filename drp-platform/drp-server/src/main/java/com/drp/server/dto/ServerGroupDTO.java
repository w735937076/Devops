package com.drp.server.dto;

import lombok.Data;

@Data
public class ServerGroupDTO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Integer sort;
}
