CREATE TABLE watches (
    id uuid primary key DEFAULT uuid_generate_v4(),
    vid_id uuid REFERENCES vid(id) ON DELETE CASCADE,
    user_id uuid REFERENCES users(id) ON DELETE cascade,
    last_segment VARCHAR(225),
    liked bool,
    times_rewatch INT
    );

CREATE INDEX  watches_vid_id_index ON watches(vid_id);
CREATE INDEX watches_user_id_index ON watches(user_id);

