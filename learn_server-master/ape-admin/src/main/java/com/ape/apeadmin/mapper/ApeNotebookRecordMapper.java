package com.ape.apeadmin.mapper;

import com.ape.apeadmin.domain.ApeNotebookRecord;
import java.util.List;

public interface ApeNotebookRecordMapper {
    int deleteByPrimaryKey(String id);
    int insert(ApeNotebookRecord record);
    int insertSelective(ApeNotebookRecord record);
    ApeNotebookRecord selectByPrimaryKey(String id);
    int updateByPrimaryKeySelective(ApeNotebookRecord record);
    int updateByPrimaryKey(ApeNotebookRecord record);
    List<ApeNotebookRecord> selectByUserId(String userId);
}
