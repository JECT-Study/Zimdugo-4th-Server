package com.zimdugo.admin.application;

import com.zimdugo.admin.application.dto.AdminDocumentCommand;
import com.zimdugo.admin.application.dto.AdminDocumentDetailResult;
import com.zimdugo.admin.domain.AdminDocument;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AdminDocumentImageWorkflow {

    private static final String EXISTING_PREFIX = "existing:";
    private static final String NEW_PREFIX = "new:";

    private final AdminDocumentService documentService;
    private final AdminNoticeImageStorage imageStorage;

    public AdminDocumentDetailResult create(
        AdminDocumentCommand command,
        List<MultipartFile> files,
        List<String> imageOrder
    ) {
        List<MultipartFile> safeFiles = safeFiles(files);
        List<String> safeOrder = safeOrder(imageOrder);
        validateOrder(safeOrder, 0, safeFiles.size());
        List<String> uploadedUrls = imageStorage.uploadAll(safeFiles);
        try {
            List<String> finalUrls = resolveUrls(safeOrder, List.of(), uploadedUrls);
            return documentService.createDocumentResult(command.withImageUrls(finalUrls));
        } catch (RuntimeException exception) {
            imageStorage.deleteAll(uploadedUrls);
            throw exception;
        }
    }

    public AdminDocumentDetailResult update(
        Long documentId,
        AdminDocumentCommand command,
        List<MultipartFile> files,
        List<String> imageOrder
    ) {
        List<String> existingUrls = documentService.getDocumentImageUrls(documentId);
        List<MultipartFile> safeFiles = safeFiles(files);
        List<String> safeOrder = safeOrder(imageOrder);
        validateOrder(safeOrder, existingUrls.size(), safeFiles.size());
        List<String> uploadedUrls = imageStorage.uploadAll(safeFiles);
        try {
            List<String> finalUrls = resolveUrls(safeOrder, existingUrls, uploadedUrls);
            AdminDocumentDetailResult result = documentService.updateDocumentResult(
                documentId,
                command.withImageUrls(finalUrls)
            );
            imageStorage.deleteAll(removedUrls(existingUrls, finalUrls));
            return result;
        } catch (RuntimeException exception) {
            imageStorage.deleteAll(uploadedUrls);
            throw exception;
        }
    }

    private void validateOrder(List<String> order, int existingCount, int newCount) {
        if (order.size() > AdminDocument.MAX_NOTICE_IMAGE_COUNT) {
            throw new BusinessException(ErrorCode.TOO_MANY_NOTICE_IMAGES);
        }
        Set<String> tokens = new HashSet<>();
        Set<Integer> newIndexes = new HashSet<>();
        for (String token : order) {
            if (!tokens.add(token)) {
                throw invalidOrder();
            }
            int index = tokenIndex(token);
            if (token.startsWith(EXISTING_PREFIX) && index >= existingCount) {
                throw invalidOrder();
            }
            if (token.startsWith(NEW_PREFIX)) {
                if (index >= newCount) {
                    throw invalidOrder();
                }
                newIndexes.add(index);
            }
        }
        if (newIndexes.size() != newCount) {
            throw invalidOrder();
        }
    }

    private List<String> resolveUrls(List<String> order, List<String> existingUrls, List<String> newUrls) {
        List<String> resolved = new ArrayList<>();
        for (String token : order) {
            int index = tokenIndex(token);
            resolved.add(token.startsWith(EXISTING_PREFIX) ? existingUrls.get(index) : newUrls.get(index));
        }
        return resolved;
    }

    private int tokenIndex(String token) {
        String value;
        if (token != null && token.startsWith(EXISTING_PREFIX)) {
            value = token.substring(EXISTING_PREFIX.length());
        } else if (token != null && token.startsWith(NEW_PREFIX)) {
            value = token.substring(NEW_PREFIX.length());
        } else {
            throw invalidOrder();
        }
        try {
            int index = Integer.parseInt(value);
            if (index < 0) {
                throw invalidOrder();
            }
            return index;
        } catch (NumberFormatException exception) {
            throw new BusinessException(ErrorCode.INVALID_NOTICE_IMAGE_ORDER, exception);
        }
    }

    private List<String> removedUrls(List<String> existingUrls, List<String> finalUrls) {
        return existingUrls.stream()
            .filter(url -> !finalUrls.contains(url))
            .toList();
    }

    private List<MultipartFile> safeFiles(List<MultipartFile> files) {
        return files == null ? List.of() : files.stream()
            .filter(file -> file != null && !file.isEmpty())
            .toList();
    }

    private List<String> safeOrder(List<String> order) {
        return order == null ? List.of() : order;
    }

    private BusinessException invalidOrder() {
        return new BusinessException(ErrorCode.INVALID_NOTICE_IMAGE_ORDER);
    }
}
