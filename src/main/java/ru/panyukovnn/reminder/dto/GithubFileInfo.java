package ru.panyukovnn.reminder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GithubFileInfo {

    @JsonProperty("download_url")
    private String downloadUrl;

    /**
     * Возможные значения:
     * - dir
     * - file
     */
    private String type;

    /**
     * Путь к файлу (или папке) внутри репозитория
     */
    private String path;
}
