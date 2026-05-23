package bg.senpai.anime.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.UUID;

@Entity
@Table(name = "animes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Anime {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String hiAnimeId;

//    @OneToMany(mappedBy = "anime", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Favorite> favorites = new ArrayList<>();
//
//    @OneToMany(mappedBy = "anime", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Episode> episodes = new ArrayList<>();

}


