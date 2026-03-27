package ru.trolsoft.hexeditor.search;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.trolsoft.hexeditor.data.AbstractByteBuffer;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ByteBufferSearchUtilsTest {

    @Mock
    private AbstractByteBuffer mockBuffer;

    // ========== indexOf(AbstractByteBuffer, byte[], long) ==========

    @Nested
    @DisplayName("indexOf: Валидация входных параметров")
    class IndexOfValidation {

        @Test
        void returnsMinusOne_whenDataIsNull() throws IOException {
            assertEquals(-1, ByteBufferSearchUtils.indexOf(null, new byte[]{1}, 0));
        }

        @Test
        void returnsMinusOne_whenPatternIsNull() throws IOException {
            assertEquals(-1, ByteBufferSearchUtils.indexOf(mockBuffer, null, 0));
        }

        @Test
        void returnsMinusOne_whenFromOffsetNegative() throws IOException {
            assertEquals(-1, ByteBufferSearchUtils.indexOf(mockBuffer, new byte[]{1}, -1));
        }

        @Test
        void returnsMinusOne_whenPatternLongerThanFile() throws IOException {
            when(mockBuffer.getFileSize()).thenReturn(5L);
            assertEquals(-1, ByteBufferSearchUtils.indexOf(mockBuffer, new byte[10], 0));
        }

        @Test
        void returnsMinusOne_whenEmptyPattern() throws IOException {
            when(mockBuffer.getFileSize()).thenReturn(10L);
            assertEquals(-1, ByteBufferSearchUtils.indexOf(mockBuffer, new byte[0], 0));
        }
    }

    @Nested
    @DisplayName("indexOf: Поиск совпадений")
    class IndexOfMatches {

        @Test
        void findsPatternAtBeginning() throws IOException {
            byte[] data = {1, 2, 3, 4, 5};
            byte[] pattern = {1, 2, 3};
            setupBuffer(data, 0, data.length);

            long result = ByteBufferSearchUtils.indexOf(mockBuffer, pattern, 0);
            assertEquals(0, result);
        }

        @Test
        void findsPatternInMiddle() throws IOException {
            byte[] data = {0, 0, 1, 2, 3, 0, 0};
            byte[] pattern = {1, 2, 3};
            setupBuffer(data, 0, data.length);

            long result = ByteBufferSearchUtils.indexOf(mockBuffer, pattern, 0);
            assertEquals(2, result);
        }

        @Test
        void findsPatternAtEnd() throws IOException {
            byte[] data = {0, 0, 1, 2, 3};
            byte[] pattern = {1, 2, 3};
            setupBuffer(data, 0, data.length);

            long result = ByteBufferSearchUtils.indexOf(mockBuffer, pattern, 0);
            assertEquals(2, result);
        }

        @Test
        void returnsMinusOne_whenPatternNotFound() throws IOException {
            byte[] data = {1, 2, 3, 4, 5};
            byte[] pattern = {6, 7, 8};
            setupBuffer(data, 0, data.length);

            long result = ByteBufferSearchUtils.indexOf(mockBuffer, pattern, 0);
            assertEquals(-1, result);
        }

        @Test
        void findsPattern_withOverlappingKMP() throws IOException {
            // Тест на корректность KMP: паттерн "ABA" в "ABABA"
            byte[] data = {1, 2, 1, 2, 1};
            byte[] pattern = {1, 2, 1};
            setupBuffer(data, 0, data.length);

            long result = ByteBufferSearchUtils.indexOf(mockBuffer, pattern, 0);
            assertEquals(0, result); // Первое вхождение
        }

        @Test
        void respectsFromOffset() throws IOException {
            byte[] data = {1, 2, 3, 1, 2, 3};
            byte[] pattern = {1, 2, 3};
            setupBuffer(data, 0, data.length);

            // Начинаем поиск с позиции 3 — должно найти второе вхождение
            long result = ByteBufferSearchUtils.indexOf(mockBuffer, pattern, 3);
            assertEquals(3, result);
        }

        @Test
        void fromOffsetBeyondFileSize_normalized() throws IOException {
            byte[] data = {1, 2, 3};
            byte[] pattern = {1, 2, 3};
            when(mockBuffer.getFileSize()).thenReturn(3L);
            // Эмуляция: при чтении любого индекса возвращаем данные из массива
            doAnswer(invocation -> data[Math.toIntExact(invocation.getArgument(0))])
                    .when(mockBuffer).getByte(anyLong());

            // fromOffset = 100, но fileSize = 3 → должно нормализоваться и найти паттерн
            long result = ByteBufferSearchUtils.indexOf(mockBuffer, pattern, 100);
            assertEquals(0, result);
        }
    }

    @Nested
    @DisplayName("indexOf: Управление CacheStrategy")
    class IndexOfCacheStrategy {

        @Test
        void restoresCacheStrategy_afterSearch() throws IOException {
            byte[] data = {1, 2, 3};
            byte[] pattern = {2};
            when(mockBuffer.getFileSize()).thenReturn(3L);
            when(mockBuffer.getCacheStrategy()).thenReturn(AbstractByteBuffer.CacheStrategy.BACKWARD);
            doAnswer(invocation -> data[Math.toIntExact(invocation.getArgument(0))])
                    .when(mockBuffer).getByte(anyLong());

            ByteBufferSearchUtils.indexOf(mockBuffer, pattern, 0);

            // Проверяем, что стратегия восстановлена в finally
            verify(mockBuffer).setCacheStrategy(AbstractByteBuffer.CacheStrategy.BACKWARD);
        }

        @Test
        void restoresCacheStrategy_onException() throws IOException {
            byte[] pattern = {1};
            when(mockBuffer.getFileSize()).thenReturn(10L);
            when(mockBuffer.getCacheStrategy()).thenReturn(AbstractByteBuffer.CacheStrategy.FORWARD);
            when(mockBuffer.getByte(0L)).thenThrow(new IOException("Test exception"));

            assertThrows(IOException.class, () ->
                    ByteBufferSearchUtils.indexOf(mockBuffer, pattern, 0));

            // Даже при исключении стратегия должна восстановиться
            verify(mockBuffer).setCacheStrategy(AbstractByteBuffer.CacheStrategy.FORWARD);
        }
    }

    // ========== indexOfBackward ==========

    @Nested
    @DisplayName("indexOfBackward: Поиск в обратном направлении")
    class IndexOfBackwardTests {

        @Test
        void findsPatternAtEnd() throws IOException {
            byte[] data = {0, 0, 1, 2, 3};
            byte[] pattern = {1, 2, 3};
            setupBuffer(data, 0, data.length);

            long result = ByteBufferSearchUtils.indexOfBackward(mockBuffer, pattern, 4);
            assertEquals(2, result);
        }

        @Test
        void findsPatternAtBeginning() throws IOException {
            byte[] data = {1, 2, 3, 0, 0};
            byte[] pattern = {1, 2, 3};
            setupBuffer(data, 0, data.length);

            long result = ByteBufferSearchUtils.indexOfBackward(mockBuffer, pattern, 4);
            assertEquals(0, result);
        }

        @Test
        void findsLastOccurrence_whenMultipleMatches() throws IOException {
            byte[] data = {1, 2, 3, 0, 1, 2, 3};
            byte[] pattern = {1, 2, 3};
            setupBuffer(data, 0, data.length);

            // Ищем с конца — должно найти последнее вхождение (индекс 4)
            long result = ByteBufferSearchUtils.indexOfBackward(mockBuffer, pattern, 6);
            assertEquals(4, result);
        }

        @Test
        void returnsMinusOne_whenPatternNotFound() throws IOException {
            byte[] data = {1, 2, 3};
            byte[] pattern = {4, 5, 6};
            setupBuffer(data, 0, data.length);

            long result = ByteBufferSearchUtils.indexOfBackward(mockBuffer, pattern, 2);
            assertEquals(-1, result);
        }

        @Test
        void restoresCacheStrategy_afterBackwardSearch() throws IOException {
            byte[] data = {1, 2, 3};
            byte[] pattern = {2};
            when(mockBuffer.getFileSize()).thenReturn(3L);
            when(mockBuffer.getCacheStrategy()).thenReturn(AbstractByteBuffer.CacheStrategy.FORWARD);
            doAnswer(invocation -> data[Math.toIntExact(invocation.getArgument(0))])
                    .when(mockBuffer).getByte(anyLong());

            ByteBufferSearchUtils.indexOfBackward(mockBuffer, pattern, 2);

            verify(mockBuffer).setCacheStrategy(AbstractByteBuffer.CacheStrategy.FORWARD);
        }
    }

    // ========== indexOf(byte[], byte[]) для обычных массивов ==========

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

    private void setupBuffer(byte[] data, long startOffset, long endOffset) throws IOException {
        when(mockBuffer.getFileSize()).thenReturn((long) data.length);
        when(mockBuffer.getCacheStrategy()).thenReturn(AbstractByteBuffer.CacheStrategy.BACKWARD);

        doAnswer(invocation -> {
            long index = invocation.getArgument(0);
            return data[Math.toIntExact(index)];
        }).when(mockBuffer).getByte(anyLong());
    }
}