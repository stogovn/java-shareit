CREATE TABLE IF NOT EXISTS users (
id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
name VARCHAR(255) NOT NULL,
email VARCHAR(320),
CONSTRAINT unique_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS requests (
id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
description VARCHAR(255),
requestor_id BIGINT REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS items (
id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
name VARCHAR(255) NOT NULL,
description VARCHAR(255),
available BOOLEAN,
owner_id BIGINT REFERENCES users (id),
request_id BIGINT REFERENCES requests (id)
);

CREATE TABLE IF NOT EXISTS bookings (
id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
start_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
end_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
item_id BIGINT REFERENCES items(id),
booker_id BIGINT REFERENCES users(id),
status VARCHAR(10) NOT NULL
);

CREATE TABLE comments (
id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
text TEXT NOT NULL,
item_id BIGINT REFERENCES items(id),
author_id BIGINT REFERENCES users(id),
created TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);