package com.svjmart.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AiChatRequestDto {
    private String query;
    private BigDecimal budget;
    private String dietPreference = "ANY"; // "ANY", "VEG", "NON_VEG"
    private String householdType = "FAMILY"; // "INDIVIDUAL", "COUPLE", "FAMILY"
    private Long targetCategoryId;

    public AiChatRequestDto() {
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getMessage() {
        return query;
    }

    public void setMessage(String message) {
        if (this.query == null || this.query.trim().isEmpty()) {
            this.query = message;
        }
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public String getDietPreference() {
        return dietPreference;
    }

    public void setDietPreference(String dietPreference) {
        this.dietPreference = dietPreference;
    }

    public String getHouseholdType() {
        return householdType;
    }

    public void setHouseholdType(String householdType) {
        this.householdType = householdType;
    }

    public Long getTargetCategoryId() {
        return targetCategoryId;
    }

    public void setTargetCategoryId(Long targetCategoryId) {
        this.targetCategoryId = targetCategoryId;
    }
}
