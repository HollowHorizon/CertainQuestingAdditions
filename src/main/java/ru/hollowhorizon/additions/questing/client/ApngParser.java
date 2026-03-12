package ru.hollowhorizon.additions.questing.client;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
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

    public static DiscoveredApng discover(InputStream stream) throws IOException {
        DataInputStream in = new DataInputStream(stream);
        byte[] signature = in.readNBytes(8);
        if (!Arrays.equals(signature, PNG_SIGNATURE)) {
            return null;
        }

        byte[] ihdr = null;
        boolean hasAnimationControl = false;
        int frameCount = 0;
        long totalDurationMs = 0L;
        List<SharedChunk> sharedChunks = new ArrayList<>();
        RawFrame current = null;

        while (true) {
            Chunk chunk = readChunk(in);
            if (chunk == null) {
                break;
            }

            switch (chunk.type) {
                case "IHDR" -> ihdr = chunk.data;
                case "acTL" -> hasAnimationControl = true;
                case "fcTL" -> {
                    if (ihdr == null) {
                        return null;
                    }

                    if (current != null && current.hasData()) {
                        frameCount++;
                        totalDurationMs += Math.max(1, current.control.delayMillis());
                    }

                    current = new RawFrame(FrameControl.fromFcTl(chunk.data, ihdr));
                }
                case "IDAT" -> {
                    if (ihdr == null) {
                        return null;
                    }

                    if (current == null) {
                        current = new RawFrame(FrameControl.defaultFrame(ihdr));
                    }
                    current.markHasData();
                }
                case "fdAT" -> {
                    if (current == null || chunk.data.length <= 4) {
                        continue;
                    }
                    current.markHasData();
                }
                case "IEND" -> {
                    if (current != null && current.hasData()) {
                        frameCount++;
                        totalDurationMs += Math.max(1, current.control.delayMillis());
                    }
                    current = null;
                }
                default -> {
                    if (!"IHDR".equals(chunk.type) && !"IDAT".equals(chunk.type) && !"fdAT".equals(chunk.type) && !"acTL".equals(chunk.type)
                            && !"fcTL".equals(chunk.type) && !"IEND".equals(chunk.type)) {
                        sharedChunks.add(new SharedChunk(chunk.type, chunk.data));
                    }
                }
            }

            if ("IEND".equals(chunk.type)) {
                break;
            }
        }

        if (!hasAnimationControl || ihdr == null || frameCount == 0) {
            return null;
        }

        return new DiscoveredApng(
                readInt(ihdr, 0),
                readInt(ihdr, 4),
                frameCount,
                Math.max(1L, totalDurationMs),
                Arrays.copyOf(ihdr, ihdr.length),
                List.copyOf(sharedChunks)
        );
    }

    public static ApngFrameStream openFrameStream(DiscoveredApng discovered, StreamOpener opener) {
        return new ApngFrameStream(discovered, opener);
    }

    private static Chunk readChunk(DataInputStream in) throws IOException {
        int length;
        try {
            length = in.readInt();
        } catch (EOFException ignored) {
            return null;
        }

        byte[] typeBytes = in.readNBytes(4);
        if (typeBytes.length < 4) {
            return null;
        }

        byte[] data = in.readNBytes(length);
        if (data.length < length) {
            return null;
        }

        in.readInt();
        return new Chunk(new String(typeBytes, StandardCharsets.US_ASCII), data);
    }

    private static BufferedImage decodeFrame(FrameControl control, List<byte[]> idatParts, byte[] ihdr, List<SharedChunk> sharedChunks) throws IOException {
        byte[] framePng = createFramePng(control, idatParts, ihdr, sharedChunks);
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(framePng));
        if (image == null) {
            throw new IOException("Failed to decode APNG frame");
        }
        return image;
    }

    private static byte[] createFramePng(FrameControl control, List<byte[]> idatParts, byte[] ihdr, List<SharedChunk> sharedChunks) throws IOException {
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

        for (SharedChunk chunk : sharedChunks) {
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

    private static void clear(BufferedImage image) {
        Graphics2D g = image.createGraphics();
        try {
            g.setComposite(AlphaComposite.Clear);
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
        } finally {
            g.dispose();
        }
    }

    private static int readInt(byte[] data, int offset) {
        return ByteBuffer.wrap(data, offset, 4).order(ByteOrder.BIG_ENDIAN).getInt();
    }

    private static void writeInt(byte[] data, int offset, int value) {
        ByteBuffer.wrap(data, offset, 4).order(ByteOrder.BIG_ENDIAN).putInt(value);
    }

    public interface StreamOpener {
        InputStream open() throws IOException;
    }

    public static final class ApngFrameStream implements Closeable {
        private final DiscoveredApng discovered;
        private final StreamOpener opener;
        private final BufferedImage canvas;

        private DataInputStream input;
        private FrameControl queuedControl;
        private FrameControl previousControl;
        private BufferedImage previousSnapshot;
        private boolean restartPending;
        private boolean closed;

        private ApngFrameStream(DiscoveredApng discovered, StreamOpener opener) {
            this.discovered = discovered;
            this.opener = opener;
            this.canvas = new BufferedImage(discovered.width(), discovered.height(), BufferedImage.TYPE_INT_ARGB);
        }

        public RenderedFrame nextFrame() throws IOException {
            ensureLoop();

            if (previousControl != null) {
                applyDispose(canvas, previousControl, previousSnapshot);
            }

            FrameData frameData = readNextFrameData();
            previousSnapshot = frameData.control.disposeOp == 2 ? copy(canvas) : null;

            BufferedImage frame = decodeFrame(frameData.control, frameData.idatParts, discovered.ihdr(), discovered.sharedChunks());
            blend(canvas, frame, frameData.control);
            previousControl = frameData.control;

            return new RenderedFrame(canvas, frameData.control.delayMillis());
        }

        @Override
        public void close() throws IOException {
            closed = true;
            closeInput();
            previousControl = null;
            previousSnapshot = null;
            queuedControl = null;
            restartPending = false;
        }

        private void ensureLoop() throws IOException {
            if (closed) {
                throw new IOException("APNG frame stream is closed");
            }

            if (input == null || restartPending) {
                restartLoop();
            }
        }

        private void restartLoop() throws IOException {
            closeInput();
            InputStream stream = opener.open();
            if (stream == null) {
                throw new IOException("APNG resource stream is unavailable");
            }

            input = new DataInputStream(stream);
            byte[] signature = input.readNBytes(8);
            if (!Arrays.equals(signature, PNG_SIGNATURE)) {
                throw new IOException("Resource is not a PNG stream");
            }

            clear(canvas);
            previousControl = null;
            previousSnapshot = null;
            queuedControl = null;
            restartPending = false;
        }

        private FrameData readNextFrameData() throws IOException {
            FrameControl control = queuedControl;
            queuedControl = null;
            List<byte[]> idatParts = new ArrayList<>();

            while (!closed) {
                Chunk chunk = readChunk(input);
                if (chunk == null) {
                    throw new IOException("Unexpected end of APNG stream");
                }

                switch (chunk.type) {
                    case "fcTL" -> {
                        FrameControl nextControl = FrameControl.fromFcTl(chunk.data, discovered.ihdr());
                        if (!idatParts.isEmpty()) {
                            queuedControl = nextControl;
                            return new FrameData(resolveControl(control), List.copyOf(idatParts));
                        }
                        control = nextControl;
                    }
                    case "IDAT" -> {
                        idatParts.add(chunk.data);
                        if (control == null) {
                            control = FrameControl.defaultFrame(discovered.ihdr());
                        }
                    }
                    case "fdAT" -> {
                        if (chunk.data.length <= 4) {
                            continue;
                        }
                        if (control == null) {
                            throw new IOException("Encountered fdAT before frame control");
                        }
                        idatParts.add(Arrays.copyOfRange(chunk.data, 4, chunk.data.length));
                    }
                    case "IEND" -> {
                        if (!idatParts.isEmpty()) {
                            restartPending = true;
                            return new FrameData(resolveControl(control), List.copyOf(idatParts));
                        }

                        restartLoop();
                        control = null;
                    }
                    default -> {
                    }
                }
            }

            throw new IOException("APNG frame stream closed");
        }

        private FrameControl resolveControl(FrameControl control) {
            return control != null ? control : FrameControl.defaultFrame(discovered.ihdr());
        }

        private void closeInput() throws IOException {
            if (input != null) {
                input.close();
                input = null;
            }
        }
    }

    public static final class DiscoveredApng {
        private final int width;
        private final int height;
        private final int frameCount;
        private final long totalDurationMs;
        private final byte[] ihdr;
        private final List<SharedChunk> sharedChunks;

        private DiscoveredApng(int width, int height, int frameCount, long totalDurationMs, byte[] ihdr, List<SharedChunk> sharedChunks) {
            this.width = width;
            this.height = height;
            this.frameCount = frameCount;
            this.totalDurationMs = totalDurationMs;
            this.ihdr = ihdr;
            this.sharedChunks = sharedChunks;
        }

        public int width() {
            return width;
        }

        public int height() {
            return height;
        }

        public int frameCount() {
            return frameCount;
        }

        public long totalDurationMs() {
            return totalDurationMs;
        }

        byte[] ihdr() {
            return ihdr;
        }

        List<SharedChunk> sharedChunks() {
            return sharedChunks;
        }
    }

    public record RenderedFrame(BufferedImage image, int delayMillis) {
    }

    static final class SharedChunk {
        private final String type;
        private final byte[] data;

        private SharedChunk(String type, byte[] data) {
            this.type = type;
            this.data = Arrays.copyOf(data, data.length);
        }
    }

    private record Chunk(String type, byte[] data) {
    }

    private record FrameData(FrameControl control, List<byte[]> idatParts) {
    }

    private static final class RawFrame {
        private final FrameControl control;
        private boolean hasData;

        private RawFrame(FrameControl control) {
            this.control = control;
        }

        private void markHasData() {
            hasData = true;
        }

        private boolean hasData() {
            return hasData;
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
