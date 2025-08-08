TRUNCATE TABLE properties RESTART IDENTITY CASCADE;

INSERT INTO properties (id, name, description, type, price, rooms, bathrooms, roommates,
                        furnished, address, city, postal_code, country, latitude, longitude)
VALUES (gen_random_uuid(), 'Piso Triana Calle Betis',
        'Luminoso piso con vistas al río Guadalquivir',
        'apartment', 850, 3, 1, 2, true,
        'Calle Betis 45', 'Sevilla', '41010', 'España',
        '37.3826', '-6.0036'),
       (gen_random_uuid(), 'Apartamento Triana San Jacinto',
        'Apartamento moderno para estudiantes',
        'apartment', 700, 2, 1, 1, true,
        'Calle San Jacinto 102', 'Sevilla', '41010', 'España',
        '37.3861', '-6.0024'),
       (gen_random_uuid(), 'Estudio en Alfarería',
        'Estudio reformado',
        'apartment', 550, 1, 1, 0, false,
        'Calle Alfarería 23', 'Sevilla', '41010', 'España',
        '37.3847', '-6.0020'),
       (gen_random_uuid(), 'Apartamento en Triana',
        'Apartamento para 4 personas',
        'apartment', 200, 4, 2, 0, true,
        'Av Santa Cecilia 23', 'Sevilla', '41010', 'España',
        '37.3847', '-6.0020');
