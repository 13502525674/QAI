package com.ape.apeadmin.service.ai.tools;

import com.ape.apeadmin.service.ai.RAGService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class PhysicsToolsTest {

    @Test
    public void testCalculateFormula() {
        PhysicsTools tools = new PhysicsTools();
        var calculator = tools.calculateFormula();
        
        // Test addition
        var result1 = calculator.apply(new PhysicsTools.CalculatorRequest("1 + 1"));
        assertEquals("2.0", result1.result());

        // Test multiplication
        var result2 = calculator.apply(new PhysicsTools.CalculatorRequest("3 * 4"));
        assertEquals("12.0", result2.result());

        // Test complex expression
        var result3 = calculator.apply(new PhysicsTools.CalculatorRequest("(2 + 3) * 4"));
        assertEquals("20.0", result3.result());
    }

    @Test
    public void testSearchKnowledge() {
        // Mock RAGService
        RAGService ragService = Mockito.mock(RAGService.class);
        PhysicsTools tools = new PhysicsTools();
        var searchTool = tools.searchKnowledge(ragService);

        // Case 1: Results found
        when(ragService.retrieve(anyString())).thenReturn(Arrays.asList("Newton's First Law", "Inertia"));
        
        var response1 = searchTool.apply(new PhysicsTools.SearchRequest("Newton"));
        assertTrue(response1.content().contains("Found relevant info"));
        assertTrue(response1.content().contains("Newton's First Law"));
        assertTrue(response1.content().contains("Inertia"));

        // Case 2: No results
        when(ragService.retrieve(anyString())).thenReturn(Collections.emptyList());
        
        var response2 = searchTool.apply(new PhysicsTools.SearchRequest("Quantum"));
        assertTrue(response2.content().contains("No relevant information found"));
    }
}
