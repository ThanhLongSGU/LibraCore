-- Run this migration once on an existing LibraCore database.
ALTER TABLE Sach
    ADD COLUMN GiaNhap DECIMAL(12,2) NOT NULL DEFAULT 0.00 AFTER SoTrang;

-- Seed each book's default import price from its most recent non-cancelled receipt.
UPDATE Sach s
SET s.GiaNhap = (
    SELECT COALESCE(ct.GiaTien, 0.00)
    FROM ChiTietPhieuNhap ct
    JOIN PhieuNhap pn ON pn.id_PhieuNhap = ct.id_PhieuNhap
    WHERE ct.id_Sach = s.id_Sach
      AND pn.TrangThai <> 'DaHuy'
    ORDER BY pn.NgayNhap DESC, pn.id_PhieuNhap DESC, ct.id_ChiTietPhieuNhap DESC
    LIMIT 1
)
WHERE EXISTS (
    SELECT 1
    FROM ChiTietPhieuNhap ct
    JOIN PhieuNhap pn ON pn.id_PhieuNhap = ct.id_PhieuNhap
    WHERE ct.id_Sach = s.id_Sach
      AND pn.TrangThai <> 'DaHuy'
);
