INSERT INTO usuarios (nome, email, senha, papel)
VALUES ('Administrador', 'admin@campusgigs.br', '$2a$10$tycjUMJusENYbfcc/q5/CubEzJTEh1oCHTAtTUysnC/VyjObHhhJ.', 'ADMIN')
    ON CONFLICT (email) DO NOTHING;