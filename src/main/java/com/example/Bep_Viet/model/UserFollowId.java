package com.example.Bep_Viet.model;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFollowId implements Serializable {
    @Column(name = "follower_id")
    private Long followerId;

    @Column(name = "following_id")
    private Long followingId;
}