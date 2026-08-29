package com.fittrack.training.service;

import com.fittrack.common.exception.BadRequestException;
import com.fittrack.common.exception.NotFoundException;
import com.fittrack.training.domain.DailyTrainingTotals;
import com.fittrack.training.domain.Exercise;
import com.fittrack.training.domain.TrainingSession;
import com.fittrack.training.domain.TrainingSessionRepository;
import com.fittrack.training.domain.TrainingSource;
import com.fittrack.training.dto.AnnotateTrainingSessionRequest;
import com.fittrack.training.dto.ExerciseDto;
import com.fittrack.training.dto.TrainingSessionResponse;
import com.fittrack.training.dto.UpsertTrainingSessionRequest;
import com.fittrack.user.service.UserService;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrainingService {

    private final TrainingSessionRepository repository;
    private final UserService userService;

    public TrainingService(TrainingSessionRepository repository, UserService userService) {
        this.repository = repository;
        this.userService = userService;
    }

    @Transactional
    public TrainingSessionResponse create(UUID userId, UpsertTrainingSessionRequest request) {
        ZoneId zone = userService.zoneOf(userId);
        TrainingSession session = new TrainingSession(
                userService.requireUser(userId),
                request.title().trim(),
                request.startedAt(),
                LocalDate.ofInstant(request.startedAt(), zone));
        session.setSource(TrainingSource.MANUAL);
        applyEditableFields(session, request);
        return TrainingSessionResponse.from(repository.save(session));
    }

    @Transactional
    public TrainingSessionResponse update(
            UUID userId, UUID sessionId, UpsertTrainingSessionRequest request) {
        TrainingSession session = require(userId, sessionId);
        if (session.isImported()) {
            throw new BadRequestException(
                    "Imported sessions cannot be edited in full - use the annotate endpoint instead");
        }
        ZoneId zone = userService.zoneOf(userId);
        session.setTitle(request.title().trim());
        session.setStartedAt(request.startedAt());
        session.setSessionDate(LocalDate.ofInstant(request.startedAt(), zone));
        applyEditableFields(session, request);
        return TrainingSessionResponse.from(repository.save(session));
    }

    /** Adds the user's own context to an imported session without touching measured values. */
    @Transactional
    public TrainingSessionResponse annotate(
            UUID userId, UUID sessionId, AnnotateTrainingSessionRequest request) {
        TrainingSession session = require(userId, sessionId);
        if (request.title() != null && !request.title().isBlank()) {
            session.setTitle(request.title().trim());
        }
        if (request.category() != null) {
            session.setCategory(request.category());
        }
        if (request.perceivedExertion() != null) {
            session.setPerceivedExertion(request.perceivedExertion());
        }
        if (request.notes() != null) {
            session.setNotes(request.notes());
        }
        return TrainingSessionResponse.from(repository.save(session));
    }

    @Transactional
    public void delete(UUID userId, UUID sessionId) {
        TrainingSession session = require(userId, sessionId);
        if (session.isImported()) {
            throw new BadRequestException(
                    "Imported sessions are owned by the wearable integration and cannot be deleted here");
        }
        repository.delete(session);
    }

    @Transactional(readOnly = true)
    public TrainingSessionResponse get(UUID userId, UUID sessionId) {
        return TrainingSessionResponse.from(require(userId, sessionId));
    }

    @Transactional(readOnly = true)
    public List<TrainingSessionResponse> list(UUID userId, int page, int size) {
        return repository
                .findByUserIdOrderByStartedAtDesc(
                        userId, PageRequest.of(Math.max(0, page), Math.clamp(size, 1, 100)))
                .map(TrainingSessionResponse::summary)
                .getContent();
    }

    @Transactional(readOnly = true)
    public List<TrainingSessionResponse> forDay(UUID userId, LocalDate date) {
        return repository.findByUserIdAndSessionDateOrderByStartedAtAsc(userId, date).stream()
                .map(TrainingSessionResponse::summary)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TrainingSessionResponse> forRange(UUID userId, LocalDate from, LocalDate to) {
        return repository.findByUserIdAndSessionDateBetweenOrderByStartedAtAsc(userId, from, to).stream()
                .map(TrainingSessionResponse::summary)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DailyTrainingTotals> aggregateByDay(UUID userId, LocalDate from, LocalDate to) {
        return repository.aggregateByDay(userId, from, to);
    }

    private TrainingSession require(UUID userId, UUID sessionId) {
        return repository
                .findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> NotFoundException.of("Training session", sessionId));
    }

    private void applyEditableFields(TrainingSession session, UpsertTrainingSessionRequest request) {
        session.setCategory(request.category());
        session.setSportLabel(request.sportLabel());
        session.setDurationMinutes(request.durationMinutes());
        session.setEndedAt(request.startedAt().plus(Duration.ofMinutes(request.durationMinutes())));
        session.setPerceivedExertion(request.perceivedExertion());
        session.setCaloriesKcal(request.caloriesKcal());
        session.setDistanceMeters(request.distanceMeters());
        session.setNotes(request.notes());

        List<Exercise> exercises = request.exercises() == null
                ? List.of()
                : request.exercises().stream().map(ExerciseDto::toEntity).toList();
        session.replaceExercises(exercises);
    }
}
