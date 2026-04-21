package com.bloodlife.repository;

import com.bloodlife.domain.Utilizator;
import com.bloodlife.domain.RolUtilizator;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilizatorDbRepository implements IUtilizatorRepository {
    private String url;
    private String username;
    private String password;

    public UtilizatorDbRepository(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public void adauga(Utilizator entitate) {
        String sql = "INSERT INTO utilizatori (nume, email, parola, rol) VALUES (?, ?, ?, ?::rol_utilizator)";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, entitate.getNume());
            ps.setString(2, entitate.getEmail());
            ps.setString(3, entitate.getParola());
            ps.setString(4, entitate.getRol().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la adaugarea utilizatorului: " + e.getMessage());
        }
    }

    @Override
    public void sterge(Long id) {
        String sql = "DELETE FROM utilizatori WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la stergerea utilizatorului: " + e.getMessage());
        }
    }

    @Override
    public void actualizeaza(Utilizator entitate) {
        String sql = "UPDATE utilizatori SET nume = ?, email = ?, parola = ?, rol = ?::rol_utilizator WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, entitate.getNume());
            ps.setString(2, entitate.getEmail());
            ps.setString(3, entitate.getParola());
            ps.setString(4, entitate.getRol().name());
            ps.setLong(5, entitate.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la actualizarea utilizatorului: " + e.getMessage());
        }
    }

    @Override
    public Utilizator cautaDupaId(Long id) {
        String sql = "SELECT * FROM utilizatori WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractUtilizatorFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la cautarea utilizatorului dupa ID: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Utilizator> extrageToate() {
        List<Utilizator> utilizatori = new ArrayList<>();
        String sql = "SELECT * FROM utilizatori";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                utilizatori.add(extractUtilizatorFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la extragerea tuturor utilizatorilor: " + e.getMessage());
        }
        return utilizatori;
    }

    @Override
    public Utilizator cautaDupaEmail(String email) {
        String sql = "SELECT * FROM utilizatori WHERE email = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractUtilizatorFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la cautarea utilizatorului dupa email: " + e.getMessage());
        }
        return null;
    }

    // Metodă helper pentru a evita duplicarea codului de mapare
    private Utilizator extractUtilizatorFromResultSet(ResultSet rs) throws SQLException {
        return new Utilizator(
                rs.getLong("id"),
                rs.getString("nume"),
                rs.getString("email"),
                rs.getString("parola").trim(),
                RolUtilizator.valueOf(rs.getString("rol"))
        );
    }
}