package com.example.Bep_Viet.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class SlugUtil {

    public static String toSlug(String input) {
        if (input == null) return "";

        // Bước 1: Bỏ dấu tiếng Việt "Gà Chiên" → "Ga Chien"
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String noAccent = pattern.matcher(normalized).replaceAll("");

        // Bước 2: Chuyển về chữ thường
        String slug = noAccent.toLowerCase()
                .replaceAll("đ", "d")            // xử lý riêng chữ đ
                .replaceAll("[^a-z0-9\\s-]", "") // bỏ ký tự đặc biệt
                .replaceAll("\\s+", "-")         // space → dấu -
                .replaceAll("-+", "-")           // nhiều dấu - → 1 dấu -
                .trim();

        return slug;
    }
}