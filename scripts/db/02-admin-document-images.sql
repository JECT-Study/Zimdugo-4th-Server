CREATE TABLE IF NOT EXISTS admin_document_images (
    id BIGSERIAL PRIMARY KEY,
    admin_document_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    list_order INTEGER NOT NULL,
    CONSTRAINT fk_admin_document_images_document
        FOREIGN KEY (admin_document_id) REFERENCES admin_documents (id) ON DELETE CASCADE
);

INSERT INTO admin_document_images (admin_document_id, image_url, list_order)
SELECT document.id, document.image_url, 0
FROM admin_documents document
WHERE document.image_url IS NOT NULL
  AND BTRIM(document.image_url) <> ''
  AND NOT EXISTS (
      SELECT 1
      FROM admin_document_images image
      WHERE image.admin_document_id = document.id
  );
