DROP TABLE vid;
CREATE TABLE vid (
                     id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
                     name VARCHAR(255) NOT NULL UNIQUE,
                     about VARCHAR(255) NOT NULL,
                     uploaded_at timestamptz DEFAULT NOW(),
                     vid_stat vid_state DEFAULT 'UPLOADED',
                     uploaded_by UUID UNIQUE  NOT NULL REFERENCES users(id)
)