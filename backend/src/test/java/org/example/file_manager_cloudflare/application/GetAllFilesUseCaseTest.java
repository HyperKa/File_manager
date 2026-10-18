package org.example.file_manager_cloudflare.application;

import org.example.file_manager_cloudflare.file.application.GetAllFilesUseCase;
import org.example.file_manager_cloudflare.file.domain.FileMetadata;
import org.example.file_manager_cloudflare.file.domain.FilePage;
import org.example.file_manager_cloudflare.file.domain.UserId;
import org.example.file_manager_cloudflare.file.domain.port.FileMetadataRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetAllFilesUseCaseTest {

    @Mock
    private FileMetadataRepository metadataRepo;

    private GetAllFilesUseCase getAllFilesUseCase;

    public GetAllFilesUseCaseTest() {
        getAllFilesUseCase = new GetAllFilesUseCase(metadataRepo);
    }

    private final UserId userId = UserId.createFromUUID(UUID.fromString("00000000-0000-0000-0000-000000000001"));

    /**
     * имитация ситуации, когда в бд есть файлы - возврат страницы с файлом
     */
    @Test
    void getAllFiles_whenFilesExist_shouldReturnFilePage() {
        FileMetadata mockFile = mock(FileMetadata.class);
        FilePage<FileMetadata> page = new FilePage<>(
                List.of(mockFile),
                1L,
                1,
                10,
                1
        );

        when(metadataRepo.findAllFilesByOwnerId(userId, any(), any()))
                .thenReturn(page);

        FilePage<FileMetadata> result = getAllFilesUseCase.getAllFilesForOnePage(userId, 0, 10);

        assertNotNull(result);
        assertEquals(page, result);
        verify(metadataRepo, times(1)).findAllFilesByOwnerId(userId, 0, 10);
    }

    /**
     * имитация ситуации, когда в бд нет файлов - возврат пустой страницы
     */
    @Test
    void getAllFiles_WhenNoFilesExist_ReturnsEmptyListWithoutErrors() {
        FilePage<FileMetadata> emptyPage = new FilePage<>(
                Collections.emptyList(),
                0L,
                0,
                10,
                0
        );

        when(metadataRepo.findAllFilesByOwnerId(any(), any(), any()))
                .thenReturn(emptyPage);

        FilePage<FileMetadata> result = getAllFilesUseCase.getAllFilesForOnePage(userId, 0, 10);

        assertNotNull(emptyPage);
        assertEquals(emptyPage, result);
        verify(metadataRepo, times(1)).findAllFilesByOwnerId(any(), any(), any());
    }

    /**
     *  имитация ситуации, когда идет обращение к несуществующей странице - возврат пустой страницы
     */
    @Test
    void getAllFiles_WhenRequestingOutOfBoundsPage_ReturnsEmptyContentButValidMeta() {
        FilePage<FileMetadata> outOfBoundsPage = new FilePage<>(
                Collections.emptyList(), // Пустой контент
                1L,                      // Но всего элементов всё еще 1
                5,                       // Запрошенный номер страницы
                10,
                1                        // И всего страниц всё еще 1
        );

        when(metadataRepo.findAllFilesByOwnerId(eq(userId), eq(5), eq(10)))
                .thenReturn(outOfBoundsPage);

        FilePage<FileMetadata> result = getAllFilesUseCase.getAllFilesForOnePage(userId, 5, 10);

        assertNotNull(result);
        assertEquals(result.content().isEmpty(), true) ;
        assertEquals(1L, result.totalElements());
        assertEquals(1, result.totalPages());

        verify(metadataRepo, times(1)).findAllFilesByOwnerId(userId, 5, 10);
    }

}
