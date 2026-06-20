package com.misaka.gtlleisureaddon.util;

import com.misaka.gtlleisureaddon.GTLLeisureAddon;

import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Loads GT-style structure binaries ({@code GTLASB2}) from {@code assets/lleisure/structures}.
 * API mirrors {@code com.gtladd.gtladditions.common.machine.multiblock.structure.StructureResourceLoader}.
 */
public final class LeisureStructureResourceLoader {

    public static final String ROOT_PATH = "assets/" + GTLLeisureAddon.MOD_ID + "/structures";

    private static final byte[] MAGIC = "GTLASB2".getBytes(StandardCharsets.US_ASCII);
    private static final int KIND_FACTORY_PATTERN = 1;
    private static final int KIND_RING_SET = 2;

    private static final Object2ReferenceOpenHashMap<String, String[][]> FACTORY_PATTERN_CACHE = new Object2ReferenceOpenHashMap<>(4);

    private LeisureStructureResourceLoader() {}

    public record RepeatableAisle(int aisleIndex, int min, int max) {}

    public static FactoryBlockPattern loadFactoryPattern(
                                                         String resourcePath,
                                                         String expectedId,
                                                         RelativeDirection... directions) {
        return loadFactoryPattern(resourcePath, expectedId, List.of(), directions);
    }

    public static FactoryBlockPattern loadFactoryPattern(
                                                         String resourcePath,
                                                         String expectedId,
                                                         List<RepeatableAisle> repeatableAisles,
                                                         RelativeDirection... directions) {
        FactoryBlockPattern pattern = switch (directions.length) {
            case 0 -> FactoryBlockPattern.start();
            case 3 -> FactoryBlockPattern.start(directions[0], directions[1], directions[2]);
            default -> throw new IllegalStateException(invalid(resourcePath, expectedId,
                    "expected 0 or 3 relative directions, got " + directions.length));
        };

        var repeatableByIndex = new java.util.HashMap<Integer, RepeatableAisle>();
        for (RepeatableAisle repeatable : repeatableAisles) {
            repeatableByIndex.put(repeatable.aisleIndex(), repeatable);
        }

        String[][] aisles = loadAisles(resourcePath, expectedId);
        for (int aisleIndex = 0; aisleIndex < aisles.length; aisleIndex++) {
            RepeatableAisle repeatable = repeatableByIndex.get(aisleIndex);
            if (repeatable == null) {
                pattern.aisle(aisles[aisleIndex]);
            } else {
                pattern.aisleRepeatable(repeatable.min(), repeatable.max(), aisles[aisleIndex]);
            }
        }
        return pattern;
    }

