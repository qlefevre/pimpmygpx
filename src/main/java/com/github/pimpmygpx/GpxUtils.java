package com.github.pimpmygpx;

import io.jenetics.jpx.*;

import java.time.*;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GpxUtils {

    public static GPX changeFinishTime(GPX gpx, LocalTime localTime) {
        Instant currentFinishTimeInstant = streamWayPoint(gpx,WayPoint::getTime).max(Instant::compareTo).get();
        int deltaMinutes = deltaMinutes(currentFinishTimeInstant,localTime);
        return addXMinutesToAllWayPoints(gpx, deltaMinutes);
    }

    public static GPX changeStartTime(GPX gpx, LocalTime localTime) {
        Instant currentStartTimeInstant = streamWayPoint(gpx,WayPoint::getTime).min(Instant::compareTo).get();
        int deltaMinutes = deltaMinutes(currentStartTimeInstant,localTime);
        return addXMinutesToAllWayPoints(gpx, deltaMinutes);
    }

    public static GPX changeDate(GPX gpx, LocalDate localdate) {
        Instant currentStartTime = streamWayPoint(gpx,WayPoint::getTime).min(Instant::compareTo).get();
        LocalDateTime currentStartTimeLdt = LocalDateTime.ofInstant(currentStartTime, ZoneId.systemDefault());
        int currentDayOfyear = currentStartTimeLdt.getDayOfYear();
        int finalDayOfYear = localdate.getDayOfYear();
        int deltaMinutes = (finalDayOfYear - currentDayOfyear) * 1440;
        return addXMinutesToAllWayPoints(gpx, deltaMinutes);
    }

    public static GPX moveWayPoints(GPX gpx, double deltaLatitude, double deltaLongitude) {
        // Modifier latitude longitude
        return applyToAllWayPoints(gpx, point ->
            point.toBuilder()
                .lat(point.getLatitude().doubleValue() + deltaLatitude)
                .lon(point.getLongitude().doubleValue() + deltaLongitude)
            .build());
    }

    public static GPX removeElevations(GPX gpx) {
        boolean allElevationsEqualZero =
                streamWayPoint(gpx,WayPoint::getElevation)
                        .allMatch(ele -> ele.intValue() == 0);

        // Si tous les éléments sont égals à zéro
        if(allElevationsEqualZero) {
            GPX rGPX = applyToAllWayPoints(gpx, point ->
                    point.toBuilder().ele(null).build());
            return rGPX;
        }else{
            return gpx;
        }
    }

    public static GPX updateMetadata(GPX gpx){
        // Mets à jour le temps au niveau des metadata
        Instant first = streamWayPoint(gpx,WayPoint::getTime).findFirst().get();
        Metadata metadata;
        if(gpx.getMetadata().isPresent()){
            metadata = gpx.getMetadata().get();
        }else{
            metadata = Metadata.builder().build();
        }
        metadata = metadata.toBuilder().time(first).build();
        GPX rGPX = gpx.toBuilder().metadata(metadata).build();
        return rGPX;
    }

    public static String info(GPX gpx) {
        Instant currentStartTimeInstant = streamWayPoint(gpx, WayPoint::getTime).min(Instant::compareTo).get();
        Instant currentFinishTimeInstant = streamWayPoint(gpx, WayPoint::getTime).max(Instant::compareTo).get();
        return "Heure de début: %s      Heure de fin: %s".formatted(currentStartTimeInstant,currentFinishTimeInstant);
    }

    private static int deltaMinutes(Instant totem, LocalTime localTime){
        int hours = localTime.get(ChronoField.CLOCK_HOUR_OF_DAY);
        int minutes = localTime.get(ChronoField.MINUTE_OF_HOUR);
        LocalDateTime instantLdt = LocalDateTime.ofInstant(totem, ZoneId.systemDefault());
        int currentFinishMinutes = 60 * instantLdt.getHour() + instantLdt.getMinute();
        int finalFinishMinutes = 60 * hours + minutes;
        return finalFinishMinutes - currentFinishMinutes;
    }

    private static GPX applyToAllWayPoints(GPX gpx, Function<WayPoint,WayPoint> function) {
        List<Track> tracks = gpx.tracks().map(track -> {
            List<TrackSegment> segments = track.segments().map(segment -> {
                List<WayPoint> points = segment.points().map(function).collect(Collectors.toList());
                return segment.toBuilder().points(points).build();
            }).collect(Collectors.toList());
            return track.toBuilder().segments(segments).build();
        }).collect(Collectors.toList());
        return gpx.toBuilder().tracks(tracks).build();
    }

    private static <T> Stream<T> streamWayPoint(GPX gpx, Function<WayPoint,Optional<T>> function) {
        // WayPoint::getTime
        return gpx.tracks()
                .flatMap(Track::segments)
                .flatMap(TrackSegment::points)
                .map(function)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private static GPX addXMinutesToAllWayPoints(GPX gpx, int plusMinutes) {
        // Calculer différence
        return applyToAllWayPoints(gpx, point ->
                point.toBuilder().time(point.getTime().get()
                        .plus(plusMinutes, ChronoUnit.MINUTES)).build());
    }
}
