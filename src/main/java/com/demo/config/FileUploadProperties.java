package com.demo.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("file.upload")
@Getter
public class FileUploadProperties {

    private String avatarPath;
    private String avatarUrlPrefix;

    public void setAvatarPath(String path) {
        avatarPath = path;
        if (path.charAt(path.length() - 1) != '/') {
            avatarPath += '/';
        }
    }

    public void setAvatarUrlPrefix(String prefix) {
        avatarUrlPrefix = prefix;
        if (prefix.charAt(prefix.length() - 1) != '/') {
            avatarUrlPrefix += '/';
        }
    }


}
