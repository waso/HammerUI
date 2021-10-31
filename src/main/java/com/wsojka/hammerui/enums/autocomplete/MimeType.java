/*
 * Copyright (C) 2017 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.enums.autocomplete;

public enum MimeType {
    /* text */
    TEXT_PLAIN("text/plain"),
    TEXT_HTML("text/html"),
    TEXT_CSS("text/css"),
    TEXT_JAVASCRIPT("text/javascript"),

    /* image */
    IMAGE_JPEG("image/jpeg"),
    IMAGE_PNG("image/png"),
    IMAGE_GIF("image/gif"),
    IMAGE_BMP("image/bmp"),
    IMAGE_WEBM("image/webp"),

    /* audio */
    AUDIO_MIDI("audio/midi"),
    AUDIO_MPEG("audio/mpeg"),
    AUDIO_WEBM("audio/webm"),
    AUDIO_OGG("audio/ogg"),
    AUDIO_WAV("audio/wav"),

    /* video */

    VIDEO_WEBM("video/webm"),
    VIDEO_OGG("video/ogg"),

    /* application */
    APPLICATION_ATOM_XML("application/atom+xml"),
    APPLICATION_LD_JSON("application/ld+json"),
    APPLICATION_RSS_XML("application/rss+xml"),
    APPLICATION_VND_GEO_JSON("application/vnd.geo+json"),
    APPLICATION_MANIFEST_JSON("application/manifest+json"),
    APPLICATION_OCTET_STREAM("application/octet-stream"),
    APPLICATION_PKCS12("application/pkcs12"),
    APPLICATION_VND_MSPOWERPOINT("application/vnd.mspowerpoint"),
    APPLICATION_XHTML_XML("application/xhtml+xml"),
    APPLICATION_XML("application/xml"),
    APPLICATION_PDF("application/pdf"),
    APPLICATION_JSON("application/json"),
    APPLICATION_JAVASCRIPT("application/javascript"),
    APPLICATION_X_WWW_FORM_URLENCODED("application/x-www-form-urlencoded"),
    ;

    private String name;

    MimeType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
