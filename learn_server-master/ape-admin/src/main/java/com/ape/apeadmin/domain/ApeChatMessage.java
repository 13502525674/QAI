package com.ape.apeadmin.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("ape_chat_message")
public class ApeChatMessage {

    @TableId
    private String id;

    private String senderId;

    private String senderName;

    private String receiverId;

    private String content;

    private String messageType;

    private Date createTime;

    private String deletedBy;
    
    /**
     * 消息内容类型: text-文本, image-图片, emoji-表情包
     */
    private String contentType = "text";
}
