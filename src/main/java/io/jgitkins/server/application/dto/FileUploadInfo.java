package io.jgitkins.server.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadInfo {

    @NotBlank(message = "File path is required")
    private String filePath;

    @NotBlank(message = "Commit message is required")
    private String commitMessage;

    private String authorName; // Controller 단에 의해 조작되거나 null로 들어올 수 있음, 혹은 @Valid로 제어

    @Email(message = "Invalid author email format")
    private String authorEmail;
}
