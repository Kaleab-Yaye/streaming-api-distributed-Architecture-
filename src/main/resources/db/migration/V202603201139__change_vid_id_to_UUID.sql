DROP TABLE vid;
CREATE TABLE vid (
                     id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
                     name VARCHAR(255) NOT NULL UNIQUE,
                     about VARCHAR(255) NOT NULL,
                     uploaded_at timestamptz DEFAULT NOW(),
                     vid_stat vid_state DEFAULT 'UPLOADREQ',
                     uploaded_by UUID UNIQUE  NOT NULL REFERENCES users(id),
                     upload_location VARCHAR(555) DEFAULT 'NOT_SPECIFIED',
                     encoded_location VARCHAR (555) DEFAULT  'NOT_SPECIFIED',
                     size BIGINT

)