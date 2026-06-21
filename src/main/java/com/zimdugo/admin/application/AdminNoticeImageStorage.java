package com.zimdugo.admin.application;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface AdminNoticeImageStorage {

    List<String> uploadAll(List<MultipartFile> files);

    void deleteAll(List<String> imageUrls);
}
