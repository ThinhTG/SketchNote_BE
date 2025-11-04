package com.sketchnotes.project_service.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ListPageRequest {
    private Long projectId;
    private  List<PageDto> pages;
}
