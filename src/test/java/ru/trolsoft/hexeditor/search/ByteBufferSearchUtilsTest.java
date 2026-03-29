package ru.trolsoft.hexeditor.search;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.trolsoft.hexeditor.data.AbstractByteBuffer;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ByteBufferSearchUtilsTest {

    static class ByteBuf extends AbstractByteBuffer {
        private final byte[] bytes;


        ByteBuf(byte[] bytes) {
            super(bytes.length);
            this.bytes = bytes;
        }

        @Override
        protected void closeStream() {}

        @Override
        protected long getStreamSize() {
            return bytes.length;
        }

//        @Override
//        public long getFileSize() throws IOException {
//            return bytes.length;
//        }

        @Override
        protected void loadBuffer() {
        }

        @Override
        protected boolean supportRandomAccess() {
            return true;
        }

        @Override
        public byte getByte(long fileOffset) {
            return bytes[(int) fileOffset];
        }
    }


    @Nested
    class IndexOfValidation {

        @Test
        void returnsMinusOne_whenDataIsNull() throws IOException {
            assertEquals(-1, ByteBufferSearchUtils.indexOf(null, new byte[]{1}, 0));
        }

        @Test
        void returnsMinusOne_whenPatternIsNull() throws IOException {
            var buf = new ByteBuf(new byte[]{1});
            assertEquals(-1, ByteBufferSearchUtils.indexOf(buf, (byte[])null, 0));
        }

        @Test
        void returnsMinusOne_whenFromOffsetNegative() throws IOException {
            var buf = new ByteBuf(new byte[]{1});
            assertEquals(-1, ByteBufferSearchUtils.indexOf(buf, new byte[]{1}, -1));
        }

        @Test
        void returnsMinusOne_whenPatternLongerThanFile() throws IOException {
            var buf = new ByteBuf(new byte[]{1,2,3,4,5});
            assertEquals(-1, ByteBufferSearchUtils.indexOf(buf, new byte[10], 0));
        }

        @Test
        void returnsMinusOne_whenEmptyPattern() throws IOException {
            var buf = new ByteBuf(new byte[]{1,2,3,4,5,6,7,8,9,10});
            assertEquals(-1, ByteBufferSearchUtils.indexOf(buf, new byte[0], 0));
        }
    }

    @Nested
    class IndexOfMatches {

        @Test
        void findsPatternAtBeginning() throws IOException {
            byte[] data = {1, 2, 3, 4, 5};
            byte[] pattern = {1, 2, 3};
            var buf = new ByteBuf(data);

            long result = ByteBufferSearchUtils.indexOf(buf, pattern, 0);
            assertEquals(0, result);
        }

        @Test
        void findsPatternInMiddle() throws IOException {
            byte[] data = {0, 0, 1, 2, 3, 0, 0};
            byte[] pattern = {1, 2, 3};
            var buf = new ByteBuf(data);

            long result = ByteBufferSearchUtils.indexOf(buf, pattern, 0);
            assertEquals(2, result);
        }

        @Test
        void findsPatternAtEnd() throws IOException {
            byte[] data = {0, 0, 1, 2, 3};
            byte[] pattern = {1, 2, 3};

            var buf = new ByteBuf(data);

            long result = ByteBufferSearchUtils.indexOf(buf, pattern, 0);
            assertEquals(2, result);
        }

        @Test
        void returnsMinusOne_whenPatternNotFound() throws IOException {
            byte[] data = {1, 2, 3, 4, 5};
            byte[] pattern = {6, 7, 8};
            var buf = new ByteBuf(data);

            long result = ByteBufferSearchUtils.indexOf(buf, pattern, 0);
            assertEquals(-1, result);
        }

        @Test
        void findsPattern_withOverlappingKMP() throws IOException {
            // Тест на корректность KMP: паттерн "ABA" в "ABABA"
            byte[] data = {1, 2, 1, 2, 1};
            byte[] pattern = {1, 2, 1};
            var buf = new ByteBuf(data);

            long result = ByteBufferSearchUtils.indexOf(buf, pattern, 0);
            assertEquals(0, result); // Первое вхождение
        }

        @Test
        void respectsFromOffset() throws IOException {
            byte[] data = {1, 2, 3, 1, 2, 3};
            byte[] pattern = {1, 2, 3};
            var buf = new ByteBuf(data);

            // Начинаем поиск с позиции 3 — должно найти второе вхождение
            long result = ByteBufferSearchUtils.indexOf(buf, pattern, 3);
            assertEquals(3, result);
        }

        @Test
        void fromOffsetBeyondFileSize_normalized() throws IOException {
            byte[] data = {1, 2, 3};
            byte[] pattern = {1, 2, 3};
            var buf = new ByteBuf(data);

            // fromOffset = 100, но fileSize = 3 → должно нормализоваться и найти паттерн
            long result = ByteBufferSearchUtils.indexOfBackward(buf, pattern, 100);
            assertEquals(0, result);
        }
    }

//    @Nested
//    class IndexOfCacheStrategy {
//
//        @Test
//        void restoresCacheStrategy_afterSearch() throws IOException {
//            byte[] data = {1, 2, 3};
//            byte[] pattern = {2};
//            doAnswer(invocation -> data[Math.toIntExact(invocation.getArgument(0))])
//                    .when(mockBuffer).getByte(anyLong());
//
//            ByteBufferSearchUtils.indexOf(mockBuffer, pattern, 0);
//
//            // Проверяем, что стратегия восстановлена в finally
//            verify(mockBuffer).setCacheStrategy(AbstractByteBuffer.CacheStrategy.BACKWARD);
//        }
//
//        @Test
//        void restoresCacheStrategy_onException() throws IOException {
//            byte[] pattern = {1};
//            when(mockBuffer.getFileSize()).thenReturn(10L);
//            when(mockBuffer.getCacheStrategy()).thenReturn(AbstractByteBuffer.CacheStrategy.FORWARD);
//            when(mockBuffer.getByte(0L)).thenThrow(new IOException("Test exception"));
//
//            assertThrows(IOException.class, () ->
//                    ByteBufferSearchUtils.indexOf(mockBuffer, pattern, 0));
//
//            // Даже при исключении стратегия должна восстановиться
//            verify(mockBuffer, atLeastOnce()).setCacheStrategy(AbstractByteBuffer.CacheStrategy.FORWARD);
//        }
//    }


    @Nested
    @DisplayName("indexOfBackward: Поиск в обратном направлении")
    class IndexOfBackwardTests {

        @Test
        void findsPatternAtEnd() throws IOException {
            byte[] data = {0, 0, 1, 2, 3};
            byte[] pattern = {1, 2, 3};
            var buf = new ByteBuf(data);

            long result = ByteBufferSearchUtils.indexOfBackward(buf, pattern, 4);
            assertEquals(2, result);
        }

        @Test
        void findsPatternAtBeginning() throws IOException {
            byte[] data = {1, 2, 3, 0, 0};
            byte[] pattern = {1, 2, 3};
            var buf = new ByteBuf(data);

            long result = ByteBufferSearchUtils.indexOfBackward(buf, pattern, 4);
            assertEquals(0, result);
        }

        @Test
        void findsLastOccurrence_whenMultipleMatches() throws IOException {
            byte[] data = {1, 2, 3, 0, 1, 2, 3};
            byte[] pattern = {1, 2, 3};
            var buf = new ByteBuf(data);

            // Ищем с конца — должно найти последнее вхождение (индекс 4)
            long result = ByteBufferSearchUtils.indexOfBackward(buf, pattern, 6);
            assertEquals(4, result);
        }

        @Test
        void returnsMinusOne_whenPatternNotFound() throws IOException {
            byte[] data = {1, 2, 3};
            byte[] pattern = {4, 5, 6};
            var buf = new ByteBuf(data);

            long result = ByteBufferSearchUtils.indexOfBackward(buf, pattern, 2);
            assertEquals(-1, result);
        }

//        @Test
//        void restoresCacheStrategy_afterBackwardSearch() throws IOException {
//            byte[] data = {1, 2, 3};
//            byte[] pattern = {2};
//            when(mockBuffer.getFileSize()).thenReturn(3L);
//            when(mockBuffer.getCacheStrategy()).thenReturn(AbstractByteBuffer.CacheStrategy.FORWARD);
//            doAnswer(invocation -> data[Math.toIntExact(invocation.getArgument(0))])
//                    .when(mockBuffer).getByte(anyLong());
//
//            ByteBufferSearchUtils.indexOfBackward(mockBuffer, pattern, 2);
//
//            verify(mockBuffer).setCacheStrategy(AbstractByteBuffer.CacheStrategy.FORWARD);
//        }
    }

    @Nested
    @DisplayName("indexOf(byte[], byte[]): Поиск в обычных массивах")
    class IndexOfArrayTests {

        @Test
        void findsPattern_simple() {
            byte[] data = {0, 1, 2, 3, 4};
            byte[] pattern = {2, 3};
            assertEquals(2, ByteBufferSearchUtils.indexOf(data, pattern));
        }

        @Test
        void returnsMinusOne_whenPatternLongerThanData() {
            byte[] data = {1, 2};
            byte[] pattern = {1, 2, 3};
            assertEquals(-1, ByteBufferSearchUtils.indexOf(data, pattern));
        }

        @Test
        void returnsMinusOne_whenNullPattern() {
            byte[] data = {1, 2, 3};
            assertEquals(-1, ByteBufferSearchUtils.indexOf(data, null));
        }

        @Test
        void handlesOverlappingPatterns_KMP() {
            // Паттерн "AA" в "AAA" — KMP должен найти первое вхождение на индексе 0
            byte[] data = {1, 1, 1};
            byte[] pattern = {1, 1};
            assertEquals(0, ByteBufferSearchUtils.indexOf(data, pattern));
        }

        @Test
        void emptyData_returnsMinusOne() {
            byte[] data = {};
            byte[] pattern = {1};
            assertEquals(-1, ByteBufferSearchUtils.indexOf(data, pattern));
        }
    }

//    private void setupBuffer(byte[] data, long startOffset, long endOffset) throws IOException {
//        reset(mockBuffer);
//
//        when(mockBuffer.getFileSize()).thenReturn((long) data.length);
//        when(mockBuffer.getCacheStrategy()).thenReturn(AbstractByteBuffer.CacheStrategy.BACKWARD);
//
//        doAnswer(invocation -> {
//            long index = invocation.getArgument(0);
//            if (index >= 0 && index < data.length) {
//                return data[Math.toIntExact(index)];
//            }
//            throw new AssertionError("getByte called with invalid index: " + index + ", data.length: " + data.length);
//        }).when(mockBuffer).getByte(anyLong());
//    }

    @Nested
    @DisplayName("indexOf(byte[][], ...): Поиск множества паттернов")
    class IndexOfMultiplePatterns {

        @Test
        void findsEarliestPattern() throws IOException {
            byte[] data = {0, 1, 2, 3, 4, 5};
            byte[][] patterns = {
                    {3, 4, 5},  // найдётся на индексе 3
                    {1, 2}      // найдётся на индексе 1 ← раньше
            };
            var buf = new ByteBuf(data);

            long result = ByteBufferSearchUtils.indexOf(buf, patterns, 0);
            assertEquals(1, result);
        }

        @Test
        void returnsMinusOne_whenNoPatternsMatch() throws IOException {
            byte[] data = {1, 2, 3};
            byte[][] patterns = {{4, 5}, {6, 7}};
            var buf = new ByteBuf(data);

            long result = ByteBufferSearchUtils.indexOf(buf, patterns, 0);
            assertEquals(-1, result);
        }

        @Test
        void skipsNullAndEmptyPatterns() throws IOException {
            byte[] data = {1, 2, 3};
            byte[][] patterns = {null, {}, {2, 3}}; // null и пустой должны игнорироваться
            var buf = new ByteBuf(data);

            long result = ByteBufferSearchUtils.indexOf(buf, patterns, 0);
            assertEquals(1, result);
        }

//        @Test
//        void restoresCacheStrategy_onMultiplePatterns() throws IOException {
//            byte[] data = {1, 2, 3};
//            byte[][] patterns = {{1}, {2}, {3}};
//            when(mockBuffer.getFileSize())
//                    .thenReturn(3L);
//            when(mockBuffer.getCacheStrategy())
//                    .thenReturn(AbstractByteBuffer.CacheStrategy.FORWARD);
//            doAnswer(invocation -> data[Math.toIntExact(invocation.getArgument(0))])
//                    .when(mockBuffer).getByte(anyLong());
//
//            ByteBufferSearchUtils.indexOf(mockBuffer, patterns, 0);
//
//            verify(mockBuffer, atLeastOnce()).setCacheStrategy(AbstractByteBuffer.CacheStrategy.FORWARD);
//        }

        @Test
        void handlesFromOffset_correctly() throws IOException {
            byte[] data = {1, 2, 3, 1, 2, 3};
            byte[][] patterns = {{1, 2, 3}};
            var buf = new ByteBuf(data);

            // Поиск с позиции 3 — должно найти второе вхождение
            long result = ByteBufferSearchUtils.indexOf(buf, patterns, 3);
            assertEquals(3, result);
        }
    }
}