package com.example.Bep_Viet.request;

import lombok.Data;

@Data
public class CookBookRequest {
    private String name;
    private String description;
    private Boolean isPublic= true;
}