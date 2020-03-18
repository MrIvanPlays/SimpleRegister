CREATE TABLE IF NOT EXISTS "simpleregister_passwords" (
  "id" SERIAL PRIMARY KEY,
  "name" VARCHAR(255),
  "uuid" VARCHAR(36),
  "ip" VARCHAR(255),
  "password" VARCHAR(255)
);
CREATE INDEX "simpleregister_passwords_uuid" ON "simpleregister_passwords" ("uuid");
