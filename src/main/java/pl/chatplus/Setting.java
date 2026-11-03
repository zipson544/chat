package pl.chatplus;

public enum Setting {
    CHAT("chat"),
    PRIVATE("prywatne"),
    DEATH("smierc"),
    AUTO("auto"),
    GIFT("gift");

    public final String key;

    Setting(String key) {
        this.key = key;
    }
}
