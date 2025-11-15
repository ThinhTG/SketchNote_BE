package com.sketchnotes.identityservice.dtos.request;

import com.sketchnotes.identityservice.enums.BlogStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class PublishRequest
{
    private BlogStatus status;
}
