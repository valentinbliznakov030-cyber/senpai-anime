package bg.senpai.anime.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Episode {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String m3u8Link;

    @Column(nullable = false)
    private Integer episodeNumber;

    @ManyToOne
    @JoinColumn(name = "anime_id", nullable = false)
    private Anime anime;


//    @OneToMany(mappedBy = "episode", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Comment> comments = new ArrayList<>();
}

