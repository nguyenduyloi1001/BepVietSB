package com.example.Bep_Viet.service.Imp;
import com.example.Bep_Viet.enums.ChatRole;
import com.example.Bep_Viet.exception.AppException;
import com.example.Bep_Viet.exception.ErrorCode;
import com.example.Bep_Viet.filter.AiChatRawResult;
import com.example.Bep_Viet.filter.AiRankResult;
import com.example.Bep_Viet.filter.ChatFilters;
import com.example.Bep_Viet.model.*;
import com.example.Bep_Viet.repository.*;
import com.example.Bep_Viet.request.ChatRequest;
import com.example.Bep_Viet.response.ChatResponse;
import com.example.Bep_Viet.service.AiChatService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final ObjectMapper objectMapper;

    @Value("${groq.api.key}")
    private String groqApiKey;

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";

    @Override
    public ChatResponse chat(Long userId, String guestId, ChatRequest request) {

        boolean isGuest = (userId == null);

        ChatSession session;
        if (request.getSessionId() != null) {
            session = sessionRepository.findById(request.getSessionId())
                    .filter(s -> isGuest
                            ? guestId.equals(s.getGuestId())
                            : s.getUser() != null && s.getUser().getId().equals(userId))
                    .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND));

            if (isGuest && session.getCreatedAt().isBefore(LocalDateTime.now().minusHours(24))) {
                throw new AppException(ErrorCode.GUEST_SESSION_EXPIRED);
            }
        } else {
            ChatSession.ChatSessionBuilder builder = ChatSession.builder();
            if (isGuest) {
                builder.guestId(guestId);
            } else {
                builder.user(userRepository.getReferenceById(userId));
            }
            session = sessionRepository.save(builder.build());
        }

        messageRepository.save(ChatMessage.builder()
                .session(session)
                .role(ChatRole.USER)
                .content(request.getMessage())
                .build());

        List<ChatMessage> history = messageRepository
                .findTop6BySessionIdOrderByCreatedAtDesc(session.getId());
        Collections.reverse(history);

        String preferenceContext = isGuest ? "" : buildPreferenceContext(userId);

        AiChatRawResult aiResult = callGroqChat(history, preferenceContext);
        log.info("AI intent={}, filters={}", aiResult.getIntent(), aiResult.getFilters());

        String replyText = aiResult.getReplyText() != null
                ? aiResult.getReplyText()
                : "Xin lỗi, mình chưa hiểu rõ ý bạn lắm, bạn nói lại giúp mình nha";

        if (!isGuest && aiResult.getFilters() != null) {
            savePreferencesFromFilters(userId, aiResult.getFilters());
        }

        List<ChatResponse.SuggestionItem> suggestions = List.of();
        if ("recipe_suggestion".equals(aiResult.getIntent()) && aiResult.getFilters() != null) {
            suggestions = suggestRecipes(aiResult.getFilters());
        }

        String refIds = suggestions.stream()
                .map(s -> s.getRecipeId().toString())
                .collect(Collectors.joining(","));

        messageRepository.save(ChatMessage.builder()
                .session(session)
                .role(ChatRole.ASSISTANT)
                .content(replyText)
                .referencedRecipeIds(refIds)
                .build());

        return ChatResponse.builder()
                .sessionId(session.getId())
                .replyText(replyText)
                .suggestions(suggestions)
                .build();
    }

    // =========================================================
    // ⭐ KHÔI PHỤC: method này bị rớt mất trong lúc viết lại file nhiều lần,
    // đây là bản dựng lại dựa theo đúng mục đích ghi trong comment của
    // RecipeRepository.findRecipeIdsByIngredientsAndDiet ("Dành cho bạn" ở
    // trang chủ). CẦN ĐỐI CHIẾU LẠI với logic gốc (nếu còn lưu ở Git) trước
    // khi merge, để tránh lệch hành vi so với bản cũ.
    //
    // Lấy top ingredient + diet đã học từ preference của user -> query công
    // thức phù hợp cho mục "Dành cho bạn" ở trang chủ, không qua Groq (chỉ
    // dùng dữ liệu đã học sẵn, không cần gọi AI mỗi lần load trang chủ).
    // =========================================================
    @Override
    public List<ChatResponse.SuggestionItem> getSearchedRecipes(Long userId) {
        List<UserPreference> prefs = userPreferenceRepository.findByUserId(userId);
        if (prefs.isEmpty()) {
            return List.of();
        }

        String diet = null;
        List<String> topIngredients = List.of();

        for (UserPreference p : prefs) {
            if ("diet".equals(p.getPreferenceKey())) {
                diet = p.getPreferenceValue();
            } else if ("favorite_ingredients".equals(p.getPreferenceKey())) {
                try {
                    Map<String, Integer> freqMap = objectMapper.readValue(
                            p.getPreferenceValue(), new TypeReference<Map<String, Integer>>() {});
                    topIngredients = freqMap.entrySet().stream()
                            .sorted((a, b) -> b.getValue() - a.getValue())
                            .limit(5)
                            .map(Map.Entry::getKey)
                            .toList();
                } catch (Exception e) {
                    log.warn("Lỗi đọc favorite_ingredients cho userId={}", userId, e);
                }
            }
        }

        if (topIngredients.isEmpty()) {
            return List.of();
        }

        String dietRaw = diet != null ? diet.trim().toLowerCase() : null;
        String dietMode = "ANY";
        if ("chay".equals(dietRaw)) {
            dietMode = "CHAY";
        } else if ("man".equals(dietRaw) || "mặn".equals(dietRaw)) {
            dietMode = "MAN";
        }
        log.info("DEBUG userId={} topIngredients={} dietMode={}", userId, topIngredients, dietMode);
        List<Long> recipeIds = recipeRepository.findRecipeIdsByIngredientsAndDiet(topIngredients, dietMode);
        log.info("DEBUG recipeIds result={}", recipeIds);

        if (recipeIds.isEmpty()) {
            return List.of();
        }

        Map<Long, Recipe> recipeMap = recipeRepository.findAllById(recipeIds).stream()
                .collect(Collectors.toMap(Recipe::getId, r -> r));

        // giữ đúng thứ tự ưu tiên mà query đã sắp (match_count DESC, avg_rating DESC)
        return recipeIds.stream()
                .map(recipeMap::get)
                .filter(Objects::nonNull)
                .map(r -> ChatResponse.SuggestionItem.builder()
                        .recipeId(r.getId())
                        .slug(r.getSlug())
                        .title(r.getName())
                        .thumbnail(r.getImageUrl())
                        .avgRating(r.getAvgRating())
                        .cookingTime(r.getCookingTime())
                        .matchScore(null)
                        .missingIngredients(List.of())
                        .aiReason("Dựa trên sở thích nấu ăn của bạn")
                        .build())
                .toList();
    }


    // ⭐ SỬA: tổng quát hóa nhánh đọc "dạng tần suất JSON" để dùng chung cho
    // favorite_ingredients, favorite_region, favorite_sort_by - thay vì chỉ
    // xử lý riêng favorite_ingredients như trước, các key mới sẽ rơi vào nhánh
    // else và in thô cả chuỗi JSON ra prompt (vd "favorite_region: {"bac":3}"),
    // vừa xấu vừa tốn token, vừa khó AI đọc hiểu.
    // =========================================================
    private static final Set<String> FREQUENCY_BASED_KEYS =
            Set.of("favorite_ingredients", "favorite_region", "favorite_sort_by");

    // Nhãn tiếng Việt hiển thị trong prompt cho từng preference key dạng tần suất
    private static final Map<String, String> FREQUENCY_KEY_LABELS = Map.of(
            "favorite_ingredients", "nguyên liệu hay hỏi tới",
            "favorite_region", "vùng miền hay hỏi tới",
            "favorite_sort_by", "tiêu chí hay quan tâm (đánh giá cao/được thích nhiều)"
    );

    private String buildPreferenceContext(Long userId) {
        List<UserPreference> prefs = userPreferenceRepository.findByUserId(userId);
        if (prefs.isEmpty()) {
            return "";
        }

        StringBuilder summary = new StringBuilder();
        for (UserPreference p : prefs) {
            if (FREQUENCY_BASED_KEYS.contains(p.getPreferenceKey())) {
                try {
                    Map<String, Integer> freqMap = objectMapper.readValue(
                            p.getPreferenceValue(), new TypeReference<Map<String, Integer>>() {});
                    // region/sort_by chỉ cần top 2 (ít giá trị khả dĩ hơn ingredients),
                    // ingredients giữ nguyên top 5 như cũ
                    int limit = "favorite_ingredients".equals(p.getPreferenceKey()) ? 5 : 2;
                    String topValues = freqMap.entrySet().stream()
                            .sorted((a, b) -> b.getValue() - a.getValue())
                            .limit(limit)
                            .map(Map.Entry::getKey)
                            .collect(Collectors.joining(", "));
                    if (!topValues.isBlank()) {
                        String label = FREQUENCY_KEY_LABELS.getOrDefault(p.getPreferenceKey(), p.getPreferenceKey());
                        summary.append(label).append(": ").append(topValues).append(". ");
                    }
                } catch (Exception e) {
                    log.warn("Lỗi đọc {} cho userId={}", p.getPreferenceKey(), userId, e);
                }
            } else {
                summary.append(p.getPreferenceKey()).append(": ").append(p.getPreferenceValue()).append(". ");
            }
        }

        if (summary.isEmpty()) {
            return "";
        }

        return "\n\nThông tin đã biết về user này từ trước: " + summary +
                "Hãy tận dụng thông tin này khi phù hợp, không cần hỏi lại từ đầu " +
                "trừ khi user có ý muốn thay đổi.";
    }

    // =========================================================
    // Lưu preference học được từ filters vào DB (upsert)
    // ⭐ SỬA: học thêm region và sort_by theo tần suất, dùng chung cơ chế
    // với favorite_ingredients (thay vì chỉ overwrite như diet, vì region/sort_by
    // cũng là thứ user có thể đổi ý qua từng lần chat, nên đếm tần suất sẽ phản
    // ánh đúng xu hướng lâu dài hơn là chỉ nhớ giá trị gần nhất)
    // =========================================================
    private void savePreferencesFromFilters(Long userId, ChatFilters filters) {
        if (filters.getDiet() != null && !filters.getDiet().isBlank()) {
            upsertPreference(userId, "diet", filters.getDiet());
        }

        if (filters.getIngredients() != null && !filters.getIngredients().isEmpty()) {
            trackFrequentValues(userId, "favorite_ingredients", filters.getIngredients());
        }

        // ⭐ MỚI
        if (filters.getRegion() != null && !filters.getRegion().isBlank()) {
            trackFrequentValues(userId, "favorite_region", List.of(filters.getRegion()));
        }

        // ⭐ MỚI
        if (filters.getSortBy() != null && !filters.getSortBy().isBlank()) {
            trackFrequentValues(userId, "favorite_sort_by", List.of(filters.getSortBy()));
        }
    }

    // ⭐ SỬA: đổi tên từ trackFrequentIngredients -> trackFrequentValues, tổng quát hóa
    // để dùng chung cho ingredients, region, sort_by - đều theo cùng 1 cơ chế:
    // đếm tần suất, lưu dạng JSON {"value": count, ...} vào preference_value
    private void trackFrequentValues(Long userId, String preferenceKey, List<String> newValues) {
        Optional<UserPreference> existing = userPreferenceRepository
                .findByUserIdAndPreferenceKey(userId, preferenceKey);

        Map<String, Integer> freqMap = new HashMap<>();
        if (existing.isPresent()) {
            try {
                freqMap = objectMapper.readValue(existing.get().getPreferenceValue(),
                        new TypeReference<Map<String, Integer>>() {});
            } catch (Exception e) {
                log.warn("Không parse được {} cũ của userId={}, reset lại", preferenceKey, userId);
            }
        }

        for (String val : newValues) {
            String key = val.trim().toLowerCase();
            if (!key.isBlank()) {
                freqMap.merge(key, 1, Integer::sum);
            }
        }

        try {
            String json = objectMapper.writeValueAsString(freqMap);
            upsertPreference(userId, preferenceKey, json);
        } catch (Exception e) {
            log.error("Lỗi lưu {} cho userId={}: ", preferenceKey, userId, e);
        }
    }

    private void upsertPreference(Long userId, String key, String value) {
        UserPreference pref = userPreferenceRepository
                .findByUserIdAndPreferenceKey(userId, key)
                .orElse(UserPreference.builder()
                        .userId(userId)
                        .preferenceKey(key)
                        .build());
        pref.setPreferenceValue(value);
        userPreferenceRepository.save(pref);
    }

    // =========================================================
    // Map 1 dòng Object[] (từ native query) -> SuggestionItem
    // Dùng chung cho nhánh sort_by và nhánh region-only (không qua callGroqRank)
    // =========================================================
    private ChatResponse.SuggestionItem mapRowToSuggestion(Object[] row, String reasonFallback) {
        // ⭐ SỬA: index dịch thêm 1 vì có thêm cột slug ở vị trí row[1]
        return ChatResponse.SuggestionItem.builder()
                .recipeId(((Number) row[0]).longValue())
                .slug((String) row[1])
                .title((String) row[2])
                .thumbnail((String) row[3])
                .cookingTime(row[4] != null ? ((Number) row[4]).intValue() : null)
                .avgRating(row[5] != null ? new BigDecimal(row[5].toString()) : BigDecimal.ZERO)
                .matchScore(null)
                .missingIngredients(List.of())
                .aiReason(reasonFallback)
                .build();
    }

    // =========================================================
    // Tìm công thức thật trong DB theo filter AI trích xuất được
    // Thứ tự ưu tiên các nhánh:
    //   1. sort_by (top_rated / most_liked) khi KHÔNG có ingredient
    //   2. region-only khi có region nhưng KHÔNG có ingredient
    //   3. luồng gốc: tìm theo ingredients (có thể kèm diet, region, cooking_time_max)
    // =========================================================
    private List<ChatResponse.SuggestionItem> suggestRecipes(ChatFilters filters) {

        boolean hasIngredients = filters.getIngredients() != null && !filters.getIngredients().isEmpty();

        // Nhánh 1: sort_by (top_rated / most_liked), không cần ingredient
        if (filters.getSortBy() != null && !hasIngredients) {
            List<Object[]> rows = "most_liked".equalsIgnoreCase(filters.getSortBy())
                    ? recipeRepository.findMostLikedRecipes()
                    : recipeRepository.findTopRatedRecipesRaw();

            if (rows.isEmpty()) {
                return List.of();
            }

            String reason = "most_liked".equalsIgnoreCase(filters.getSortBy())
                    ? "Món được nhiều người yêu thích"
                    : "Món có đánh giá cao từ cộng đồng";

            return rows.stream()
                    .limit(3)
                    .map(row -> mapRowToSuggestion(row, reason))
                    .toList();
        }

        // Nhánh 2: region-only, không cần ingredient
        String regionRaw = filters.getRegion() != null ? filters.getRegion().trim().toLowerCase() : null;
        if (regionRaw != null && !hasIngredients) {
            String regionSlug = switch (regionRaw) {
                case "bac" -> "mien-bac";
                case "trung" -> "mien-trung";
                case "nam" -> "mien-nam";
                default -> null;
            };

            if (regionSlug != null) {
                List<Object[]> rows = recipeRepository.findRecipesByRegionOnly(regionSlug);
                if (rows.isEmpty()) {
                    return List.of();
                }
                return rows.stream()
                        .limit(3)
                        .map(row -> mapRowToSuggestion(row, "Món đặc trưng vùng miền bạn chọn"))
                        .toList();
            }
        }

        // ================= Nhánh 3 (luồng gốc): tìm theo ingredients =================
        if (!hasIngredients) {
            return List.of();
        }

        List<String> keywords = filters.getIngredients().stream()
                .map(s -> s.trim().toLowerCase())
                .distinct()
                .toList();

        // ⭐ SỬA: exact match trước, nếu không có thì fallback sang match theo từ-đầu
        // (fix bug "cá" không match được vì DB chỉ có tên cụ thể như "Cá lóc", "Cá thu"...)
        List<String> expandedIngredients = keywords.stream()
                .flatMap(keyword -> {
                    Optional<Ingredient> exact = ingredientRepository.findByNameIgnoreCase(keyword);
                    if (exact.isPresent()) {
                        return Stream.of(exact.get().getName().toLowerCase());
                    }
                    return ingredientRepository.findByNameStartingWithWord(keyword).stream()
                            .map(i -> i.getName().toLowerCase());
                })
                .distinct()
                .toList();

        if (expandedIngredients.isEmpty()) {
            return List.of();
        }

        String dietRaw = filters.getDiet() != null ? filters.getDiet().trim().toLowerCase() : null;
        String dietMode = "ANY";
        if ("chay".equals(dietRaw)) {
            dietMode = "CHAY";
        } else if ("man".equals(dietRaw) || "mặn".equals(dietRaw)) {
            dietMode = "MAN";
        }

        // ⭐ MỚI: map region sang regionMode dùng cho query findCandidatesByIngredients
        String regionMode = "ANY";
        if ("bac".equals(regionRaw)) {
            regionMode = "BAC";
        } else if ("trung".equals(regionRaw)) {
            regionMode = "TRUNG";
        } else if ("nam".equals(regionRaw)) {
            regionMode = "NAM";
        }

        List<Object[]> rows = recipeRepository.findCandidatesByIngredients(
                expandedIngredients, dietMode, regionMode, filters.getCookingTimeMax());
        if (rows.isEmpty()) {
            return List.of();
        }

        List<String> excluded = filters.getExcludedIngredients() != null
                ? filters.getExcludedIngredients().stream()
                .map(s -> s.trim().toLowerCase())
                .filter(s -> !s.isBlank())
                .toList()
                : List.of();

        if (!excluded.isEmpty()) {
            rows = rows.stream()
                    .filter(row -> {
                        // ⭐ SỬA: index dịch thêm 1 vì có thêm cột slug ở vị trí row[1]
                        String rawIngredients = ((String) row[6]).toLowerCase();
                        return excluded.stream().noneMatch(rawIngredients::contains);
                    })
                    .toList();
        }

        if (rows.isEmpty()) {
            return List.of();
        }

        // ⭐ SỬA: index dịch thêm 1 vì có thêm cột slug ở vị trí row[1]
        List<Map<String, Object>> context = rows.stream().map(row -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", ((Number) row[0]).longValue());
            map.put("name", row[2]);
            map.put("cooking_time", row[4]);
            map.put("avg_rating", row[5]);
            String rawIngredients = (String) row[6];
            List<String> ingredientList = Arrays.stream(rawIngredients.split("\\|"))
                    .map(s -> {
                        String[] parts = s.split(":");
                        String qty = parts.length > 1 ? parts[1] : "";
                        String unit = parts.length > 2 ? parts[2] : "";
                        return parts[0] + (qty.isEmpty() ? "" : " (" + qty + " " + unit + ")");
                    }).toList();
            map.put("ingredients", ingredientList);
            return map;
        }).toList();

        AiRankResult rankResult = callGroqRank(expandedIngredients, context, filters);

        Map<Long, Object[]> rowMap = rows.stream()
                .collect(Collectors.toMap(r -> ((Number) r[0]).longValue(), r -> r));

        return rankResult.getSuggestions().stream()
                .filter(s -> rowMap.containsKey(s.getRecipeId()))
                .collect(Collectors.toMap(
                        s -> s.getRecipeId(),
                        s -> s,
                        (existing, duplicate) -> existing.getMatchScore() >= duplicate.getMatchScore() ? existing : duplicate,
                        LinkedHashMap::new
                ))
                .values().stream()
                .map(s -> {
                    Object[] row = rowMap.get(s.getRecipeId());
                    // ⭐ SỬA: index dịch thêm 1 vì có thêm cột slug ở vị trí row[1]
                    return ChatResponse.SuggestionItem.builder()
                            .recipeId(s.getRecipeId())
                            .slug((String) row[1])
                            .title((String) row[2])
                            .thumbnail((String) row[3])
                            .avgRating(row[5] != null
                                    ? new BigDecimal(row[5].toString()) : BigDecimal.ZERO)
                            .cookingTime(row[4] != null
                                    ? ((Number) row[4]).intValue() : null)
                            .matchScore(s.getMatchScore())
                            .missingIngredients(s.getMissingIngredients())
                            .aiReason(s.getReason())
                            .build();
                }).toList();
    }

    // =========================================================
    // Gọi Groq lần 1: hiểu ý định + trả lời tự nhiên
    // =========================================================
    private AiChatRawResult callGroqChat(List<ChatMessage> history, String preferenceContext) {
        List<Map<String, String>> messages = new ArrayList<>();

        String systemPrompt = """
        Bạn là "Bếp Ơi" - trợ lý ẩm thực Việt Nam, nói chuyện gần gũi
        như người thân trong nhà, thỉnh thoảng dùng từ dân dã
        ("nè", "đó nha", "ngon lắm luôn"). Không trả lời máy móc,
        hãy nói như đang trò chuyện thật sự.

        QUY TẮC XƯNG HÔ BẮT BUỘC: Luôn xưng "mình" và gọi người dùng là "bạn".
        TUYỆT ĐỐI KHÔNG dùng "tao", "mày", "anh chị", "cô/chú", hay bất kỳ
        đại từ suồng sã/thiếu tôn trọng nào khác. Gần gũi nhưng luôn lịch sự,
        tôn trọng người đối diện.

        QUAN TRỌNG: CHỈ trả lời bằng tiếng Việt thuần túy,
        tuyệt đối không chèn bất kỳ ký tự hay từ ngữ tiếng Anh/Trung/ngôn ngữ khác nào.

        QUAN TRỌNG NHẤT: Luôn bám sát và trả lời đúng trọng tâm CÂU HỎI MỚI NHẤT
        của user. Lịch sử hội thoại và preference cũ chỉ dùng để bổ sung ngữ
        cảnh, KHÔNG được lấn át hay thay thế nội dung câu hỏi hiện tại. Nếu
        câu hỏi mới chuyển sang chủ đề khác hẳn so với các câu trước, PHẢI
        trả lời theo chủ đề mới, không tiếp tục bám theo chủ đề cũ.

        KHÔNG được tự suy đoán hay bịa ra tình huống không có căn cứ rõ ràng
        (vd tự cho rằng "bạn muốn hỏi lại câu cũ" khi không có bằng chứng
        trong lịch sử hội thoại). Nếu không chắc chắn hiểu đúng ý, hãy hỏi
        thẳng lại một cách tự nhiên, đừng suy diễn.

        Quy tắc độ dài reply_text: TỐI ĐA 2-3 câu ngắn. Không liệt kê quá
        2-3 món trong câu trả lời (nếu cần nhiều lựa chọn hơn, để phần
        suggestions xử lý, reply_text chỉ nói khái quát và hỏi lại ngắn gọn).

        QUAN TRỌNG - KHÔNG ĐƯỢC BỊA TÊN MÓN CỤ THỂ TRONG reply_text:
        Ở bước này bạn CHƯA biết chính xác trong hệ thống có món nào - danh sách
        món thật sẽ được hệ thống tự tìm và hiển thị riêng SAU câu trả lời của bạn.
        Vì vậy, khi intent = "recipe_suggestion", TUYỆT ĐỐI KHÔNG nêu tên món ăn
        cụ thể (vd "cá lóc kho tộ", "gà nướng mật ong") trong reply_text, vì tên
        đó có thể không khớp với món thật sẽ được gợi ý bên dưới, gây hiểu lầm.
        Chỉ nói khái quát, ví dụ: "Để mình tìm vài món cá ngon cho bạn nè!",
        "Mình có vài công thức phù hợp, xem thử bên dưới nha!".

        LUÔN trả về JSON, KHÔNG thêm bất kỳ text nào khác ngoài JSON:
        {
          "reply_text": "câu trả lời tự nhiên bằng tiếng Việt",
          "intent": "recipe_suggestion" hoặc "general_chat" hoặc "clarify",
          "filters": {
            "ingredients": ["nguyên liệu user CÓ SẴN và muốn dùng"],
            "excluded_ingredients": ["nguyên liệu user KHÔNG muốn dùng hoặc dị ứng"],
            "diet": "chay" hoặc "man" hoặc null,
            "cooking_time_max": số phút hoặc null,
            "sort_by": "top_rated" hoặc "most_liked" hoặc null,
            "region": "bac" hoặc "trung" hoặc "nam" hoặc null
          }
        }

        Quy tắc chọn intent:
        - "recipe_suggestion": khi user có ý muốn tìm món ăn/công thức cụ thể,
          có đủ thông tin để tìm (ít nhất biết loại món, nguyên liệu, chế độ ăn,
          vùng miền, hoặc muốn xem món nổi bật).
        - "clarify": khi user muốn tìm món nhưng còn thiếu thông tin quan trọng
          (vd nói "ăn kiêng" nhưng chưa rõ kiêng kiểu gì) -> hỏi lại trong reply_text,
          filters để null.
        - "general_chat": khi user chỉ trò chuyện thông thường, không có ý tìm món,
          filters để null.

        QUAN TRỌNG - ƯU TIÊN CHỐT KHI USER RA TÍN HIỆU MUỐN NHẬN CÔNG THỨC NGAY:
        Nếu user dùng các cụm thể hiện muốn chốt/nhận công thức ngay, ví dụ:
        "gửi công thức đi", "món nào cũng được", "cho mình luôn đi",
        "nấu đại đi", "sao cũng được", "tùy mình" - thì PHẢI ưu tiên intent =
        "recipe_suggestion" NGAY LẬP TỨC, dùng nguyên liệu/chủ đề đã có trong
        lịch sử hội thoại gần nhất (kể cả khi user không lặp lại rõ ràng trong
        câu hiện tại). TUYỆT ĐỐI KHÔNG hỏi lại thêm chi tiết trong trường hợp này,
        kể cả khi thông tin còn khá chung chung - hãy tìm với thông tin đang có,
        đừng làm user phải trả lời thêm câu hỏi khi họ đã nói rõ muốn chốt ngay.

        Ví dụ minh họa (few-shot):
        1. Lịch sử: "bò" -> "món liên quan đến bò đi" -> AI liệt kê vài món bò
           -> User: "bò nào cũng được gửi công thức cho mình đi"
           => intent PHẢI là "recipe_suggestion", filters.ingredients = ["thịt bò"],
           reply_text kiểu "Để mình tìm công thức món bò cho bạn nha!"
           SAI: hỏi lại "bạn đang có thịt bò gì, muốn nấu món gì"
           (vì user đã nói rõ "nào cũng được" - tức không cần hỏi thêm).

        2. Lịch sử: "muốn ăn chay" (chưa nói nguyên liệu gì)
           -> User: "món nào cũng được, gợi ý đại đi"
           => Nếu HOÀN TOÀN chưa có nguyên liệu/loại món nào được nhắc trong suốt
           lịch sử (không chỉ câu hiện tại), vẫn có thể dùng intent =
           "recipe_suggestion" với filters.diet = "chay", ingredients để trống -
           hệ thống sẽ tự gợi ý theo diet, KHÔNG cần hỏi lại thêm.

        3. Lịch sử: chưa có bất kỳ nguyên liệu, món ăn, hay diet nào được nhắc tới
           -> User: "nấu gì cũng được"
           => Trường hợp này mới thực sự là "clarify", vì không có bất kỳ thông
           tin nào để tìm kiếm - hỏi lại user muốn ăn loại gì (mặn/chay, thịt/cá...).

        LƯU Ý KHI TRÍCH XUẤT INGREDIENTS TRONG TRƯỜNG HỢP CHỐT NGAY:
        Khi áp dụng rule "ưu tiên chốt" ở trên, PHẢI điền vào filters.ingredients
        tên loại nguyên liệu chính đang được nhắc tới trong ngữ cảnh (dù user chỉ
        nói chung chung, không chỉ rõ loại cụ thể), KHÔNG được để ingredients
        rỗng - vì rỗng sẽ khiến hệ thống không tìm được công thức nào, làm user
        không nhận được gợi ý dù đã chốt.

        4. Lịch sử: "cho tao những món liên quan đến cá" -> AI hỏi lại "cá kho tộ,
           cá nướng muối ớt, hay cá chiên xù, thích món nào" -> User: "cá gì cũng được"
           => intent = "recipe_suggestion", filters.ingredients = ["cá"]
           (lấy từ chủ đề "cá" đã được nhắc trong lịch sử, dù user không nói tên
           món cá cụ thể). SAI: để ingredients = [] hoặc null.

        Quy tắc sort_by (khi user hỏi món nổi bật, không phải theo nguyên liệu cụ thể):
        - "top_rated": khi user hỏi "món ngon nhất", "được đánh giá cao nhất",
          "chất lượng nhất", "nổi tiếng nhất".
        - "most_liked": khi user hỏi "món được thích nhiều nhất", "nhiều like nhất",
          "hot nhất", "được nhiều người yêu thích nhất".
        - Khi dùng sort_by, ingredients có thể để TRỐNG nếu user không nhắc nguyên
          liệu cụ thể - hệ thống sẽ tự lấy top công thức theo tiêu chí này,
          KHÔNG cần hỏi lại thêm.
        - Nếu user vừa nhắc ingredient vừa muốn "ngon nhất trong số đó" (vd
          "món cá nào ngon nhất") thì vẫn điền ingredients bình thường, sort_by
          để null - hệ thống sẽ ưu tiên rating khi xếp hạng theo ingredient.

        5. Lịch sử: chưa nhắc nguyên liệu/món ăn nào
           -> User: "cho tao món được đánh giá cao nhất"
           => intent = "recipe_suggestion", filters.ingredients = [],
           filters.sort_by = "top_rated"

        6. Lịch sử: chưa nhắc nguyên liệu/món ăn nào
           -> User: "món nào được like nhiều nhất vậy"
           => intent = "recipe_suggestion", filters.ingredients = [],
           filters.sort_by = "most_liked"

        ⭐ MỚI - Quy tắc region (vùng miền):
        - Trả "bac" nếu user nói "miền Bắc", "món Bắc", "ẩm thực Bắc Bộ", "Hà Nội"...
        - Trả "trung" nếu user nói "miền Trung", "món Huế", "Đà Nẵng"...
        - Trả "nam" nếu user nói "miền Nam", "món Nam Bộ", "Sài Gòn"...
        - Để null nếu không có tín hiệu rõ ràng về vùng miền.
        - Khi user CHỈ hỏi theo vùng miền (không nhắc ingredient cụ thể), vẫn dùng
          intent = "recipe_suggestion", ingredients để trống, KHÔNG cần hỏi lại.

        7. Lịch sử: chưa nhắc nguyên liệu/món ăn nào
           -> User: "gợi ý món liên quan đến miền Bắc đi"
           => intent = "recipe_suggestion", filters.ingredients = [],
           filters.region = "bac"

        Quy tắc nhận diện diet (áp dụng bất kể intent là gì):
        - Trả "chay" nếu user nói: "ăn chay", "không ăn thịt", "thuần chay", "vegan",
          "theo đạo nên kiêng thịt", "không ăn đồ tanh", hoặc bất kỳ cách diễn đạt nào
          thể hiện rõ họ không dùng nguyên liệu động vật.
        - Trả "man" nếu user nói rõ "ăn mặn", "ăn thịt bình thường".
        - Để null nếu không có tín hiệu rõ ràng về chế độ ăn - KHÔNG suy đoán diet
          chỉ dựa vào loại nguyên liệu đang có trong tay (vì user có thể chưa nói hết
          dự định nấu món gì).

        Quy tắc excluded_ingredients: nếu user nói "không thích X", "dị ứng X",
        "đừng cho X vào", "ghét X", hãy điền X vào excluded_ingredients, TUYỆT ĐỐI
        không điền vào ingredients (để tránh hệ thống hiểu nhầm là user đang
        có/muốn dùng X).

        Quy tắc trích xuất ingredients: LUÔN dùng tên nguyên liệu đầy đủ, cụ thể
        theo cách gọi phổ biến trong ẩm thực Việt Nam (vd "thịt gà" thay vì chỉ
        "gà", "thịt heo" thay vì chỉ "heo") để tránh nhầm với nguyên liệu khác
        (vd "gà" có thể bị hiểu nhầm thành "trứng gà" hay "nấm đùi gà"). Chỉ
        trích xuất tối đa 5 nguyên liệu quan trọng nhất mà user thực sự nhắc
        tới trực tiếp, KHÔNG tự suy diễn thêm nguyên liệu user không đề cập.
        Điền vào filters.ingredients kể cả khi intent là "clarify" hoặc
        "general_chat" - miễn là có nhắc tới tên nguyên liệu/món ăn cụ thể.
        """ + preferenceContext;

        messages.add(Map.of("role", "system", "content", systemPrompt));

        for (ChatMessage msg : history) {
            messages.add(Map.of(
                    "role", msg.getRole() == ChatRole.USER ? "user" : "assistant",
                    "content", msg.getContent()
            ));
        }

        String rawJson = callGroqRaw(messages);
        return parseJson(rawJson, AiChatRawResult.class);
    }

    // =========================================================
    // Gọi Groq lần 2: rank công thức thật lấy từ DB
    // =========================================================
    private AiRankResult callGroqRank(List<String> ingredients,
                                      List<Map<String, Object>> context,
                                      ChatFilters filters) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("Người dùng có: ").append(String.join(", ", ingredients)).append(".\n");
            if ("chay".equalsIgnoreCase(filters.getDiet())) {
                sb.append("Người dùng ĂN CHAY. TUYỆT ĐỐI KHÔNG được gợi ý bất kỳ món nào chứa thịt, cá, hải sản, trứng, hay nguyên liệu động vật. Loại bỏ hoàn toàn các món này khỏi kết quả, kể cả khi chúng có nguyên liệu trùng khớp.\n");
            }
            if (filters.getCookingTimeMax() != null) {
                sb.append("Thời gian tối đa: ").append(filters.getCookingTimeMax()).append(" phút.\n");
            }

            sb.append("\nDanh sách công thức:\n")
                    .append(objectMapper.writeValueAsString(context));

            sb.append("""
                \nChọn TOP 3 phù hợp nhất. Ưu tiên:
                1. Nguyên liệu khớp là thành phần CHÍNH của món (số lượng lớn),
                   không phải gia vị/phụ liệu phụ (số lượng nhỏ).
                2. Nhiều nguyên liệu khớp hơn.
                3. Thời gian ngắn hơn, rating cao hơn.
                MỖI recipe_id CHỈ được xuất hiện DUY NHẤT MỘT LẦN trong kết quả.
                Trả về JSON, KHÔNG thêm text nào khác:
                {
                  "suggestions": [
                    {
                      "recipe_id": <số>,
                      "match_score": <0-100>,
                      "missing_ingredients": ["tên nguyên liệu còn thiếu"],
                      "reason": "Giải thích ngắn bằng tiếng Việt"
                    }
                  ]
                }
                """);

            String rawJson = callGroqRaw(List.of(Map.of("role", "user", "content", sb.toString())));
            return parseJson(rawJson, AiRankResult.class);

        } catch (Exception e) {
            log.error("Lỗi build prompt rank: ", e);
            throw new AppException(ErrorCode.AI_SERVICE_ERROR);
        }
    }

    // =========================================================
    // Hàm dùng chung gọi Groq API
    // =========================================================
    private String callGroqRaw(List<Map<String, String>> messages) {
        try {
            Map<String, Object> body = Map.of(
                    "model", MODEL,
                    "messages", messages,
                    "response_format", Map.of("type", "json_object")
            );

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(GROQ_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + groqApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(
                            objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() != 200) {
                log.error("Groq API lỗi {}: {}", res.statusCode(), res.body());
                throw new AppException(ErrorCode.AI_SERVICE_ERROR);
            }

            JsonNode root = objectMapper.readTree(res.body());
            String rawText = root.path("choices").get(0).path("message").path("content").asText();

            String cleaned = rawText.replaceAll("(?s)```json|```", "").trim();

            int start = cleaned.indexOf('{');
            int end = cleaned.lastIndexOf('}');
            if (start != -1 && end != -1 && end > start) {
                cleaned = cleaned.substring(start, end + 1);
            }

            return cleaned;

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Lỗi gọi Groq: ", e);
            throw new AppException(ErrorCode.AI_SERVICE_ERROR);
        }
    }

    private <T> T parseJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("Lỗi parse JSON từ AI: {}", json, e);
            throw new AppException(ErrorCode.AI_SERVICE_ERROR);
        }
    }
}