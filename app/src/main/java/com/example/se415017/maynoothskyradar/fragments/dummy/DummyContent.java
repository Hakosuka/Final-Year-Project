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
        ITEM_MAP.put(item.id, item);
    }

    private static DummyItem createDummyItem(int position) {
        return new DummyItem(String.valueOf(position), "Item " + position, makeDetails(position));
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
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        char[] icaoHexAddr;
        char[] flightNum;
        int altitude;
        int speed;
        int track;
        int odd_cprlat;
        int odd_cprlon;
        int even_cprlat;
        int even_cprlon;
        double lat, lon;
        public DummyItem(char[] icaoHexAddr, char[] flightNum, int altitude, int speed, int track, int even_cprlat, int even_cprlon, int odd_cprlat, int odd_cprlon) {
            this.icaoHexAddr = icaoHexAddr;
            this.flightNum = flightNum;
            this.altitude = altitude;
            this.speed = speed;
            this.track = track;
            this.odd_cprlat = odd_cprlat;
            this.odd_cprlon = odd_cprlon;
            this.even_cprlat = even_cprlat;
            this.even_cprlon = even_cprlon;
        }

        @Override
        public String toString() {
            return icaoHexAddr + " " + flightNum + " " +
                    Integer.toString(altitude) + " " +
                    Integer.toString(speed) + " " +
                    Integer.toString(track) + " " +
                    Integer.toString(odd_cprlat) + " " +
                    Integer.toString(odd_cprlon) + " " +
                    Integer.toString(even_cprlat) + " " +
                    Integer.toString(even_cprlon);
        }
    }
}
