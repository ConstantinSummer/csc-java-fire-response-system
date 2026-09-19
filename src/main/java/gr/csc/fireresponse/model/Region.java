package gr.csc.fireresponse.model;

/** The 13 administrative regions of Greece, used to group incidents and stations. */
public enum Region {
    ATTICA("Attica"),
    CENTRAL_GREECE("Central Greece"),
    PELOPONNESE("Peloponnese"),
    WESTERN_GREECE("Western Greece"),
    THESSALY("Thessaly"),
    EPIRUS("Epirus"),
    CENTRAL_MACEDONIA("Central Macedonia"),
    EASTERN_MACEDONIA_THRACE("E. Macedonia-Thrace"),
    WESTERN_MACEDONIA("Western Macedonia"),
    IONIAN_ISLANDS("Ionian Islands"),
    NORTH_AEGEAN("North Aegean"),
    SOUTH_AEGEAN("South Aegean"),
    CRETE("Crete");

    private final String displayName;

    Region(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
