INSERT INTO books (id, title, price, is_published) VALUES (1, 'テストブック', 1000, true);
INSERT INTO books (id, title, price, is_published) VALUES (2, 'テストブック2', 2000, false);
INSERT INTO books (id, title, price, is_published) VALUES (3, 'テストブック3', 3000, false);

INSERT INTO authors (id, name, birthday) VALUES (1, 'テスト著者', '1995-01-01');
INSERT INTO authors (id, name, birthday) VALUES (2, 'テスト著者2', '1995-02-01');
INSERT INTO authors (id, name, birthday) VALUES (3, 'テスト著者3', '1995-03-01');
INSERT INTO authors (id, name, birthday) VALUES (4, 'テスト著者4', '1995-04-01');

INSERT INTO books_authors (book_id, author_id) VALUES (1, 1);
INSERT INTO books_authors (book_id, author_id) VALUES (1, 2);
INSERT INTO books_authors (book_id, author_id) VALUES (2, 2);
INSERT INTO books_authors (book_id, author_id) VALUES (2, 3);
INSERT INTO books_authors (book_id, author_id) VALUES (3, 4);
