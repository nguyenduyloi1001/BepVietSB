package com.example.Bep_Viet.controller;

import com.example.Bep_Viet.request.TagRequest;
import com.example.Bep_Viet.response.TagResponse;
import com.example.Bep_Viet.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    // GET public - ai cũng xem được
    @GetMapping
    public ResponseEntity<List<TagResponse>> getAll() {
        return ResponseEntity.ok(tagService.getAll());
    }

    @GetMapping("/{slug}")
    public ResponseEntity<TagResponse> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(tagService.getBySlug(slug));
    }

    // POST/DELETE - phải đăng nhập
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TagResponse> create(@RequestBody TagRequest request) {
        return ResponseEntity.ok(tagService.create(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return ResponseEntity.noContent().build();
    }
}