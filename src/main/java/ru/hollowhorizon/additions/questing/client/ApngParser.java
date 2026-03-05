package ru.hollowhorizon.additions.questing.client;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.CRC32;

public final class ApngParser {
    private static final byte[] PNG_SIGNATURE = new byte[]{
            (byte) 137, 80, 78, 71, 13, 10, 26, 10
    };

    private ApngParser() {
    }

    public static ParsedApng parse(InputStream stream) throws IOException {
        DataInputStream in = new DataInputStream(stream);
        byte[] signature = in.readNBytes(8);
        if (!Arrays.equals(signature, PNG_SIGNATURE)) {
            return null;
        }

        byte[] ihdr = null;
        boolean hasAnimationControl = false;
        List<Chunk> sharedChunks = new ArrayList<>();
        List<RawFrame> frames = new ArrayList<>();
        RawFrame current = null;

        while (true) {
            int length;
            try {
                length = in.readInt();
            } catch (IOException ignored) {
                break;
            }

            byte[] typeBytes = in.readNBytes(4);
            if (typeBytes.length < 4) {
                break;
            }

            String type = new String(typeBytes, StandardCharsets.US_ASCII);
            byte[] data = in.readNBytes(length);
            in.readInt();

            switch (type) {
                case "IHDR" -> ihdr = data;
                case "acTL" -> hasAnimationControl = true;
                case "fcTL" -> {
                    if (ihdr == null) {
                        return null;
                    }
                    if (current != null && current.hasData()) {
                        frames.add(current);
                    }
                    current = new RawFrame(FrameControl.fromFcTl(data, ihdr));
                }
                case "IDAT" -> {
                    if (ihdr == null) {
                        return null;
                    }
                    if (current == null) {
                        current = new RawFrame(FrameControl.defaultFrame(ihdr));
                    }
                    current.idatParts.add(data);
                }
                case "fdAT" -> {
                    if (current != null && data.length > 4) {
                        current.idatParts.add(Arrays.copyOfRange(data, 4, data.length));
                    }
                }
                case "IEND" -> {
                    if (current != null && current.hasData()) {
                        frames.add(current);
                    }
                    current = null;
                }
                default -> {
                    if (!"IDAT".equals(type) && !"IEND".equals(type)) {
                        sharedChunks.add(new Chunk(type, data));
                    }
                }
            }

            if ("IEND".equals(type)) {
                break;
            }
        }

        if (!hasAnimationControl || ihdr == null || frames.isEmpty()) {
            return null;
        }

        return compose(ihdr, sharedChunks, frames);
    }

    private static ParsedApng compose(byte[] ihdr, List<Chunk> sharedChunks, List<RawFrame> rawFrames) throws IOException {
        int canvasWidth = readInt(ihdr, 0);
        int canvasHeight = readInt(ihdr, 4);

        BufferedImage canvas = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
        List<RenderedFrame> output = new ArrayList<>(rawFrames.size());

        FrameControl previousControl = null;
        BufferedImage previousSnapshot = null;

        for (RawFrame rawFrame : rawFrames) {
            if (previousControl != null) {
                applyDispose(canvas, previousControl, previousSnapshot);
            }

            previousSnapshot = rawFrame.control.disposeOp == 2 ? copy(canvas) : null;

            BufferedImage frame = decodeFrame(rawFrame.control, rawFrame.idatParts, ihdr, sharedChunks);
            blend(canvas, frame, rawFrame.control);
            output.add(new RenderedFrame(copy(canvas), rawFrame.control.delayMillis()));

            previousControl = rawFrame.control;
        }

        return new ParsedApng(canvasWidth, canvasHeight, output);
    }

