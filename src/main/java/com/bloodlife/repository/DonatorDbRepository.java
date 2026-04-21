package com.bloodlife.repository;

import com.bloodlife.domain.Donator;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DonatorDbRepository implements IDonatorRepository {
    private String url, user, password;

    public DonatorDbRepository(String url, String user, String password) {
        this.url = url; this.user = user; this.password = password;
    }

    @Override
    public void adauga(Donator d) {
        String sql = "INSERT INTO donatori (id_utilizator, grupa_sanguina, rh, greutate, data_nasterii, ultima_donare, este_eligibil) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, d.getIdUtilizator());
            ps.setString(2, d.getGrupaSanguina());
            ps.setString(3, d.getRh());
            ps.setDouble(4, d.getGreutate());
            ps.setDate(5, Date.valueOf(d.getDataNasterii()));
            ps.setDate(6, d.getUltimaDonare() != null ? Date.valueOf(d.getUltimaDonare()) : null);
            ps.setBoolean(7, d.isEsteEligibil());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public Donator cautaDupaId(Long id) {
        String sql = "SELECT * FROM donatori WHERE id_utilizator = ?";
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Donator(
                            rs.getLong("id_utilizator"),
                            rs.getString("grupa_sanguina"),
                            rs.getString("rh"),
                            rs.getDouble("greutate"),
                            rs.getDate("data_nasterii").toLocalDate(),
                            rs.getDate("ultima_donare") != null ? rs.getDate("ultima_donare").toLocalDate() : null,
                            rs.getBoolean("este_eligibil")
                    );
                }
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return null;
    }

    @Override public void sterge(Long id) {}
    @Override public void actualizeaza(Donator d) {
        String sql = "UPDATE donatori SET grupa_sanguina=?, rh=?, greutate=?, ultima_donare=?, este_eligibil=? WHERE id_utilizator=?";
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, d.getGrupaSanguina());
            ps.setString(2, d.getRh());
            ps.setDouble(3, d.getGreutate());
            ps.setDate(4, d.getUltimaDonare() != null ? Date.valueOf(d.getUltimaDonare()) : null);
            ps.setBoolean(5, d.isEsteEligibil());
            ps.setLong(6, d.getIdUtilizator());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
    @Override public List<Donator> extrageToate() { return new ArrayList<>(); }
}