package dev.lucas.magicchef.recipe;

import dev.lucas.magicchef.common.StringListConverter;
import dev.lucas.magicchef.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recipes")
@Getter
@Setter
@NoArgsConstructor
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 600)
    private String summary;

    @Column(name = "chef_comment", length = 600)
    private String chefComment;

    @Column(length = 20)
    private String difficulty;

    @Column(name = "prep_time_minutes")
    private Integer prepTimeMinutes;

    private Integer servings;

    @Convert(converter = StringListConverter.class)
    @Column(nullable = false, length = 8000)
    private List<String> ingredients = new ArrayList<>();

    @Convert(converter = StringListConverter.class)
    @Column(nullable = false, length = 8000)
    private List<String> steps = new ArrayList<>();

    @Convert(converter = StringListConverter.class)
    @Column(length = 4000)
    private List<String> tips = new ArrayList<>();

    @Column(name = "user_request", nullable = false, length = 1000)
    private String userRequest;

    @Column(nullable = false)
    private boolean favorite;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Recipe(User user) {
        this.user = user;
        this.createdAt = Instant.now();
    }
}
