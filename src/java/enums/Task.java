package enums;

@SuppressWarnings("unused")
public enum Task {
    ZOOM_OUT(1, "Zoom-Out"),
    ZOOM_IN(2, "Zoom-In"),
    PAN(3, "Pan"),
    PAN_ZOOM(4, "Pan-Zoom"),
    SCROLL(5, "Scroll");

    private final int id;
    private final String text;

    Task(int id, String text) {
        this.id = id;
        this.text = text;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return text;
    }
}
