CREATE TABLE IF NOT EXISTS patogeno (
  id int auto_increment NOT NULL ,
  tipo VARCHAR(255) NOT NULL UNIQUE,
  cantidad_de_especies int NOT NULL,
  PRIMARY KEY (id)
)
