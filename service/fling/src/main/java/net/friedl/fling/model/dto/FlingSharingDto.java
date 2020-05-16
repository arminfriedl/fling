package net.friedl.fling.model.dto;

import lombok.Data;

@Data
public class FlingSharingDto {
    private Boolean allowUpload;

    private Boolean directDownload;

    private String shareUrl;
}
