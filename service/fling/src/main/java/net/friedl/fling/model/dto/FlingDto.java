package net.friedl.fling.model.dto;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class FlingDto {
    private String name;

    private Long id;

    private Instant creationTime;

    @JsonIgnore
    private Boolean directDownload;

    @JsonIgnore
    private Boolean allowUpload;

    @JsonIgnore
    private Boolean shared;

    @JsonIgnore
    private String shareUrl;

    @JsonIgnore
    private Integer expirationClicks;

    @JsonIgnore
    private Instant expirationTime;

    private String authCode;

    @JsonProperty("sharing")
    private void unpackSharing(Map<String, Object> sharing) {
        this.directDownload = (Boolean) sharing.getOrDefault("directDownload", false);
        this.allowUpload = (Boolean) sharing.getOrDefault("allowUpload", false);
        this.shared = (Boolean) sharing.getOrDefault("shared", true);
        this.shareUrl = (String) sharing.getOrDefault("shareUrl", null);
    }

    @JsonProperty("sharing")
    private Map<String, Object> packSharing() {
        Map<String, Object> sharing = new HashMap<>();
        sharing.put("directDownload", this.directDownload);
        sharing.put("allowUpload", this.allowUpload);
        sharing.put("shared", this.shared);
        sharing.put("shareUrl", this.shareUrl);

        return sharing;
    }

    @JsonProperty("expiration")
    private void unpackExpiration(Map<String, Object> expiration) {
        String type = (String) expiration.getOrDefault("type", null);
        if(type == null) return;

        switch(type) {
        case "time":
            this.expirationClicks = null;
            // json can only handle int, long must be given as string
            // TODO: this back and forth conversion is a bit hack-ish
            this.expirationTime = Instant.ofEpochMilli(Long.valueOf(expiration.get("value").toString()));
            break;
        case "clicks":
            this.expirationTime = null;
            this.expirationClicks = Integer.valueOf(expiration.get("value").toString());
            break;
        default:
            throw new IllegalArgumentException("Unexpected value '"+type+"'");
        }
    }

    @JsonProperty("expiration")
    private Map<String, Object> packExpiration() {
        Map<String, Object> expiration = new HashMap<>();

        if(this.expirationClicks != null) {
            expiration.put("type", "clicks");
            expiration.put("value", this.expirationClicks);
        }

        if(this.expirationTime != null) {
            expiration.put("type", "time");
            expiration.put("value", this.expirationTime.toEpochMilli());
        }

        return expiration;
    }

    @JsonProperty("creationTime")
    public Long getJsonUploadTime() {
        return creationTime.toEpochMilli();
    }
}
