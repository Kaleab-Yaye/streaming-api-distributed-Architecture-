DROP TABLE vid;

ALTER  TABLE  users ADD COLUMN is_studio BOOLEAN DEFAULT FALSE;

CREATE  TYPE vid_state AS ENUM ('UPLOADED','APPROVED', 'ENCODED', 'ENCODERR', 'MODEREGCT');
--- for now those are the skelten enums.

CREATE TABLE vid (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    about VARCHAR(255) NOT NULL,
    uploaded_at timestamptz DEFAULT NOW(),
    vid_stat vid_state DEFAULT 'UPLOADED',
    uploaded_by UUID UNIQUE  NOT NULL REFERENCES users(id)
)
