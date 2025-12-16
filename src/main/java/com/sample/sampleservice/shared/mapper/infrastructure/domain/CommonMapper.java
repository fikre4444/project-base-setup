package com.sample.sampleservice.shared.mapper.infrastructure.domain;

import org.mapstruct.Mapper;

import java.time.*;
import java.util.Objects;

@Mapper(componentModel = "spring")
public interface CommonMapper {

    default Instant toInstant(LocalDate date) {
        return date.atTime(LocalTime.of(0, 0, 0, 0)).toInstant(ZoneOffset.UTC);
    }

    default OffsetDateTime toOffsetDateTime(LocalDateTime dateTime) {
        if (dateTime != null) {
            ZoneOffset zoneOffSet = ZoneId.systemDefault().getRules().getOffset(dateTime);
            return dateTime.atOffset(zoneOffSet);
        }
        return null;
    }

    default LocalDateTime toOffsetDateTime(OffsetDateTime offsetDateTime) {
        if (offsetDateTime != null) {
            ZonedDateTime zoned = offsetDateTime.atZoneSameInstant(ZoneId.systemDefault());
            return zoned.toLocalDateTime();
        }
        return null;
    }

    default OffsetDateTime toOffsetDateTime(LocalDate dateTime) {
        if (dateTime != null) {
            LocalDateTime localDateTime = dateTime.atStartOfDay();
            ZoneOffset zoneOffSet = ZoneId.systemDefault().getRules().getOffset(localDateTime);
            return localDateTime.atOffset(zoneOffSet);
        }
        return null;
    }

    default LocalDate toLocalDate(OffsetDateTime offsetDateTime) {
        if (offsetDateTime != null) {
            ZonedDateTime zoned = offsetDateTime.atZoneSameInstant(ZoneId.systemDefault());
            return zoned.toLocalDate();
        }
        return null;
    }

    default LocalDate toLocalDate(Instant instant) {
        return instant.atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