    public static String[][] loadShapeInfoSlices(
                                                 String resourcePath,
                                                 String expectedId,
                                                 SliceSymbolMapper mapper) {
        String[][] aisles = loadAisles(resourcePath, expectedId);
        int rowCount = aisles[0].length;
        int planeCount = aisles[0][0].length();

        String[][] slices = new String[planeCount][aisles.length];
        for (int planeIndex = 0; planeIndex < planeCount; planeIndex++) {
            for (int aisleIndex = 0; aisleIndex < aisles.length; aisleIndex++) {
                StringBuilder row = new StringBuilder(rowCount);
                for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                    char symbol = aisles[aisleIndex][rowIndex].charAt(planeIndex);
                    row.append(mapper.map(planeIndex, aisleIndex, rowIndex, symbol));
                }
                slices[planeIndex][aisleIndex] = row.toString();
            }
        }
        return slices;
    }

    public static String[][] loadAisles(String resourcePath, String expectedId) {
        return getOrLoad(resourcePath, () -> readFactoryAisles(resourcePath, expectedId));
    }

    private static String[][] getOrLoad(String resourcePath, java.util.function.Supplier<String[][]> loader) {
        String[][] cached = FACTORY_PATTERN_CACHE.get(resourcePath);
        if (cached != null) {
            return cached;
        }
        String[][] loaded = loader.get();
        FACTORY_PATTERN_CACHE.put(resourcePath, loaded);
        return loaded;
    }

    private static String[][] readFactoryAisles(String resourcePath, String expectedId) {
        return readResource(resourcePath, expectedId, KIND_FACTORY_PATTERN, (input, fullPath) -> readStringGrid(input, fullPath, expectedId, "aisles"));
    }

    private static <T> T readResource(
                                      String resourcePath,
                                      String expectedId,
                                      int expectedKind,
                                      ResourceReader<T> reader) {
        String fullPath = ROOT_PATH + "/" + resourcePath.trim().replace('\\', '/').replaceAll("^/+", "");
        InputStream stream = LeisureStructureResourceLoader.class.getClassLoader().getResourceAsStream(fullPath);
        if (stream == null) {
            throw new IllegalStateException(invalid(fullPath, expectedId, "resource is missing"));
        }

        try (DataInputStream input = new DataInputStream(new BufferedInputStream(stream))) {
            byte[] magic = input.readNBytes(MAGIC.length);
            if (!java.util.Arrays.equals(magic, MAGIC)) {
                throw new IllegalStateException(invalid(fullPath, expectedId, "invalid binary magic"));
            }

            int kind = input.readUnsignedByte();
            if (kind != expectedKind) {
                throw new IllegalStateException(invalid(fullPath, expectedId, "expected kind " + expectedKind + ", got " + kind));
            }

            String id = readUtf8String(input, fullPath, expectedId, "id");
            if (!id.equals(expectedId)) {
                throw new IllegalStateException(invalid(fullPath, expectedId, "expected id '" + expectedId + "', got '" + id + "'"));
            }

            T result = reader.read(input, fullPath);
            if (input.read() != -1) {
                throw new IllegalStateException(invalid(fullPath, expectedId, "trailing bytes after structure payload"));
            }
            return result;
        } catch (IllegalStateException exception) {
            throw exception;
        } catch (EOFException exception) {
            throw new IllegalStateException("Invalid structure resource " + fullPath + " for id '" + expectedId +
                    "': truncated binary resource", exception);
        } catch (IOException exception) {
            throw new IllegalStateException("Invalid structure resource " + fullPath + " for id '" + expectedId +
                    "': failed to read binary resource", exception);
        }
    }

    private static String[][] readStringGrid(DataInputStream input, String resourcePath, String expectedId, String fieldName)
                                                                                                                              throws IOException {
        int aisleCount = readPositiveUnsignedShort(input, resourcePath, expectedId, fieldName + " aisle count");
        int rowCount = readPositiveUnsignedShort(input, resourcePath, expectedId, fieldName + " row count");
        int width = readPositiveUnsignedShort(input, resourcePath, expectedId, fieldName + " row width");
        int dictionarySize = readPositiveUnsignedShort(input, resourcePath, expectedId, fieldName + " dictionary size");
        String[] dictionary = new String[dictionarySize];
        for (int index = 0; index < dictionarySize; index++) {
            dictionary[index] = readAsciiRow(input, width, resourcePath, expectedId, fieldName + " dictionary row " + index);
        }

        String[][] grid = new String[aisleCount][];
        for (int aisleIndex = 0; aisleIndex < aisleCount; aisleIndex++) {
            String[] aisle = new String[rowCount];
            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                int index = input.readUnsignedShort();
                if (index >= dictionarySize) {
                    throw new IllegalStateException(invalid(resourcePath, expectedId, fieldName + "[" + aisleIndex + "][" + rowIndex +
                            "] index " + index + " exceeds dictionary size " + dictionarySize));
                }
                aisle[rowIndex] = dictionary[index];
            }
            grid[aisleIndex] = aisle;
        }
        return grid;
    }

    private static String readAsciiRow(
                                       DataInputStream input,
                                       int width,
                                       String resourcePath,
                                       String expectedId,
                                       String fieldName) throws IOException {
        byte[] bytes = input.readNBytes(width);
        for (byte value : bytes) {
            int code = value & 0xFF;
            if (code < 0x20 || code > 0x7E) {
                throw new IllegalStateException(invalid(resourcePath, expectedId, fieldName + " contains non-printable byte " + code));
            }
        }
        return new String(bytes, StandardCharsets.US_ASCII);
    }

    private static String readUtf8String(DataInputStream input, String resourcePath, String expectedId, String fieldName)
                                                                                                                          throws IOException {
        int length = readPositiveUnsignedShort(input, resourcePath, expectedId, fieldName + " length");
        byte[] bytes = input.readNBytes(length);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static int readPositiveUnsignedShort(DataInputStream input, String resourcePath, String expectedId, String fieldName)
                                                                                                                                  throws IOException {
        int value = input.readUnsignedShort();
        if (value == 0) {
            throw new IllegalStateException(invalid(resourcePath, expectedId, fieldName + " must be non-zero"));
        }
        return value;
    }

    private static String invalid(String resourcePath, String expectedId, String message) {
        return "Invalid structure resource " + resourcePath + " for id '" + expectedId + "': " + message;
    }

    @FunctionalInterface
    private interface ResourceReader<T> {

        T read(DataInputStream input, String fullPath) throws IOException;
    }

    @FunctionalInterface
    public interface SliceSymbolMapper {

        char map(int planeIndex, int aisleIndex, int rowIndex, char symbol);
    }
}