package com.fittrack.training.domain;

import com.fittrack.common.domain.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.BatchSize;

/** A named movement within a session, owning its ordered sets. */
@Entity
@Table(name = "exercises")
public class Exercise extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_session_id", nullable = false)
    private TrainingSession trainingSession;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "name", nullable = false, length = 160)
    private String name;

    @Column(name = "notes", length = 500)
    private String notes;

    // Batch-loaded so rendering a session's exercises issues one extra query rather than one
    // per exercise.
    @OneToMany(
            mappedBy = "exercise",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @OrderBy("position ASC")
    @BatchSize(size = 50)
    private List<ExerciseSet> sets = new ArrayList<>();

    protected Exercise() {}

    public Exercise(String name, String notes) {
        this.name = name;
        this.notes = notes;
    }

    void attachTo(TrainingSession session, int position) {
        this.trainingSession = session;
        this.position = position;
    }

    public void replaceSets(List<ExerciseSet> newSets) {
        sets.clear();
        int setPosition = 0;
        for (ExerciseSet set : newSets) {
            set.attachTo(this, setPosition++);
            sets.add(set);
        }
    }

    public TrainingSession getTrainingSession() {
        return trainingSession;
    }

    public int getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }

    public String getNotes() {
        return notes;
    }

    public List<ExerciseSet> getSets() {
        return sets;
    }
}
