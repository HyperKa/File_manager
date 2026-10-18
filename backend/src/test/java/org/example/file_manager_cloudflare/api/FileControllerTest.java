package org.example.file_manager_cloudflare.api;

import org.example.file_manager_cloudflare.file.application.GetAllFilesUseCase;
import org.example.file_manager_cloudflare.file.application.UploadFileCommand;
import org.example.file_manager_cloudflare.file.application.UploadFileUseCase;
import org.example.file_manager_cloudflare.file.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMultipartHttpServletRequestDsl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
class FileControllerTest {

    // Инструмент для отправки HTTP-запросов в тест
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetAllFilesUseCase getAllFilesUseCase;

    @MockitoBean
    private UploadFileUseCase uploadFileUseCase;

    private final UserId testUserId = UserId.createFromUUID(UUID.fromString("00000000-0000-0000-0000-000000000001"));

    @Test
    void getAllFiles_WithDefaultParams_ReturnsHttp200AndArray() throws Exception {

        FileMetadata mockFile = FileMetadata.pending(
                testUserId,
                new FileName("test.pdf"),
                new StorageKey("files/test.pdf"),
                new FileMediaType("application/pdf"),
                1024L
        );
        mockFile.markReady("etag-123", "sha256-hash");

        FilePage<FileMetadata> page = new FilePage<>(
                List.of(mockFile),
                1L,
                0,
                50,
                1
        );

        // Контроллер передаст в UseCase дефолтные значения (0 и 50)
        when(getAllFilesUseCase.getAllFilesForOnePage(eq(testUserId), eq(0), eq(50)))
                .thenReturn(page);

        // Проверка
        mockMvc.perform(get("/api/v1/files")) // Запрос БЕЗ параметров
                .andExpect(status().isOk())             // возвращает HTTP 200 OK
                .andExpect(jsonPath("$.content").isArray()) // возвращается массив
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void getAllFiles_WhenNoFiles_ReturnsHttp200AndEmptyArray() throws Exception {
        // имитация пустой бд
        FilePage<FileMetadata> emptyPage = new FilePage<>(
                Collections.emptyList(),
                0L,
                0,
                50,
                0
        );
        when(getAllFilesUseCase.getAllFilesForOnePage(eq(testUserId), eq(0), eq(50)))
                .thenReturn(emptyPage);

        // Проверка
        mockMvc.perform(get("/api/v1/files"))
                .andExpect(status().isOk())                             // без ошибок (не 404 и не 500)
                .andExpect(jsonPath("$.content").isEmpty())   // возвращается пустой список []
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getAllFiles_WithCustomParams_PassesThemToUseCase() throws Exception {
        // Подготовка
        FilePage<FileMetadata> emptyPage = new FilePage<>(
                Collections.emptyList(),
                0L,
                2,
                10,
                0);
        when(getAllFilesUseCase.getAllFilesForOnePage(eq(testUserId), eq(2), eq(10)))
                .thenReturn(emptyPage);

        // Проверка с явными параметрами
        mockMvc.perform(get("/api/v1/files?page=2&size=10"))
                .andExpect(status().isOk());

        // статус 200, мокинг сработал, и UseCase был вызван
        // именно с параметрами 2 и 10, иначе Mockito выбросил бы исключение "Wanted but not invoked".
    }

    // ЗАГРУЗКА, FM-1

    // 1. Файл корректный, загрузка успешна
    @Test
    void shouldUploadFileSuccessfully() throws Exception {
        byte[] fileContent = "fake pdf content".getBytes();

        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                fileContent
        );

        when(uploadFileUseCase.uploadFile(any(UploadFileCommand.class), any(UserId.class)))
                .thenAnswer(invocation -> {
                    UploadFileCommand cmd = invocation.getArgument(0);
                    UserId ownerId = invocation.getArgument(1);

                    // метаданные с ЛЮБЫМ ID (UseCase всё равно его перезапишет)
                    FileMetadata metadata = FileMetadata.pending(
                            ownerId,
                            new FileName(cmd.fileName()),
                            new StorageKey("files/test.pdf"),
                            new FileMediaType(cmd.mediaType()),
                            cmd.sizeBytes()
                    );
                    metadata.markReady("mock-etag", "mock-hash");
                    return metadata;
                });

        // выполнение запроса и сохранение результата
        MvcResult result = mockMvc.perform(multipart("/api/v1/files").file(mockFile))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("READY"))
                .andExpect(jsonPath("$.fileName").value("document.pdf"))
                .andExpect(jsonPath("$.sizeBytes").value(fileContent.length))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(responseBody);
        String actualId = jsonNode.get("id").asText();

        String locationHeader = result.getResponse().getHeader("Location");
        assertTrue(locationHeader.contains("/api/v1/files/" + actualId));
    }

    // 2. Загрузка некорректна
    @Test
    void shouldReturnBadRequestWhenFileIsEmpty() throws Exception {
        // новый файл с пустым массивом байт (size = 0)
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0] // Пустое содержимое
        );

        // запрос
        mockMvc.perform(multipart("/api/v1/files")
                        .file(emptyFile))
                // контроллер (или глобальный обработчик ошибок) вернул 400
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenFileParameterIsMissing() throws Exception {
        // multipart запрос, но без файла (.file(...) отсутствует)
        mockMvc.perform(multipart("/api/v1/files"))
                // Spring сам отвергнет запрос, так как @RequestParam("file") обязательный
                .andExpect(status().isBadRequest());
    }

}