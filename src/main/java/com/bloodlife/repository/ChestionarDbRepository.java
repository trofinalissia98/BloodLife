package com.bloodlife.repository;

import com.bloodlife.domain.Intrebare;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChestionarDbRepository {
    private String url, user, password;

    public ChestionarDbRepository(String url, String user, String password) {
        this.url = url; this.user = user; this.password = password;
    }

    public List<Intrebare> extrageIntrebari() {
        List<Intrebare> intrebari = new ArrayList<>();
        String sql = "SELECT * FROM intrebari_chestionar";
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                intrebari.add(new Intrebare(
                        rs.getLong("id"),
                        rs.getString("text_intrebare"),
                        rs.getBoolean("raspuns_admisibil")
                ));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return intrebari;
    }

    public void salveazaRezultat(Long idDonator, boolean esteEligibil) {
        String sql = "INSERT INTO raspunsuri_chestionar (id_donator, este_eligibil) VALUES (?, ?)";
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idDonator);
            ps.setBoolean(2, esteEligibil);
            ps.executeUpdate();

            // Update status donator
            String updateSql = "UPDATE donatori SET este_eligibil = ? WHERE id_utilizator = ?";
            try (PreparedStatement psUpdate = con.prepareStatement(updateSql)) {
                psUpdate.setBoolean(1, esteEligibil);
                psUpdate.setLong(2, idDonator);
                psUpdate.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Eroare DB: " + e.getMessage());
            // Aruncăm o eroare personalizată ca să o prindem în Controller
            throw new RuntimeException("Nu am putut salva rezultatul. Verifică dacă utilizatorul există în tabelul donatori.");
        }
    }
}