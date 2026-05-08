CREATE TABLE users (
id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
name VARCHAR(255) NOT NULL UNIQUE,
email VARCHAR(255) NOT NULL UNIQUE,
created_date TIMESTAMPTZ DEFAULT NOW(),
subscription_id UUID UNIQUE
);
CREATE TABLE subscription (
id BIGSERIAL PRIMARY KEY,
user_id UUID UNIQUE  NOT NULL REFERENCES users(id),
user_subscription_id UUID UNIQUE NOT NULL  REFERENCES users(subscription_id),
status BOOLEAN DEFAULT FALSE,
last_updated TIMESTAMPTZ,
version INT
);

