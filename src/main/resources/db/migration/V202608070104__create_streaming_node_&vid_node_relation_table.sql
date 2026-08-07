CREATE TABLE  streaming_node (
    id uuid primary key DEFAULT uuid_generate_v4(),
    ip_addr VARCHAR(255) NOT NULL, -- we could make this unique, but what if we dont want to remove entries
    port_number INT NOT NULL,
    up_stat BOOLEAN NOT NULL,
    updated_at timestamptz DEFAULT NOW()
);

CREATE TABLE vid_store_location(
    id uuid primary key DEFAULT  uuid_generate_v4(),
    streaming_node_id uuid references streaming_node(id) ON DELETE CASCADE,
    vid_id uuid references vid(id) ON DELETE  CASCADE
);

CREATE INDEX  store_location_streaming_node_id_index ON vid_store_location (streaming_node_id);
CREATE INDEX  streaming_node_id_index ON streaming_node(id);