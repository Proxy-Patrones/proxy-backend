package com.aicloud.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/** Reloj de la aplicacion en una zona horaria fija (Colombia por defecto). */
public class AppClock {

    private final ZoneId zone;

    public AppClock(ZoneId zone) {
        this.zone = zone;
    }

    public LocalDate today() {
        return LocalDate.now(zone);
    }

    /** Segundos que faltan para el siguiente minuto exacto (cuando el scheduler resetea). */
    public long secondsUntilNextMinute() {
        return 60 - ZonedDateTime.now(zone).getSecond();
    }

    /** Primer dia del mes siguiente a las 00:00 (cuando se resetea la cuota mensual). */
    public LocalDateTime nextMonthlyReset() {
        return today().withDayOfMonth(1).plusMonths(1).atStartOfDay();
    }

    public ZoneId getZone() {
        return zone;
    }
}
