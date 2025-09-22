-- Sample users
INSERT INTO users (username, email, password, first_name, last_name, active) VALUES
('admin', 'admin@example.com', 'password123', 'Admin', 'User', true),
('john.doe', 'john@example.com', 'password123', 'John', 'Doe', true),
('jane.smith', 'jane@example.com', 'password123', 'Jane', 'Smith', true),
('bob.wilson', 'bob@example.com', 'password123', 'Bob', 'Wilson', true),
('alice.brown', 'alice@example.com', 'password123', 'Alice', 'Brown', false);

-- Sample posts
INSERT INTO posts (title, content, user_id, status, view_count) VALUES
('Getting Started with Spring WebFlux', 'Spring WebFlux is a reactive web framework...', 1, 'PUBLISHED', 150),
('Understanding R2DBC', 'R2DBC brings reactive programming to relational databases...', 2, 'PUBLISHED', 230),
('Reactive Streams in Java', 'Reactive Streams specification provides a standard...', 2, 'PUBLISHED', 180),
('Building Microservices', 'Microservices architecture is an approach to developing...', 3, 'PUBLISHED', 420),
('Docker Best Practices', 'When working with Docker containers...', 1, 'DRAFT', 0),
('Kubernetes Fundamentals', 'Kubernetes is a container orchestration platform...', 4, 'PUBLISHED', 350),
('GraphQL vs REST', 'Comparing GraphQL and REST APIs...', 3, 'PUBLISHED', 280),
('CI/CD Pipeline Setup', 'Continuous Integration and Continuous Deployment...', 2, 'PUBLISHED', 190),
('Database Indexing Strategies', 'Proper indexing is crucial for database performance...', 1, 'PUBLISHED', 210),
('Security Best Practices', 'Web application security is paramount...', 4, 'DRAFT', 0);

-- Sample comments
INSERT INTO comments (content, post_id, user_id, parent_id) VALUES
('Great article! Very helpful.', 1, 2, NULL),
('Thanks for sharing this information.', 1, 3, NULL),
('Could you provide more examples?', 1, 4, NULL),
('I have a question about connection pooling.', 2, 1, NULL),
('Nice explanation of reactive streams.', 3, 4, NULL),
('This helped me understand the concepts better.', 3, 1, NULL),
('Excellent overview of microservices.', 4, 2, NULL),
('What about service mesh?', 4, 3, NULL),
('Very useful Docker tips!', 5, 2, NULL),
('Looking forward to the full article.', 5, 3, NULL);

-- Sample nested comments (replies)
INSERT INTO comments (content, post_id, user_id, parent_id) VALUES
('You are welcome!', 1, 1, 2),
('Sure, I will add more examples in the next post.', 1, 1, 3),
('Connection pooling is configured in the R2DBC settings.', 2, 2, 4),
('Service mesh will be covered in a separate article.', 4, 3, 8);