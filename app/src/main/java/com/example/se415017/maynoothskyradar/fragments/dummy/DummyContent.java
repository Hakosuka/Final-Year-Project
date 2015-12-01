package com.example.se415017.maynoothskyradar.fragments.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<DummyItem> ITEMS = new ArrayList<DummyItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    private static void addItem(DummyItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.icaoHexAddr, item);
    }

    private static DummyItem createDummyItem(int position) {
        return new DummyItem("AAAA" + Integer.toString(position),
                "EI" + String.valueOf(position),
                30000, 300, "50");
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * The dummy data is in SBS-1 (BaseStation) format
     */
    public static class DummyItem {
        // TODO: replace all of this stuff (from dump1090) with SBS-1 data
        boolean isAirbourne = true; // Let's just assume in the dummy data that all detected planes are airbourne
        public String icaoHexAddr;
        String flightNum;
        int impAltitude; // imperial
        double metAltitude; // metric
        int speed;
        String track;
        public DummyItem(String icaoHexAddr, String flightNum, int altitude, int speed, String track) {
            this.icaoHexAddr = icaoHexAddr;
            this.flightNum = flightNum;
            this.impAltitude = altitude;
            metAltitude = ((double)altitude)*0.3048; // 1 ft = 30.48cm
            this.speed = speed;
            this.track = track;
        }

        @Override
        public String toString() {
            if(isAirbourne)
                return "Y, " + icaoHexAddr + ", " + flightNum + ", " +
                    Integer.toString(impAltitude) + "ft, " +
                    Double.toString(metAltitude) + "m, " +
                    Integer.toString(speed) + ", " +
                    track;
            else {
                return "N, " + icaoHexAddr + ", " + flightNum + ", " +
                        Integer.toString(impAltitude) + "ft, " +
                        Double.toString(metAltitude) + "m, " +
                        Integer.toString(speed) + ", " +
                        track;
            }
        }
    }
}
