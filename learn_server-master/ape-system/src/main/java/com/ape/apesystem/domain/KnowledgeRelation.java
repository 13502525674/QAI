package com.ape.apesystem.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@TableName("knowledge_relation")
public class KnowledgeRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String fromKpId;

    private String toKpId;

    private String relationType;

    private Float weight;

    private String description;

    private Date createTime;

    @TableField(exist = false)
    private String fromKpName;

    @TableField(exist = false)
    private String toKpName;
}
