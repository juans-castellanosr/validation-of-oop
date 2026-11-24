package co.edu.autonoma.ancientseas.ui;

import java.awt.Color;
import java.awt.Font;

final class Theme {

    static final double MIN_TEXT_CONTRAST = 4.5;

    static final Color NAVY = new Color(10, 38, 77);
    static final Color SEA = new Color(20, 100, 150);
    static final Color SKY = new Color(140, 217, 242);
    static final Color SAND = new Color(237, 212, 153);
    static final Color CORAL = new Color(184, 66, 38);
    static final Color GREEN = new Color(26, 122, 68);
    static final Color GREY = new Color(82, 94, 106);
    static final Color PALE = new Color(240, 247, 252);
    static final Color INK = new Color(26, 31, 41);
    static final Color WHITE = Color.WHITE;
    static final Color FOCUS = new Color(255, 214, 90);
    static final Color DISABLED_FILL = new Color(206, 212, 218);
    static final Color DISABLED_TEXT = new Color(60, 68, 78);
    static final int MENU_PANEL_ALPHA = 215;
    static final Color WATER_TOP = new Color(51, 153, 204);
    static final Color WATER_BOTTOM = new Color(5, 26, 56);
    static final Color LIFE = new Color(240, 96, 72);
    static final Color AIR = new Color(120, 205, 240);
    static final Color GROWTH = new Color(96, 200, 120);

    static final double SCALE = UiScale.factor();

    static final Font TITLE = font(Font.BOLD, 52);
    static final Font HEADING = font(Font.BOLD, 22);
    static final Font SECTION = font(Font.BOLD, 17);
    static final Font BODY = font(Font.PLAIN, 16);
    static final Font BODY_BOLD = font(Font.BOLD, 16);
    static final Font SMALL = font(Font.PLAIN, 14);

    static final Color[][] TEXT_PAIRS = {
        {WHITE, SEA}, {WHITE, GREEN}, {WHITE, CORAL}, {WHITE, GREY}, {WHITE, NAVY}, {SKY, NAVY},
        {INK, PALE}, {NAVY, PALE}, {GREY, PALE}, {SEA, PALE}, {CORAL, PALE}, {GREEN, PALE}, {INK, SAND},
        {WHITE, darker(SEA)}, {WHITE, darker(GREEN)}, {WHITE, darker(CORAL)}, {WHITE, darker(GREY)},
        {WHITE, darker(darker(SEA))}, {WHITE, darker(darker(GREEN))}, {WHITE, darker(darker(CORAL))},
        {WHITE, darker(darker(GREY))}, {DISABLED_TEXT, DISABLED_FILL}, {FOCUS, NAVY}, {NAVY, FOCUS}
    };

    static final Color[][] TRANSLUCENT_TEXT_PAIRS = {
        {WHITE, new Color(NAVY.getRed(), NAVY.getGreen(), NAVY.getBlue(), MENU_PANEL_ALPHA)},
        {SKY, new Color(NAVY.getRed(), NAVY.getGreen(), NAVY.getBlue(), MENU_PANEL_ALPHA)}
    };

    static Color composite(Color top, Color bottom) {
        double a = top.getAlpha() / 255.0;
        return new Color((int) Math.round(top.getRed() * a + bottom.getRed() * (1 - a)),
                (int) Math.round(top.getGreen() * a + bottom.getGreen() * (1 - a)),
                (int) Math.round(top.getBlue() * a + bottom.getBlue() * (1 - a)));
    }

    private Theme() {
    }

    static Font font(int style, float points) {
        return new Font(Font.SANS_SERIF, style, 12).deriveFont(style, size(points));
    }

    static float size(float points) {
        return (float) (points * SCALE);
    }

    static int px(int pixels) {
        return (int) Math.round(pixels * SCALE);
    }

    static Color darker(Color color) {
        return new Color((int) (color.getRed() * 0.8), (int) (color.getGreen() * 0.8), (int) (color.getBlue() * 0.8));
    }

    static double contrast(Color a, Color b) {
        double la = luminance(a);
        double lb = luminance(b);
        return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
    }

    private static double luminance(Color c) {
        return 0.2126 * channel(c.getRed()) + 0.7152 * channel(c.getGreen()) + 0.0722 * channel(c.getBlue());
    }

    private static double channel(int value) {
        double v = value / 255.0;
        return v <= 0.03928 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
    }
}
