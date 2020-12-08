package GameControllerServer.connection.bindings;

import java.util.Arrays;

public enum Keybinding {

    ESC, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14, F15, F16, F17, F18, F19, F20, F21, F22, F23, F24, PRNT_SCR, SCRL_LCK, PAUSE,
    TILDA("~"), ONE("1"), TWO("2"), THREE("3"), FOUR("4"), FIVE("5"), SIX("6"), SEVEN("7"), EIGHT("8"), NINE("9"), ZERO("0"),
    DASH("-"), PLUS("+"), BACKSPACE, INS, HOME, PG_UP,
    TAB, Q, W, E, R, T, Y, I, O, U, P, L_BRACKET("["), R_BRACKET("]"), BACKSLASH("\\"), DELETE, END, PG_DOWN,
    CAPS, A, S, D, F, G, H, J, K, L, SEMICOLON(";"), APOSTROPHE("'"), ENTER,
    SHIFT, Z, X, C, V, B, N, M, COMMA(","), PERIOD("."), SLASH("/"), R_SHIFT,
    CTRL_L, SUPER, ALT_L, SPACE, ALT_R, FUNC, OPT, CTRL_R,
    ARROW_UP, ARROW_LEFT, ARROW_RIGHT, ARROW_DOWN;

    private String serialized;

    Keybinding() {
        this.serialized = name().toLowerCase();
    }

    Keybinding(String serialized) {
        this.serialized = serialized;
    }

    public String getSerialized() {
        return serialized;
    }

    /**
     * Retrieves a keybinding from a serialized payload.
     * @param button the button component from the payload.
     * @return the translated {@link Keybinding} equivilent for the button.
     */
    public static Keybinding fromPayload(String button) {
        return Arrays
                .stream(values())
                .filter(keybinding -> keybinding
                            .name()
                            .equalsIgnoreCase(button)
                        || keybinding
                            .getSerialized()
                            .equalsIgnoreCase(button))
                .findFirst()
                .orElse(null);
    }

}
