package com.ape.apeadmin.service.ai;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface RAGService {
    void ingestDocument(MultipartFile file);
    List<String> retrieve(String query);
}
