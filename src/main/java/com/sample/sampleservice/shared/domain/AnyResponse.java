package com.sample.sampleservice.shared.domain;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnyResponse {

    private String message;

    private String status;

}
