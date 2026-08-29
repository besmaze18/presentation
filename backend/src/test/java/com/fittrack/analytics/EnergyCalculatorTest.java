package com.fittrack.analytics;

import static org.assertj.core.api.Assertions.assertThat;

import com.fittrack.analytics.service.EnergyCalculator;
import com.fittrack.user.domain.ActivityLevel;
import com.fittrack.user.domain.Sex;
import com.fittrack.user.domain.User;
import com.fittrack.user.domain.UserSettings;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class EnergyCalculatorTest {

    private final EnergyCalculator calculator = new EnergyCalculator();

    private static UserSettings settings(Sex sex, String heightCm, LocalDate birthDate) {
        UserSettings settings = new UserSettings(new User("a@b.test", "hash", "Test"));
        settings.setSex(sex);
        settings.setHeightCm(heightCm == null ? null : new BigDecimal(heightCm));
        settings.setBirthDate(birthDate);
        return settings;
    }

    @Test
    void computesMifflinStJeorForAMaleUser() {
        // 10*80 + 6.25*180 - 5*30 + 5 = 800 + 1125 - 150 + 5 = 1780
        BigDecimal bmr = calculator.basalMetabolicRate(
                settings(Sex.MALE, "180.0", LocalDate.of(1996, 1, 1)),
                new BigDecimal("80.0"),
                LocalDate.of(2026, 3, 10));

        assertThat(bmr).isEqualByComparingTo("1780");
    }

    @Test
    void computesMifflinStJeorForAFemaleUser() {
        // 10*62 + 6.25*168 - 5*35 - 161 = 620 + 1050 - 175 - 161 = 1334
        BigDecimal bmr = calculator.basalMetabolicRate(
                settings(Sex.FEMALE, "168.0", LocalDate.of(1991, 1, 1)),
                new BigDecimal("62.0"),
                LocalDate.of(2026, 3, 10));

        assertThat(bmr).isEqualByComparingTo("1334");
    }

    @Test
    void returnsNullRatherThanGuessingWhenAnInputIsMissing() {
        assertThat(calculator.basalMetabolicRate(
                        settings(Sex.MALE, null, LocalDate.of(1996, 1, 1)),
                        new BigDecimal("80.0"),
                        LocalDate.of(2026, 3, 10)))
                .isNull();

        assertThat(calculator.basalMetabolicRate(
                        settings(Sex.MALE, "180.0", null), new BigDecimal("80.0"), LocalDate.of(2026, 3, 10)))
                .isNull();

        assertThat(calculator.basalMetabolicRate(
                        settings(Sex.MALE, "180.0", LocalDate.of(1996, 1, 1)), null, LocalDate.of(2026, 3, 10)))
                .isNull();
    }

    @Test
    void appliesTheActivityMultiplierToProduceTdee() {
        BigDecimal tdee =
                calculator.totalDailyEnergyExpenditure(new BigDecimal("1780"), ActivityLevel.MODERATE);
        // 1780 * 1.55 = 2759
        assertThat(tdee).isEqualByComparingTo("2759");
    }

    @Test
    void tdeeIsNullWhenBmrIsUnknown() {
        assertThat(calculator.totalDailyEnergyExpenditure(null, ActivityLevel.ACTIVE)).isNull();
    }

    @Test
    void convertsWhoopKilojoulesToKilocalories() {
        // 8288.297 kJ * 0.2390057 = 1980.94... -> 1981
        assertThat(calculator.kilojoulesToKilocalories(new BigDecimal("8288.297")))
                .isEqualByComparingTo("1981");
        assertThat(calculator.kilojoulesToKilocalories(null)).isNull();
    }

    @Test
    void energyBalanceIsIntakeMinusExpenditure() {
        assertThat(calculator.energyBalance(new BigDecimal("2400"), new BigDecimal("2759")))
                .isEqualByComparingTo("-359");
        assertThat(calculator.energyBalance(new BigDecimal("3000"), new BigDecimal("2759")))
                .isEqualByComparingTo("241");
        assertThat(calculator.energyBalance(new BigDecimal("2400"), null)).isNull();
    }
}
