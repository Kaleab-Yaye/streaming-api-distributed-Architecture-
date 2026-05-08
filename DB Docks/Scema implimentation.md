# users
* user_name: should be a VARCHAR(225)
* id: should be UUID
* password: should be a VARCHAR(225); it will be hashed and stored here.
* type: this will be VARCHAR(20); it will be either ("NORMAL_USER"), ("ADMIN"), ("MOD")
* balance: BIGINT to make sure there are no rounding errors from using DOUBLE or FLOAT

# vid
* name: VARCHAR(255)
* m_id: BIGSERIAL; we want the database to handle the ID.
* user_id: FK → users.id
* state: INT (0, 1, 2, 3)

# Subscription
* id: BIGSERIAL
* subscriber_id: FK → users.id
* subscribed_id: FK → users.id
* subscription_level: INT (1, 2, 3, 4)
