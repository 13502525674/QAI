package com.ape.apeadmin.mapper;

import com.ape.apeadmin.domain.ApeChatMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ApeChatMessageMapper extends BaseMapper<ApeChatMessage> {

    @Select("SELECT * FROM ape_chat_message WHERE message_type = 'group' AND (deleted_by IS NULL OR deleted_by NOT LIKE CONCAT('%', #{userId}, '%')) ORDER BY create_time ASC LIMIT #{limit}")
    List<ApeChatMessage> selectGroupMessages(@Param("limit") int limit, @Param("userId") String userId);

    @Select("SELECT * FROM ape_chat_message WHERE ((sender_id = #{userId1} AND receiver_id = #{userId2}) OR (sender_id = #{userId2} AND receiver_id = #{userId1})) AND (deleted_by IS NULL OR deleted_by NOT LIKE CONCAT('%', #{userId1}, '%')) ORDER BY create_time ASC")
    List<ApeChatMessage> selectPrivateMessages(@Param("userId1") String userId1, @Param("userId2") String userId2);

    @Select("SELECT * FROM ape_chat_message WHERE message_type = 'group' AND (deleted_by IS NULL OR deleted_by NOT LIKE CONCAT('%', #{userId}, '%'))")
    List<ApeChatMessage> selectAllVisibleGroupMessages(@Param("userId") String userId);

    @Select("SELECT * FROM ape_chat_message WHERE ((sender_id = #{userId1} AND receiver_id = #{userId2}) OR (sender_id = #{userId2} AND receiver_id = #{userId1})) AND (deleted_by IS NULL OR deleted_by NOT LIKE CONCAT('%', #{userId1}, '%'))")
    List<ApeChatMessage> selectAllVisiblePrivateMessages(@Param("userId1") String userId1, @Param("userId2") String userId2);
}
