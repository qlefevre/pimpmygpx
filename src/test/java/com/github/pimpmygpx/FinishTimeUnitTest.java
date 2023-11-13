package com.github.pimpmygpx;

import io.jenetics.jpx.GPX;
import io.jenetics.jpx.WayPoint;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *  Junit pour tester l'option --finish-time : changer l'heure de fin
 */
public class FinishTimeUnitTest {

    public static final String FILE = "/La_Landasienne_2023_5Km_20231015.gpx";

    @Test
    public void testOptionFinishTime() throws IOException {

        try(InputStream is = getClass().getResourceAsStream(FILE)) {
            GPX gpx = GPX.Reader.DEFAULT.read(is);

            // finish time 2023-10-15T08:18:40Z fichier 10h18 (GMT+2)

            // On change l'heure de fin à 15h15
            LocalTime localTime = LocalTime.parse("15:15", DateTimeFormatter.ISO_LOCAL_TIME);
            gpx = GpxUtils.changeFinishTime(gpx,localTime);

            // Tests;
            List<WayPoint> points = gpx.getTracks().get(0).getSegments().get(0).getPoints();
            assertEquals("2023-10-15T13:15:40Z", points.get(points.size()-1).getTime().get().toString(),
                    "Metadata - L'heure de fin n'est pas correcte.");
        }
    }

}