package com.fittrack.nutrition.domain;

import com.fittrack.common.domain.BaseEntity;
import com.fittrack.user.domain.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * One logged eating event. Totals are denormalised onto the entry so a day's summary is a single
 * aggregate query rather than a walk over every component item.
 *
 * <p>{@code entryDate} is the calendar date in the user's own time zone at the moment of logging.
 * Storing it explicitly keeps "what did I eat on Tuesday" stable even if the user later travels
 * or changes their configured zone.
 */
@Entity
@Table(name = "food_entries")
public class FoodEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 24)
    private MealType mealType = MealType.OTHER;

    @Column(name = "consumed_at", nullable = false)
    private Instant consumedAt;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "quantity", precision = 8, scale = 2)
    private java.math.BigDecimal quantity;

    @Column(name = "unit", length = 32)
    private String unit;

    @Embedded
    private Macros macros = Macros.zero();

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 24)
    private FoodEntrySource source = FoodEntrySource.MANUAL;

    /** Links back to the AI prediction this entry was confirmed from, when there was one. */
    @Column(name = "ai_analysis_id")
    private UUID aiAnalysisId;

    @Column(name = "saved_food_id")
    private UUID savedFoodId;

    @Column(name = "notes", length = 1000)
    private String notes;

    @OneToMany(
            mappedBy = "foodEntry",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @OrderBy("position ASC")
    private List<FoodItem> items = new ArrayList<>();

    protected FoodEntry() {}

    public FoodEntry(User user, String name, Instant consumedAt, LocalDate entryDate) {
        this.user = user;
        this.name = name;
        this.consumedAt = consumedAt;
        this.entryDate = entryDate;
    }

    public void replaceItems(List<FoodItem> newItems) {
        items.clear();
        int position = 0;
        for (FoodItem item : newItems) {
            item.attachTo(this, position++);
            items.add(item);
        }
    }

    /** Recomputes the entry totals from its components; a no-op for single-item entries. */
    public void recalculateTotalsFromItems() {
        if (items.isEmpty()) {
            return;
        }
        Macros total = Macros.zero();
        for (FoodItem item : items) {
            total = total.plus(item.getMacros());
        }
        this.macros = total;
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

    public MealType getMealType() {
        return mealType;
    }

    public void setMealType(MealType mealType) {
        this.mealType = mealType;
    }

    public Instant getConsumedAt() {
        return consumedAt;
    }

    public void setConsumedAt(Instant consumedAt) {
        this.consumedAt = consumedAt;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public java.math.BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(java.math.BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Macros getMacros() {
        return macros;
    }

    public void setMacros(Macros macros) {
        this.macros = macros == null ? Macros.zero() : macros;
    }

    public FoodEntrySource getSource() {
        return source;
    }

    public void setSource(FoodEntrySource source) {
        this.source = source;
    }

    public UUID getAiAnalysisId() {
        return aiAnalysisId;
    }

    public void setAiAnalysisId(UUID aiAnalysisId) {
        this.aiAnalysisId = aiAnalysisId;
    }

    public UUID getSavedFoodId() {
        return savedFoodId;
    }

    public void setSavedFoodId(UUID savedFoodId) {
        this.savedFoodId = savedFoodId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<FoodItem> getItems() {
        return items;
    }
}
