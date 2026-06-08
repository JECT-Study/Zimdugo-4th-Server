package com.zimdugo.locker.infrastructure.persistence;

import com.zimdugo.locker.domain.LockerVoteType;
import com.zimdugo.user.infrastructure.persistence.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "locker_votes",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_locker_votes_user_locker", columnNames = {"user_id", "locker_id"})
    },
    indexes = {
        @Index(name = "idx_locker_votes_user_id", columnList = "user_id"),
        @Index(name = "idx_locker_votes_locker_id", columnList = "locker_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LockerVoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "locker_id", nullable = false)
    private LockerEntity locker;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LockerVoteType voteType;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public LockerVoteEntity(UserEntity user, LockerEntity locker, LockerVoteType voteType) {
        this.user = user;
        this.locker = locker;
        this.voteType = voteType;
    }

    public void changeVoteType(LockerVoteType voteType) {
        this.voteType = voteType;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
