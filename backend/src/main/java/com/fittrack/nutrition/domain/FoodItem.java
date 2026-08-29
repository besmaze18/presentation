package com.fittrack.nutrition.domain;

import com.fittrack.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * A single component of a logged meal - "300 g chicken breast" within a three-item dinner. AI
 * analysis produces one of these per detected food, so the user can correct individual portions
 * rather than a single opaque total.
 */
@Entity
@Table(name = "food_items")
public class FoodItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "food_entry_id", nullable = false)
    private FoodEntry foodEntry;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "quantity", precision = 8, scale = 2)
    private BigDecimal quantity;

    @Column(name = "unit", length = 32)
    private String unit;

    @Embedded
    private Macros macros = Macros.zero();

    protected FoodItem() {}

    public FoodItem(String name, BigDecimal quantity, String unit, Macros macros) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.macros = macros == null ? Macros.zero() : macros;
    }

    void attachTo(FoodEntry entry, int position) {
        this.foodEntry = entry;
        this.position = position;
    }

    public FoodEntry getFoodEntry() {
        return foodEntry;
    }

    public int getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public Macros getMacros() {
        return macros;
    }
}
