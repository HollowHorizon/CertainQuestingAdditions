package ru.hollowhorizon.additions.questing.client;

record ScreenRect(double centerX, double centerY, double width, double height) {
    boolean isValid() {
        return Double.isFinite(centerX)
                && Double.isFinite(centerY)
                && Double.isFinite(width)
                && Double.isFinite(height)
                && width > 0D
                && height > 0D;
    }

    ScreenRect expand(double horizontalFactor, double verticalFactor) {
        double expandedWidth = width + width * Math.max(0D, horizontalFactor) * 2D;
        double expandedHeight = height + height * Math.max(0D, verticalFactor) * 2D;
        return new ScreenRect(centerX, centerY, expandedWidth, expandedHeight);
    }

    int left() {
        return (int) Math.floor(centerX - width / 2D);
    }

    int top() {
        return (int) Math.floor(centerY - height / 2D);
    }

    int right() {
        return (int) Math.ceil(centerX + width / 2D);
    }

    int bottom() {
        return (int) Math.ceil(centerY + height / 2D);
    }

    int pixelWidth() {
        return Math.max(1, right() - left());
    }

    int pixelHeight() {
        return Math.max(1, bottom() - top());
    }
}
