ALTER TABLE subscription
DROP CONSTRAINT subscription_user_id_fkey;

ALTER TABLE subscription
DROP CONSTRAINT subscription_user_subscription_id_fkey;

ALTER TABLE subscription
ADD CONSTRAINT subscription_user_id_fkey FOREIGN KEY (user_id)
REFERENCES users (id)
ON DELETE CASCADE;

ALTER TABLE subscription
ADD CONSTRAINT subscription_user_subscription_id_fkey FOREIGN KEY (user_subscription_id)
REFERENCES users (subscription_id)
ON DELETE CASCADE;
