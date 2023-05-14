1- Instalação da biblioteca jsoup através do seguinte link: https://jsoup.org/

2- Instalação da biblioteca postgres.sql através do seguinte link:
https://jdbc.postgresql.org/download/

3- criação de um user na base de dados com username: test e password: test , com todas as permissões.

4- Criação das base de dados, 1 para cada barrel com o nome db + numbarrel , correr o seguinte script na base de dados:

CREATE TABLE url (
    url     VARCHAR(512),
    title     VARCHAR(512),
    citation VARCHAR(512),
    PRIMARY KEY(url)
);

CREATE TABLE word (
    word VARCHAR(512),
    PRIMARY KEY(word)
);

CREATE TABLE url_url (
    url_url     VARCHAR(512),
    url_url1 VARCHAR(512),
    PRIMARY KEY(url_url,url_url1)
);

CREATE TABLE word_url (
    word_word VARCHAR(512),
    url_url     VARCHAR(512),
    PRIMARY KEY(word_word,url_url)
);

ALTER TABLE url_url ADD CONSTRAINT url_url_fk1 FOREIGN KEY (url_url) REFERENCES url(url);
ALTER TABLE url_url ADD CONSTRAINT url_url_fk2 FOREIGN KEY (url_url1) REFERENCES url(url);
ALTER TABLE word_url ADD CONSTRAINT word_url_fk1 FOREIGN KEY (word_word) REFERENCES word(word);
ALTER TABLE word_url ADD CONSTRAINT word_url_fk2 FOREIGN KEY (url_url) REFERENCES url(url);

5- O projeto corre com 3 barrels e 3 downloaders em que quando são iniciados necessitam de argumentos, sendo o argumento que deve ser fornecido o numero do barrel/downloader que se está a correr.

6- Para o correto funcionamento do programa deve ser iniciado os programas com uma ordem especifica: 
    1- SearchModule , 
    2- Interface , 
    3- UrlQueueMain, 
    4- Storage Barrels com devido numero em argumento.
    5- Downloaders com devido numero em argumento.