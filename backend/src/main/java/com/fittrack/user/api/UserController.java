package com.fittrack.user.api;

import com.fittrack.common.security.AuthenticatedUser;
import com.fittrack.common.security.CurrentUser;
import com.fittrack.user.dto.NutritionGoalResponse;
import com.fittrack.user.dto.UpdateProfileRequest;
import com.fittrack.user.dto.UpdateUserSettingsRequest;
import com.fittrack.user.dto.UpsertNutritionGoalRequest;
import com.fittrack.user.dto.UserResponse;
import com.fittrack.user.dto.UserSettingsResponse;
import com.fittrack.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Profile, settings and nutrition targets")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "The authenticated user's profile")
    public UserResponse me(@CurrentUser AuthenticatedUser currentUser) {
        return UserResponse.from(userService.requireUser(currentUser.getId()));
    }

    @PutMapping("/me")
    @Operation(summary = "Update the authenticated user's profile")
    public UserResponse updateMe(
            @CurrentUser AuthenticatedUser currentUser, @Valid @RequestBody UpdateProfileRequest request) {
        return UserResponse.from(userService.updateProfile(currentUser.getId(), request));
    }

    @GetMapping("/me/settings")
    @Operation(summary = "Unit system, time zone and the inputs used for BMR estimation")
    public UserSettingsResponse settings(@CurrentUser AuthenticatedUser currentUser) {
        return UserSettingsResponse.from(userService.requireSettings(currentUser.getId()));
    }

    @PutMapping("/me/settings")
    @Operation(summary = "Update settings")
    public UserSettingsResponse updateSettings(
            @CurrentUser AuthenticatedUser currentUser,
            @Valid @RequestBody UpdateUserSettingsRequest request) {
        return UserSettingsResponse.from(userService.updateSettings(currentUser.getId(), request));
    }

    @GetMapping("/me/goals")
    @Operation(summary = "The nutrition goal history, most recent first")
    public List<NutritionGoalResponse> goals(
            @CurrentUser AuthenticatedUser currentUser,
            @RequestParam(defaultValue = "20") int limit) {
        return userService.goalHistory(currentUser.getId(), limit).stream()
                .map(NutritionGoalResponse::from)
                .toList();
    }

    @PutMapping("/me/goals")
    @Operation(summary = "Create or replace the nutrition goal effective from a given date")
    public NutritionGoalResponse upsertGoal(
            @CurrentUser AuthenticatedUser currentUser,
            @Valid @RequestBody UpsertNutritionGoalRequest request) {
        return NutritionGoalResponse.from(userService.upsertGoal(currentUser.getId(), request));
    }
}
