package com.adnakiwoch.platform.streaming_api.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "vid_store_location")
public class VidStoreLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private  @Getter @Setter UUID id;

    @ManyToOne
    @JoinColumn(name="streaming_node_id")
    private @Getter @Setter StreamingNode streamingNode;

    @ManyToOne
    @JoinColumn(name="vid_id")
    private @Getter @Setter Vid vid;

}
