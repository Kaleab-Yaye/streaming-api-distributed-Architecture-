# The Aim
The aim is to create a streaming site. So generally speaking, let's see the actors of this website.

## Users
They are the target users of the website.

### What they can do:
* They can go live; but for now, they can upload videos.
* They can recharge their balance.
* They can subscribe to other users.
* They can watch videos that they are allowed to.

### What this entity has:
* They will have names.
* They will have a UUID.
* They will have a hashed password (possibly salted).
* They will have attributes to distinguish them ("Normal User", "Mod", "Admin").
* They will have a Balance attribute.

**NOTE:** This is the first schema and a lot of things will be added as it progresses.

## vod
These are the entities that users upload, stream, and watch. They are just video files (in the current state).

### What attributes this entity will have:
* They will have a name chosen by the person who owns it.
* They will have a unique media identification number.
* They will have a foreign key referencing the user who owns it.
* They will have a *state* attribute that indicates if the user who uploaded it wants it to be seen for free (0), or only for tier 3 subscribers, or tier 1.

## Subscriptions
This is a connecting table that will relate the users with their subscribers or a user they are subscribed to.

### The attributes:
* Subscriptions will have a unique ID.
* They will reference two users at once.
* The combination of the two users should be unique and will be enforced at the database level.
* They will have an attribute for subscription level: int (1, 2, 3), with 1 being the smallest subscription and 3 being the highest subscription.

## Mods
They are users as well, but they will have elevated privileges and can take down videos if they violate our streaming platform's guidelines.

## Admins
Admins are the highest privileged entities.
