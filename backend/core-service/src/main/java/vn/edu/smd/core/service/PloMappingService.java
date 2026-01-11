package vn.edu.smd.core.service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for intelligent CLO-PLO mapping suggestions
 * Based on keyword matching and Bloom's taxonomy level
 */
@Service
public class PloMappingService {
    
    private static final Logger log = LoggerFactory.getLogger(PloMappingService.class);
    
    // PLO definitions with keywords (from database PLO1-PLO8)
    private static final Map<String, PloDefinition> PLO_DEFINITIONS = Map.of(
        "PLO1", new PloDefinition("PLO1", "Áp dụng kiến thức toán học, khoa học và kỹ thuật",
            Arrays.asList("toán", "khoa học", "kỹ thuật", "lý thuyết", "kiến thức", "công thức", "thuật toán")),
        "PLO2", new PloDefinition("PLO2", "Phân tích, thiết kế và đánh giá hệ thống phần mềm",
            Arrays.asList("phân tích", "thiết kế", "đánh giá", "hệ thống", "kiến trúc", "mô hình", "yêu cầu")),
        "PLO3", new PloDefinition("PLO3", "Thiết kế và quản lý cơ sở dữ liệu",
            Arrays.asList("cơ sở dữ liệu", "dữ liệu", "database", "sql", "query", "quản lý", "lưu trữ")),
        "PLO4", new PloDefinition("PLO4", "Lập trình thành thạo với nhiều ngôn ngữ",
            Arrays.asList("lập trình", "code", "ngôn ngữ", "java", "python", "c++", "thuật toán", "cài đặt")),
        "PLO5", new PloDefinition("PLO5", "Làm việc nhóm hiệu quả",
            Arrays.asList("nhóm", "team", "cộng tác", "phối hợp", "chia sẻ", "dự án")),
        "PLO6", new PloDefinition("PLO6", "Giao tiếp chuyên nghiệp",
            Arrays.asList("giao tiếp", "thuyết trình", "báo cáo", "văn bản", "trình bày", "presentation")),
        "PLO7", new PloDefinition("PLO7", "Tuân thủ đạo đức nghề nghiệp",
            Arrays.asList("đạo đức", "trách nhiệm", "bảo mật", "an toàn", "quyền riêng tư", "ethics")),
        "PLO8", new PloDefinition("PLO8", "Học hỏi liên tục và thích ứng",
            Arrays.asList("học hỏi", "nghiên cứu", "tự học", "công nghệ mới", "cập nhật", "phát triển"))
    );
    
    /**
     * Auto-suggest PLO mappings for a list of CLOs
     * @param clos List of CLO objects with code, description, bloomLevel
     * @return List of PLO mapping suggestions with contribution level
     */
    public List<Map<String, Object>> suggestPloMappings(List<Map<String, Object>> clos) {
        List<Map<String, Object>> mappings = new ArrayList<>();
        
        for (Map<String, Object> clo : clos) {
            String cloCode = (String) clo.get("code");
            String description = (String) clo.get("description");
            String bloomLevel = (String) clo.getOrDefault("bloomLevel", "Remember");
            
            if (description == null || description.isEmpty()) {
                log.warn("CLO {} has no description, skipping PLO mapping", cloCode);
                continue;
            }
            
            // Find matching PLOs based on keywords
            List<PloMatch> matches = findMatchingPlos(description, bloomLevel);
            
            // Convert top matches to mappings
            for (PloMatch match : matches) {
                Map<String, Object> mapping = new HashMap<>();
                mapping.put("cloCode", cloCode);
                mapping.put("ploCode", match.ploCode);
                mapping.put("contributionLevel", match.contributionLevel);
                mapping.put("confidence", match.confidence); // For debugging
                mappings.add(mapping);
                
                log.info("Mapped {} -> {} (level: {}, confidence: {:.2f})", 
                    cloCode, match.ploCode, match.contributionLevel, match.confidence);
            }
        }
        
        return mappings;
    }
    
    /**
     * Find PLOs that match the CLO description
     * Returns top 2-3 matches with contribution levels
     */
    private List<PloMatch> findMatchingPlos(String cloDescription, String bloomLevel) {
        String descLower = cloDescription.toLowerCase();
        List<PloMatch> allMatches = new ArrayList<>();
        
        // Calculate match score for each PLO
        for (Map.Entry<String, PloDefinition> entry : PLO_DEFINITIONS.entrySet()) {
            PloDefinition plo = entry.getValue();
            double score = calculateMatchScore(descLower, plo.keywords);
            
            if (score > 0.1) { // Minimum threshold
                allMatches.add(new PloMatch(
                    plo.code, 
                    score,
                    determineContributionLevel(score, bloomLevel)
                ));
            }
        }
        
        // Sort by score descending and take top 2-3
        allMatches.sort((a, b) -> Double.compare(b.confidence, a.confidence));
        return allMatches.stream().limit(3).collect(Collectors.toList());
    }
    
    /**
     * Calculate match score based on keyword presence
     */
    private double calculateMatchScore(String description, List<String> keywords) {
        int matchCount = 0;
        for (String keyword : keywords) {
            if (description.contains(keyword)) {
                matchCount++;
            }
        }
        return (double) matchCount / keywords.size();
    }
    
    /**
     * Determine contribution level (M/I/R) based on score and Bloom's level
     * M = Main (chính), I = Important (quan trọng), R = Related (liên quan)
     */
    private String determineContributionLevel(double score, String bloomLevel) {
        // High Bloom levels (Analyze, Evaluate, Create) + high score = Main
        if (isHighBloom(bloomLevel) && score >= 0.4) {
            return "M";
        }
        // Medium score or mid Bloom = Important
        if (score >= 0.25 || isMidBloom(bloomLevel)) {
            return "I";
        }
        // Low score = Related
        return "R";
    }
    
    private boolean isHighBloom(String level) {
        return level != null && (
            level.equalsIgnoreCase("Analyze") ||
            level.equalsIgnoreCase("Evaluate") ||
            level.equalsIgnoreCase("Create")
        );
    }
    
    private boolean isMidBloom(String level) {
        return level != null && (
            level.equalsIgnoreCase("Apply") ||
            level.equalsIgnoreCase("Understand")
        );
    }
    
    // Helper classes
    private static class PloDefinition {
        String code;
        String description;
        List<String> keywords;
        
        PloDefinition(String code, String description, List<String> keywords) {
            this.code = code;
            this.description = description;
            this.keywords = keywords;
        }
    }
    
    private static class PloMatch {
        String ploCode;
        double confidence;
        String contributionLevel;
        
        PloMatch(String ploCode, double confidence, String contributionLevel) {
            this.ploCode = ploCode;
            this.confidence = confidence;
            this.contributionLevel = contributionLevel;
        }
    }
}
