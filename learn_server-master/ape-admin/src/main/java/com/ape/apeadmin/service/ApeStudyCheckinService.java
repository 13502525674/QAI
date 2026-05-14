package com.ape.apeadmin.service;

import java.util.List;

public interface ApeStudyCheckinService {
    int insert(String userId, String checkinDate);
    List<String> selectCheckinDatesByUserId(String userId);
    boolean hasCheckedIn(String userId, String checkinDate);
}
