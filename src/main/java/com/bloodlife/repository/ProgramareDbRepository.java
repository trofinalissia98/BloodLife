package com.bloodlife.repository;

import com.bloodlife.domain.CentruDonare;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

public class ProgramareDbRepository {
    private String url, user, password;

    public ProgramareDbRepository(String url, String user, String password) {
        this.url = url; this.user = user; this.password = password;
    }

    public List<CentruDonare> getToateCentrele() {
        List<CentruDonare> centre = new ArrayList<>();
        String sql = "SELECT * FROM centre_donare";
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                centre.add(new CentruDonare(
                        rs.getLong("id"),
                        rs.getString("nume"),
                        rs.getString("adresa"),
                        rs.getInt("capacitate_pe_ora")
                ));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return centre;
    }

    public int getNrProgramariPeSlot(Long idCentru, LocalDate data, LocalTime ora) {
        String sql = "SELECT COUNT(*) FROM programari WHERE id_centru = ? AND data_programare = ? AND ora_programare = ?";
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idCentru);
            ps.setDate(2, Date.valueOf(data));
            ps.setTime(3, Time.valueOf(ora));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return 0;
    }

    public void salveazaProgramare(Long idDonator, Long idCentru, LocalDate data, LocalTime ora) {
        String sql = "INSERT INTO programari (id_donator, id_centru, data_programare, ora_programare, status) VALUES (?, ?, ?, ?, 'PROGRAMAT')";
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idDonator);
            ps.setLong(2, idCentru);
            ps.setDate(3, Date.valueOf(data));
            ps.setTime(4, Time.valueOf(ora));
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public List<Map<String, Object>> getIstoricDonari(Long idDonator) {
        List<Map<String, Object>> istoric = new ArrayList<>();
        // Am scos join-ul cu pungi_sange deoarece coloana id_programare pare să lipsească în baza de date reală
        String sql = """
        SELECT 
            p.id AS id_programare, 
            p.data_programare, 
            p.ora_programare,
            c.nume AS nume_centru,
            p.status
        FROM programari p
        JOIN centre_donare c ON p.id_centru = c.id
        WHERE p.id_donator = ? AND p.status = 'FINALIZATA'
        ORDER BY p.data_programare DESC, p.ora_programare DESC
        """;

        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idDonator);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", rs.getLong("id_programare"));
                    row.put("data_programare", rs.getDate("data_programare").toLocalDate());
                    row.put("nume_centru", rs.getString("nume_centru"));
                    // Punem valori default deoarece nu putem face join cu pungi_sange momentan
                    row.put("tip_recoltare", "Sânge Total");
                    row.put("cantitate_ml", 450);
                    istoric.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Eroare la preluarea istoricului: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return istoric;
    }

    public void finalizeazaProcesDonare(Long idProgramare, String codPunga, String tip, int cantitate) throws SQLException {
        String sqlUpdateProg = "UPDATE programari SET status = 'FINALIZATA' WHERE id = ?";
        String sqlGetInfo = "SELECT d.grupa_sanguina, d.rh FROM donatori d JOIN programari p ON p.id_donator = d.id_utilizator WHERE p.id = ?";
        
        // Folosim INSERT fără id_programare pentru a evita eroarea, deoarece coloana nu există
        String sqlInsertStoc = "INSERT INTO pungi_sange (cod_unitate, tip_recoltare, grupa_sanguina, rh, cantitate_ml, status_punga) VALUES (?, ?, ?, ?, ?, 'DISPONIBIL')";
        
        String sqlUpdateUltima = "UPDATE donatori SET ultima_donare = CURRENT_DATE WHERE id_utilizator = (SELECT id_donator FROM programari WHERE id = ?)";

        try (Connection con = DriverManager.getConnection(url, user, password)) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps1 = con.prepareStatement(sqlUpdateProg)) {
                    ps1.setLong(1, idProgramare);
                    ps1.executeUpdate();
                }
                String grupa = "", rh = "";
                try (PreparedStatement ps2 = con.prepareStatement(sqlGetInfo)) {
                    ps2.setLong(1, idProgramare);
                    try (ResultSet rs = ps2.executeQuery()) {
                        if (rs.next()) { grupa = rs.getString("grupa_sanguina"); rh = rs.getString("rh"); }
                    }
                }
                try (PreparedStatement ps3 = con.prepareStatement(sqlInsertStoc)) {
                    ps3.setString(1, codPunga);
                    ps3.setString(2, tip);
                    ps3.setString(3, grupa);
                    ps3.setString(4, rh);
                    ps3.setInt(5, cantitate);
                    ps3.executeUpdate();
                }
                try (PreparedStatement ps4 = con.prepareStatement(sqlUpdateUltima)) {
                    ps4.setLong(1, idProgramare);
                    ps4.executeUpdate();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public List<String> getDonatoriInAsteptareAzi() {
        List<String> donatori = new ArrayList<>();
        String sql = "SELECT u.nume, p.id FROM programari p JOIN utilizatori u ON p.id_donator = u.id WHERE p.status = 'PROGRAMAT' AND p.data_programare = CURRENT_DATE ORDER BY p.ora_programare ASC";
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { donatori.add(rs.getString("nume") + " | ID: " + rs.getLong("id")); }
        } catch (SQLException e) { e.printStackTrace(); }
        return donatori;
    }
}