package com.example.Bep_Viet.service.Imp;

import com.example.Bep_Viet.model.Post;
import com.example.Bep_Viet.model.PostShare;
import com.example.Bep_Viet.model.User;
import com.example.Bep_Viet.repository.PostRepository;
import com.example.Bep_Viet.repository.PostShareRepository;
import com.example.Bep_Viet.repository.UserRepository;
import com.example.Bep_Viet.request.PostShareRequest;
import com.example.Bep_Viet.response.PostShareResponse;
import com.example.Bep_Viet.service.PostShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostShareServiceImpl implements PostShareService {

    private final PostShareRepository postShareRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    public PostShareResponse sharePost(Long userId, PostShareRequest request) {
        if (postShareRepository.existsByUserIdAndPostId(userId, request.getPostId())) {
            throw new RuntimeException("Bạn đã đăng lại bài viết này rồi");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết"));

        PostShare share = PostShare.builder()
                .user(user)
                .post(post)
                .content(request.getContent())
                .build();

        PostShare saved = postShareRepository.save(share);
        return mapToResponse(saved);
    }

    @Override
    public void unsharePost(Long userId, Long shareId) {
        PostShare share = postShareRepository.findById(shareId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bản đăng lại"));

        if (!share.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xoá bản đăng lại này");
        }

        postShareRepository.delete(share);
    }

    @Override
    public Page<PostShareResponse> getSharesByUser(Long userId, Pageable pageable) {
        return postShareRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    private PostShareResponse mapToResponse(PostShare share) {
        Post post = share.getPost();
        User sharer = share.getUser();

        return PostShareResponse.builder()
                .id(share.getId())
                .userId(sharer.getId())
                .username(sharer.getUsername())
                .userAvatar(sharer.getAvatarUrl())
                .content(share.getContent())
                .createdAt(share.getCreatedAt())
                .originalPostId(post.getId())
                .originalTitle(post.getTitle())
                .originalThumbnail(post.getThumbnail())
                .originalAuthorName(post.getUser().getFullName())
                .build();
    }
}