package com.fittrack.nutrition.domain;

import com.fittrack.common.domain.BaseEntity;
import com.fittrack.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * A reusable food or meal. Macros are stored per reference serving; logging a different quantity
 * scales them, which keeps one row usable for any portion size.
 */
@Entity
@Table(name = "saved_foods")
public class SavedFood extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "brand", length = 120)
    private String brand;

    /** The quantity the stored macros describe, e.g. 100 with unit "g". */
    @Column(name = "serving_quantity", nullable = false, precision = 8, scale = 2)
    private BigDecimal servingQuantity = BigDecimal.ONE;

    @Column(name = "serving_unit", nullable = false, length = 32)
    private String servingUnit = "serving";

    @Embedded
    private Macros macros = Macros.zero();

    @Enumerated(EnumType.STRING)
    @Column(name = "default_meal_type", length = 24)
    private MealType defaultMealType;

    @Column(name = "usage_count", nullable = false)
    private long usageCount = 0;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    protected SavedFood() {}

    public SavedFood(User user, String name) {
        this.user = user;
        this.name = name;
    }

    public void recordUsage(Instant at) {
        this.usageCount++;
        this.lastUsedAt = at;
    }

    public User getUser() {
        return user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public BigDecimal getServingQuantity() {
        return servingQuantity;
    }

    public void setServingQuantity(BigDecimal servingQuantity) {
        this.servingQuantity = servingQuantity;
    }

    public String getServingUnit() {
        return servingUnit;
    }

    public void setServingUnit(String servingUnit) {
        this.servingUnit = servingUnit;
    }

    public Macros getMacros() {
        return macros;
    }

    public void setMacros(Macros macros) {
        this.macros = macros == null ? Macros.zero() : macros;
    }

    public MealType getDefaultMealType() {
        return defaultMealType;
    }

    public void setDefaultMealType(MealType defaultMealType) {
        this.defaultMealType = defaultMealType;
    }

    public long getUsageCount() {
        return usageCount;
    }

    public Instant getLastUsedAt() {
        return lastUsedAt;
    }
}
