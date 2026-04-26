package com.drp.server.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.drp.common.util.AesEncryptUtil;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("srv_server")
public class Server {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;

    private String hostname;

    private Integer port = 22;

    private String username;

    private String password;

    private String privateKey;

    private String privateKeyPassphrase;

    @TableField("`groups`")
    private String groups;

    private String workDir = "/opt/drp";

    private String backupDir = "/opt/drp/backup";

    private Integer status = 1;

    private String env;

    private String tags;

    private String remark;

    private LocalDateTime lastHeartbeat;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    @TableLogic
    private Boolean deleted;

    public String getDecryptedPassword() {
        if (password == null || password.isBlank()) {
            return null;
        }
        try {
            return AesEncryptUtil.decrypt(password);
        } catch (Exception e) {
            return password;
        }
    }

    public String getDecryptedPrivateKey() {
        if (privateKey == null || privateKey.isBlank()) {
            return null;
        }
        try {
            return AesEncryptUtil.decrypt(privateKey);
        } catch (Exception e) {
            return privateKey;
        }
    }
}
