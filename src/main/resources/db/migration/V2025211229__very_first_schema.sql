 CREATE TABLE users(
     user_name VARCHAR(255) NOT NULL,
     id UUID PRIMARY KEY NOT NULL,
     password VARCHAR(255) NOT NULL, -- must be hashed and stalted with bycrypet
     balance BIGINT,
     type VARCHAR(20) NOT NULL
 );

CREATE TABLE vid(
    name VARCHAR(255) NOT NULL, --this cooudl be more than this
    id BIGSERIAL PRIMARY KEY ,
    user_id UUID references users(id) ON DELETE CASCADE,
    status INT NOT NULL

);
CREATE TABLE subscriptions(
    id BIGSERIAL PRIMARY KEY ,
    subscriber_id UUID references users(id),
    subscribed_id UUID references users(id),
    subscription_level INT NOT NULL,

    CONSTRAINT prevent_self_subscription CHECK ( subscribed_id <> subscriber_id ),
    CONSTRAINT no_double_subbing UNIQUE (subscribed_id, subscriber_id )
);

CREATE INDEX index_owner ON vid(user_id);
CREATE INDEX index_subscriber ON subscriptions(subscriber_id);
CREATE INDEX index_subscribed ON subscriptions(subscribed_id);