    private static BufferedImage decodeFrame(FrameControl control, List<byte[]> idatParts, byte[] ihdr, List<Chunk> sharedChunks) throws IOException {
        byte[] framePng = createFramePng(control, idatParts, ihdr, sharedChunks);
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(framePng));
        if (image == null) {
            throw new IOException("Failed to decode APNG frame");
        }
        return image;
    }

    private static byte[] createFramePng(FrameControl control, List<byte[]> idatParts, byte[] ihdr, List<Chunk> sharedChunks) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);

        out.write(PNG_SIGNATURE);
        out.writeInt(13);
        out.writeBytes("IHDR");
        byte[] patched = Arrays.copyOf(ihdr, ihdr.length);
        writeInt(patched, 0, control.width);
        writeInt(patched, 4, control.height);
        out.write(patched);
        writeCrc(out, "IHDR", patched);

        for (Chunk chunk : sharedChunks) {
            if ("acTL".equals(chunk.type) || "fcTL".equals(chunk.type) || "fdAT".equals(chunk.type)) {
                continue;
            }
            writeChunk(out, chunk.type, chunk.data);
        }

        for (byte[] idat : idatParts) {
            writeChunk(out, "IDAT", idat);
        }

        writeChunk(out, "IEND", new byte[0]);
        out.flush();
        return bytes.toByteArray();
    }

    private static void writeChunk(DataOutputStream out, String type, byte[] data) throws IOException {
        out.writeInt(data.length);
        out.writeBytes(type);
        out.write(data);
        writeCrc(out, type, data);
    }

    private static void writeCrc(DataOutputStream out, String type, byte[] data) throws IOException {
        CRC32 crc = new CRC32();
        crc.update(type.getBytes(StandardCharsets.US_ASCII));
        crc.update(data);
        out.writeInt((int) crc.getValue());
    }

    private static void blend(BufferedImage canvas, BufferedImage frame, FrameControl control) {
        Graphics2D g = canvas.createGraphics();
        try {
            if (control.blendOp == 0) {
                g.setComposite(AlphaComposite.Clear);
                g.fillRect(control.xOffset, control.yOffset, control.width, control.height);
                g.setComposite(AlphaComposite.SrcOver);
            }

            g.drawImage(frame, control.xOffset, control.yOffset, null);
        } finally {
            g.dispose();
        }
    }

    private static void applyDispose(BufferedImage canvas, FrameControl previousControl, BufferedImage previousSnapshot) {
        if (previousControl.disposeOp == 1) {
            Graphics2D g = canvas.createGraphics();
            try {
                g.setComposite(AlphaComposite.Clear);
                g.fillRect(previousControl.xOffset, previousControl.yOffset, previousControl.width, previousControl.height);
            } finally {
                g.dispose();
            }
        } else if (previousControl.disposeOp == 2 && previousSnapshot != null) {
            Graphics2D g = canvas.createGraphics();
            try {
                g.setComposite(AlphaComposite.Src);
                g.drawImage(previousSnapshot, 0, 0, null);
            } finally {
                g.dispose();
            }
        }
    }

    private static BufferedImage copy(BufferedImage src) {
        BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = copy.createGraphics();
        try {
            g.setComposite(AlphaComposite.Src);
            g.drawImage(src, 0, 0, null);
        } finally {
            g.dispose();
        }
        return copy;
    }

    private static int readInt(byte[] data, int offset) {
        return ByteBuffer.wrap(data, offset, 4).order(ByteOrder.BIG_ENDIAN).getInt();
    }

    private static void writeInt(byte[] data, int offset, int value) {
        ByteBuffer.wrap(data, offset, 4).order(ByteOrder.BIG_ENDIAN).putInt(value);
    }

    public record ParsedApng(int width, int height, List<RenderedFrame> frames) {
    }

    public record RenderedFrame(BufferedImage image, int delayMillis) {
    }

    private record Chunk(String type, byte[] data) {
    }

    private static final class RawFrame {
        private final FrameControl control;
        private final List<byte[]> idatParts = new ArrayList<>();

        private RawFrame(FrameControl control) {
            this.control = control;
        }

        private boolean hasData() {
            return !idatParts.isEmpty();
        }
    }

    private static final class FrameControl {
        private final int width;
        private final int height;
        private final int xOffset;
        private final int yOffset;
        private final int delayMillis;
        private final int disposeOp;
        private final int blendOp;

        private FrameControl(int width, int height, int xOffset, int yOffset, int delayMillis, int disposeOp, int blendOp) {
            this.width = width;
            this.height = height;
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.delayMillis = delayMillis;
            this.disposeOp = disposeOp;
            this.blendOp = blendOp;
        }

        private static FrameControl fromFcTl(byte[] data, byte[] ihdr) {
            if (data.length < 26) {
                return defaultFrame(ihdr);
            }

            ByteBuffer b = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);
            b.getInt();
            int width = b.getInt();
            int height = b.getInt();
            int xOffset = b.getInt();
            int yOffset = b.getInt();
            int delayNum = Short.toUnsignedInt(b.getShort());
            int delayDen = Short.toUnsignedInt(b.getShort());
            int disposeOp = Byte.toUnsignedInt(b.get());
            int blendOp = Byte.toUnsignedInt(b.get());

            if (delayDen == 0) {
                delayDen = 100;
            }
            int delayMs = Math.max(1, (int) Math.round((1000.0 * delayNum) / delayDen));

            return new FrameControl(width, height, xOffset, yOffset, delayMs, disposeOp, blendOp);
        }

        private static FrameControl defaultFrame(byte[] ihdr) {
            int width = readInt(ihdr, 0);
            int height = readInt(ihdr, 4);
            return new FrameControl(width, height, 0, 0, 100, 0, 0);
        }

        private int delayMillis() {
            return delayMillis;
        }
    }
}
