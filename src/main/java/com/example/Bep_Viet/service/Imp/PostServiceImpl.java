package com.example.Bep_Viet.service.Imp;

import com.example.Bep_Viet.enums.PostStatus;
import com.example.Bep_Viet.enums.PostType;
import com.example.Bep_Viet.enums.RecipeStatus;
import com.example.Bep_Viet.enums.TargetType;
import com.example.Bep_Viet.exception.AppException;
import com.example.Bep_Viet.exception.ErrorCode;
import com.example.Bep_Viet.model.Post;
import com.example.Bep_Viet.model.Recipe;
import com.example.Bep_Viet.model.User;
import com.example.Bep_Viet.repository.*;
import com.example.Bep_Viet.request.PostRequest;
import com.example.Bep_Viet.response.PostResponse;
import com.example.Bep_Viet.service.LikeService;
import com.example.Bep_Viet.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final ShareRepository shareRepository;
    private final RecipeRepository recipeRepository; // ← thêm mới, để lấy info recipe gốc

    @Override
    public PostResponse create(PostRequest request, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        PostType type = request.getType();
        Recipe recipe = null;

        // Nếu là post chia sẻ từ recipe -> validate + ép type
        if (request.getOriginalRecipeId() != null) {
            recipe = recipeRepository.findById(request.getOriginalRecipeId())
                    .orElseThrow(() -> new AppException(ErrorCode.RECIPE_NOT_FOUND));

            if (recipe.getStatus() != RecipeStatus.PUBLISHED) {
                throw new AppException(ErrorCode.RECIPE_SHARE_NOT_PUBLISHED);
            }

            type = PostType.SHARED_RECIPE;
        }

        Post post = Post.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .thumbnail(request.getThumbnail() != null
                        ? request.getThumbnail()
                        : (recipe != null ? recipe.getImageUrl() : null))
                .postType(type)
                .postStatus(PostStatus.PENDING)
                .originalRecipeId(request.getOriginalRecipeId())
                .viewCount(0)
                .build();

        return mapToResponse(postRepository.save(post));
    }

    @Override
    @Transactional
    public PostResponse getPostById(Long id) {
        postRepository.incrementViewCount(id);
        return mapToResponse(findById(id));
    }

    @Override
    public List<PostResponse> getAllPost() {
        return postRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<PostResponse> getByUserId(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return postRepository.findByUserId(userId).stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<PostResponse> getByStatus(PostStatus status) {
        return postRepository.findByPostStatus(status).stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<PostResponse> getByType(PostType type) {
        return postRepository.findByPostType(type).stream().map(this::mapToResponse).toList();
    }

    @Override
    public PostResponse update(Long id, PostRequest request, Long currentUserId) {
        Post post = findById(id);

        if (!post.getUser().getId().equals(currentUserId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setThumbnail(request.getThumbnail());
        post.setPostType(request.getType());
        post.setPostStatus(PostStatus.PENDING);

        return mapToResponse(postRepository.save(post));
    }

    @Override
    @Transactional
    public void delete(Long id, Long currentUserId, boolean isAdmin) {
        Post post = findById(id);

        if (!isAdmin && !post.getUser().getId().equals(currentUserId)) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
        shareRepository.deleteByTargetIdAndTargetType(id, TargetType.POST);

        postRepository.delete(post);
    }

    @Override
    public PostResponse approve(Long id) {
        Post post = findById(id);
        post.setPostStatus(PostStatus.PUBLISHED);
        return mapToResponse(postRepository.save(post));
    }

    @Override
    public PostResponse reject(Long id) {
        Post post = findById(id);
        post.setPostStatus(PostStatus.REJECTED);
        return mapToResponse(postRepository.save(post));
    }

    private Post findById(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
    }

    private PostResponse mapToResponse(Post post) {
        PostResponse.PostResponseBuilder builder = PostResponse.builder()
                .id(post.getId())
                .userId(post.getUser().getId())
                .userName(post.getUser().getUsername())
                .title(post.getTitle())
                .content(post.getContent())
                .thumbnail(post.getThumbnail())
                .type(post.getPostType())
                .status(post.getPostStatus())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .originalRecipeId(post.getOriginalRecipeId());
        if (post.getOriginalRecipeId() != null) {
            recipeRepository.findById(post.getOriginalRecipeId()).ifPresent(r -> {
                builder.originalRecipeName(r.getName());
                builder.originalRecipeImage(r.getImageUrl());
                builder.originalRecipeSlug(r.getSlug());
            });
        }

        return builder.build();
    }
}