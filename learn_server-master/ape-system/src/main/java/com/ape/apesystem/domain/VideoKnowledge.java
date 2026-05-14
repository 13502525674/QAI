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
@TableName("video_knowledge")
public class VideoKnowledge implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String chapterId;

    private String kpId;

    private Float weight;

    private Date createTime;

    @TableField(exist = false)
    private String kpName;

    @TableField(exist = false)
    private String chapterName;
}
