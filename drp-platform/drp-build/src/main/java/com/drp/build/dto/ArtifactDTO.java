package com.drp.build.dto;

import lombok.Data;

@Data
public class ArtifactDTO {
    private String name;
    private String path;
    private Long size;
    private String downloadUrl;
}
