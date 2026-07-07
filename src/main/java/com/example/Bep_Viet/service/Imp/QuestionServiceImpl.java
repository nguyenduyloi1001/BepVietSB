package com.example.Bep_Viet.service.Imp;

import com.example.Bep_Viet.exception.AppException;
import com.example.Bep_Viet.exception.ErrorCode;
import com.example.Bep_Viet.model.Answer;
import com.example.Bep_Viet.model.Question;
import com.example.Bep_Viet.model.User;
import com.example.Bep_Viet.repository.QuestionRepository;
import com.example.Bep_Viet.repository.UserRepository;
import com.example.Bep_Viet.request.QuestionRequest;
import com.example.Bep_Viet.response.AnswerResponse;
import com.example.Bep_Viet.response.QuestionResponse;
import com.example.Bep_Viet.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class QuestionServiceImpl implements QuestionService {
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    @Override
    public QuestionResponse create(QuestionRequest request, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));
        Question question = Question.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .user(user)
                .build();
        return mapToResponse(questionRepository.save(question));
    }

    @Override
    public QuestionResponse getById(Long id) {
        return mapToResponse(findById(id));
    }

    @Override
    public List<QuestionResponse> getAll() {
        return questionRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<QuestionResponse> getByUserId(Long userId) {
        return questionRepository.findByUserId(userId).stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<QuestionResponse> search(String keyword) {
        return questionRepository.searchQuestions(keyword).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public QuestionResponse update(Long id, QuestionRequest request, Long currentUserId) {
        Question question = findById(id);

        if (!question.getUser().getId().equals(currentUserId)) {
            throw new AppException(ErrorCode.ANSWER_FORBIDDEN);
        }

        question.setTitle(request.getTitle());
        question.setContent(request.getContent());

        return mapToResponse(questionRepository.save(question));
    }

    @Override
    public void delete(Long id, Long currentUserId) {
        Question question = findById(id);
        if(!question.getUser().getId().equals(currentUserId))
        {
            throw new AppException(ErrorCode.ANSWER_FORBIDDEN);
        }
        questionRepository.delete(question);
    }


    private Question findById(Long id){
        return questionRepository.findById(id).orElseThrow(()->new AppException(ErrorCode.QUESTION_NOT_FOUND));
    }

    private QuestionResponse mapToResponse(Question question){
        return QuestionResponse.builder()
                .id(question.getId())
                .userId(question.getUser().getId())
                .email(question.getUser().getEmail())
                .userName(question.getUser().getFullName())
                .title(question.getTitle())
                .content(question.getContent())
                .isSolved(question.getAnswer() != null)
                .answer(question.getAnswer() != null?mapAnswerToResponse(question.getAnswer()) : null)
                .createdAt(question.getCreatedAt())
                .build();
    }

    private AnswerResponse mapAnswerToResponse(Answer a) {
        return AnswerResponse.builder()
                .id(a.getId())
                .questionId(a.getQuestion().getId())
                .userId(a.getUser().getId())
                .email(a.getUser().getEmail())
                .userName(a.getUser().getUsername())
                .content(a.getContent())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }

}
