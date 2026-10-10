package com.svjmart.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AiChatResponseDto {

    private String replyText;
    private String intentType; // "RECIPE_BASKET", "BUDGET_OPTIMIZER", "CATEGORY_SEARCH", "GENERAL_ASSIST"
    private String rationale;
    private BigDecimal totalEstimatedPrice = BigDecimal.ZERO;
    private List<ProductSummaryDto> suggestedProducts = new ArrayList<>();

    public AiChatResponseDto() {
    }

    public AiChatResponseDto(String replyText, String intentType, String rationale) {
        this.replyText = replyText;
        this.intentType = intentType;
        this.rationale = rationale;
    }

    public String getReplyText() {
        return replyText;
    }

    public void setReplyText(String replyText) {
        this.replyText = replyText;
    }

    public String getIntentType() {
        return intentType;
    }

    public void setIntentType(String intentType) {
        this.intentType = intentType;
    }

    public String getRationale() {
        return rationale;
    }

    public void setRationale(String rationale) {
        this.rationale = rationale;
    }

    public BigDecimal getTotalEstimatedPrice() {
        return totalEstimatedPrice;
    }

    public void setTotalEstimatedPrice(BigDecimal totalEstimatedPrice) {
        this.totalEstimatedPrice = totalEstimatedPrice;
    }

    public List<ProductSummaryDto> getSuggestedProducts() {
        return suggestedProducts;
    }

    public void setSuggestedProducts(List<ProductSummaryDto> suggestedProducts) {
        this.suggestedProducts = suggestedProducts;
    }

    public void addProduct(ProductSummaryDto product) {
        this.suggestedProducts.add(product);
        if (product.getPrice() != null) {
            this.totalEstimatedPrice = this.totalEstimatedPrice.add(product.getPrice());
        }
    }
}
