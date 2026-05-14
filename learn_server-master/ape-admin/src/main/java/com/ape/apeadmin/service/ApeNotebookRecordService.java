package com.ape.apeadmin.service;

import com.ape.apeadmin.domain.ApeNotebookRecord;
import java.util.List;

public interface ApeNotebookRecordService {
    int deleteByPrimaryKey(String id);
    int insert(ApeNotebookRecord record);
    int insertSelective(ApeNotebookRecord record);
    ApeNotebookRecord selectByPrimaryKey(String id);
    int updateByPrimaryKeySelective(ApeNotebookRecord record);
    int updateByPrimaryKey(ApeNotebookRecord record);
    List<ApeNotebookRecord> selectByUserId(String userId);
}
