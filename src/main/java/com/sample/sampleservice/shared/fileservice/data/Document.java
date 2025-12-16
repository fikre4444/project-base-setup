package com.sample.sampleservice.shared.fileservice.data;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
public class Document implements Serializable {

    @Serial
    private static final long serialVersionUID = -8136427324547720296L;

    private String fileName;

    private String documentType;

    private String documentId;

    private String fileUrl;
}
