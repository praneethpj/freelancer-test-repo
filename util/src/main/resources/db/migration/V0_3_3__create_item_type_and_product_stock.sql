-- Create table for ItemType
CREATE TABLE item_type (
                           id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           name VARCHAR(255) NOT NULL,
                           type VARCHAR(255) NOT NULL DEFAULT 'normal',
                           activate BOOLEAN NOT NULL DEFAULT TRUE,
                           created_at TIMESTAMP,
                           updated_at TIMESTAMP
);

-- Create table for ProductStock
CREATE TABLE product_stock (
                               id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                               name VARCHAR(255) NOT NULL,
                               description TEXT NOT NULL DEFAULT '',
                               item_type_id INTEGER,
                               count INTEGER,
                               created_at TIMESTAMP,
                               updated_at TIMESTAMP,
                               CONSTRAINT fk_product_stock_item_type FOREIGN KEY (item_type_id)
                                   REFERENCES item_type (id) ON DELETE SET NULL
);
