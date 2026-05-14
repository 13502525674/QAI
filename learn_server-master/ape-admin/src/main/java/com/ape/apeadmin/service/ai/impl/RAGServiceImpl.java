package com.ape.apeadmin.service.ai.impl;

import com.ape.apeadmin.service.ai.RAGService;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ai.vectorstore.SearchRequest;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.ai.vectorstore.SimpleVectorStore;

@Service
public class RAGServiceImpl implements RAGService {

    private static final Logger log = LoggerFactory.getLogger(RAGServiceImpl.class);
    private final Tika tika = new Tika();
    private final VectorStore vectorStore;
    private final TokenTextSplitter tokenTextSplitter;
    private final ResourceLoader resourceLoader;

    public RAGServiceImpl(VectorStore vectorStore, ResourceLoader resourceLoader) {
        this.vectorStore = vectorStore;
        this.tokenTextSplitter = new TokenTextSplitter();
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() {
        try {
            // 尝试加载持久化的向量库，避免重复消耗 Token
            File storeFile = new File("vectorstore.json");
            if (storeFile.exists() && vectorStore instanceof SimpleVectorStore) {
                ((SimpleVectorStore) vectorStore).load(storeFile);
                log.info("Loaded existing vector store from file, skipping ingestion.");
                return;
            }

            Resource resource = resourceLoader.getResource("classpath:knowledge/physics_basics.txt");
            if (resource.exists()) {
                String content = tika.parseToString(resource.getInputStream());
                Document document = new Document(content);
                document.getMetadata().put("filename", "physics_basics.txt");
                List<Document> splitDocuments = tokenTextSplitter.apply(Collections.singletonList(document));
                vectorStore.add(splitDocuments);
                log.info("Initialized knowledge base with physics_basics.txt");
                
                // 保存向量库到本地
                if (vectorStore instanceof SimpleVectorStore) {
                    ((SimpleVectorStore) vectorStore).save(storeFile);
                    log.info("Saved vector store to file.");
                }
            } else {
                log.warn("physics_basics.txt not found in classpath");
            }
        } catch (Exception e) {
            log.error("Failed to initialize knowledge base", e);
        }
    }

    @Override
    public void ingestDocument(MultipartFile file) {
        try {
            String content = tika.parseToString(file.getInputStream());
            log.info("Parsed document {}: {} chars", file.getOriginalFilename(), content.length());
            
            Document document = new Document(content);
            document.getMetadata().put("filename", file.getOriginalFilename());
            
            List<Document> splitDocuments = tokenTextSplitter.apply(Collections.singletonList(document));
            vectorStore.add(splitDocuments);
            
            log.info("Ingested {} segments from {}", splitDocuments.size(), file.getOriginalFilename());
        } catch (Exception e) {
            log.error("Failed to parse document", e);
            throw new RuntimeException("Document parsing failed", e);
        }
    }

    @Override
    public List<String> retrieve(String query) {
        List<Document> similarDocuments = vectorStore.similaritySearch(SearchRequest.query(query).withTopK(3));
        return similarDocuments.stream()
                .map(Document::getContent)
                .collect(Collectors.toList());
    }
}
