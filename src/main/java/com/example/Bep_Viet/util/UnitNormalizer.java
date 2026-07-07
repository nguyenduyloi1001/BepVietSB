package com.example.Bep_Viet.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class UnitNormalizer {

    // ── Alias map: chuẩn hóa cách viết về key thống nhất ─────────────────────
    private static final Map<String, String> ALIAS = Map.ofEntries(
            // Khối lượng
            Map.entry("g",              "g"),
            Map.entry("gram",           "g"),
            Map.entry("grams",          "g"),
            Map.entry("gr",             "g"),
            Map.entry("kg",             "kg"),
            Map.entry("kilo",           "kg"),
            Map.entry("kilogram",       "kg"),
            Map.entry("mg",             "mg"),

            // Thể tích
            Map.entry("ml",             "ml"),
            Map.entry("mililit",        "ml"),
            Map.entry("l",              "l"),
            Map.entry("lit",            "l"),
            Map.entry("lít",            "l"),
            Map.entry("liter",          "l"),
            Map.entry("tsp",            "tsp"),
            Map.entry("muỗng cà phê",   "tsp"),
            Map.entry("muong ca phe",   "tsp"),
            Map.entry("tbsp",           "tbsp"),
            Map.entry("muỗng canh",     "tbsp"),
            Map.entry("muong canh",     "tbsp"),
            Map.entry("cup",            "cup"),
            Map.entry("chén",           "cup"),
            Map.entry("chen",           "cup"),

            // Đếm — giữ nguyên key, chỉ normalize cách viết
            Map.entry("cái",            "cái"),
            Map.entry("cai",            "cái"),
            Map.entry("củ",             "củ"),
            Map.entry("cu",             "củ"),
            Map.entry("quả",            "quả"),
            Map.entry("qua",            "quả"),
            Map.entry("trái",           "trái"),
            Map.entry("trai",           "trái"),
            Map.entry("lá",             "lá"),
            Map.entry("la",             "lá"),
            Map.entry("nhánh",          "nhánh"),
            Map.entry("nhanh",          "nhánh"),
            Map.entry("tép",            "tép"),
            Map.entry("tep",            "tép"),
            Map.entry("hạt",            "hạt"),
            Map.entry("hat",            "hạt"),
            Map.entry("muỗng",          "muỗng"),
            Map.entry("muong",          "muỗng"),
            Map.entry("thìa",           "thìa"),
            Map.entry("thia",           "thìa")
    );

    // ── Hệ số quy về gram ────────────────────────────────────────────────────
    private static final Map<String, BigDecimal> TO_GRAM = Map.of(
            "kg", BigDecimal.valueOf(1000),
            "g",  BigDecimal.ONE,
            "mg", BigDecimal.valueOf(0.001)
    );

    // ── Hệ số quy về ml ──────────────────────────────────────────────────────
    private static final Map<String, BigDecimal> TO_ML = Map.of(
            "l",    BigDecimal.valueOf(1000),
            "ml",   BigDecimal.ONE,
            "tsp",  BigDecimal.valueOf(5),
            "tbsp", BigDecimal.valueOf(15),
            "cup",  BigDecimal.valueOf(240)
    );

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Chuẩn hóa cách viết unit về key thống nhất.
     * Ví dụ: "Gram", "grams", "gr" → "g"
     */
    public static String normalize(String unit) {
        if (unit == null || unit.isBlank()) return "không rõ";
        return ALIAS.getOrDefault(unit.trim().toLowerCase(), unit.trim().toLowerCase());
    }

    /**
     * Quy số lượng về đơn vị cơ bản (g hoặc ml).
     * Đơn vị đếm (cái, củ...) giữ nguyên số lượng.
     */
    public static BigDecimal toBase(BigDecimal qty, String unit) {
        if (qty == null) return BigDecimal.ZERO;
        String u = normalize(unit);
        if (TO_GRAM.containsKey(u)) {
            return qty.multiply(TO_GRAM.get(u)).setScale(2, RoundingMode.HALF_UP);
        }
        if (TO_ML.containsKey(u)) {
            return qty.multiply(TO_ML.get(u)).setScale(2, RoundingMode.HALF_UP);
        }
        return qty; // đơn vị đếm — giữ nguyên
    }

    /**
     * Trả về đơn vị cơ bản tương ứng.
     * kg/g/mg → "g" | l/ml/tsp/tbsp/cup → "ml" | còn lại → giữ nguyên
     */
    public static String baseUnit(String unit) {
        String u = normalize(unit);
        if (TO_GRAM.containsKey(u)) return "g";
        if (TO_ML.containsKey(u))   return "ml";
        return u;
    }
}