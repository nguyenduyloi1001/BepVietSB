package com.example.Bep_Viet.service;

import com.example.Bep_Viet.request.TagRequest;
import com.example.Bep_Viet.response.TagResponse;

import java.util.List;

public interface TagService {

    TagResponse create(TagRequest request);

    List<TagResponse> getAll();

    TagResponse getBySlug(String slug);

    void delete(Long id);
}