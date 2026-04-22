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

    // Extrage toate centrele disponibile pentru ComboBox
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

    // Numără câte programări există deja într-un anumit slot (centru, dată, oră)
    // Ne va ajuta în Service să vedem dacă mai e loc (Capacitate - Ocupate > 0)
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

    // Salvează programarea efectivă
    public void salveazaProgramare(Long idDonator, Long idCentru, LocalDate data, LocalTime ora) {
        String sql = "INSERT INTO programari (id_donator, id_centru, data_programare, ora_programare) VALUES (?, ?, ?, ?)";
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

        // SQL-ul face JOIN între programări, centre și rezultate_analize
        // Folosim LEFT JOIN la rezultate_analize pentru că o programare poate să nu aibă încă analizele gata
        String sql = """
        SELECT 
            p.id AS id_programare, 
            p.data_programare, 
            p.ora_programare,
            c.nume AS nume_centru, 
            c.adresa AS adresa_centru,
            r.id AS id_analiza,
            r.status_analize
        FROM programari p
        JOIN centre_donare c ON p.id_centru = c.id
        LEFT JOIN rezultate_analize r ON p.id = r.id_programare
        WHERE p.id_donator = ?
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
                    row.put("ora_programare", rs.getTime("ora_programare").toLocalTime());
                    row.put("centru", rs.getString("nume_centru"));
                    row.put("adresa", rs.getString("adresa_centru"));

                    // Verificăm dacă există analize (id_analiza va fi 0 sau null dacă nu există)
                    Object idAnaliza = rs.getObject("id_analiza");
                    row.put("id_analiza", idAnaliza);
                    row.put("status_analize", rs.getString("status_analize"));

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
        // 1. Update status în programări
        String sqlUpdateProg = "UPDATE programari SET status = 'FINALIZATA' WHERE id = ?";

        // 2. Aflăm grupa și RH-ul folosind id_utilizator (cheia ta primară din tabelul donatori)
        String sqlGetInfo = "SELECT d.grupa_sanguina, d.rh FROM donatori d " +
                "JOIN programari p ON p.id_donator = d.id_utilizator " +
                "WHERE p.id = ?";

        // 3. INSERT în pungi_sange (TABELUL CORECT conform StocController)
        // Am verificat: coloanele din script sunt: cod_unitate, tip_recoltare, grupa_sanguina, rh, cantitate_ml, status_punga
        String sqlInsertStoc = "INSERT INTO pungi_sange (cod_unitate, tip_recoltare, grupa_sanguina, rh, cantitate_ml, status_punga) " +
                "VALUES (?, ?, ?, ?, ?, 'DISPONIBIL')";

        // 4. Actualizăm data ultimei donări
        String sqlUpdateUltima = "UPDATE donatori SET ultima_donare = CURRENT_DATE " +
                "WHERE id_utilizator = (SELECT id_donator FROM programari WHERE id = ?)";

        try (Connection con = DriverManager.getConnection(url, user, password)) {
            con.setAutoCommit(false); // Începem tranzacția

            try {
                // Pas 1: Închidem programarea
                try (PreparedStatement ps1 = con.prepareStatement(sqlUpdateProg)) {
                    ps1.setLong(1, idProgramare);
                    ps1.executeUpdate();
                }

                // Pas 2: Preluăm datele sângelui
                String grupa = "";
                String rh = "";
                try (PreparedStatement ps2 = con.prepareStatement(sqlGetInfo)) {
                    ps2.setLong(1, idProgramare);
                    try (ResultSet rs = ps2.executeQuery()) {
                        if (rs.next()) {
                            grupa = rs.getString("grupa_sanguina");
                            rh = rs.getString("rh");
                        } else {
                            throw new SQLException("Nu s-au putut găsi datele donatorului pentru ID programare: " + idProgramare);
                        }
                    }
                }

                // Pas 3: Adăugăm punga în stoc
                try (PreparedStatement ps3 = con.prepareStatement(sqlInsertStoc)) {
                    ps3.setString(1, codPunga);      // cod_unitate
                    ps3.setString(2, tip);           // tip_recoltare
                    ps3.setString(3, grupa);         // grupa_sanguina
                    ps3.setString(4, rh);            // rh
                    ps3.setInt(5, cantitate);        // cantitate_ml
                    ps3.executeUpdate();
                }

                // Pas 4: Actualizăm data în tabelul donatori
                try (PreparedStatement ps4 = con.prepareStatement(sqlUpdateUltima)) {
                    ps4.setLong(1, idProgramare);
                    ps4.executeUpdate();
                }

                con.commit(); // Dacă am ajuns aici, totul e salvat definitiv
                System.out.println("✅ Succes: Donare procesată complet!");

            } catch (SQLException e) {
                con.rollback(); // Dacă ceva a crăpat, nu salvăm nimic (consistență)
                System.err.println("❌ Eroare la finalizare: " + e.getMessage());
                throw e;
            }
        }
    }
    public List<String> getDonatoriInAsteptareAzi() {
        List<String> donatori = new ArrayList<>();
        // Luăm doar programările care au statusul implicit 'PROGRAMAT' și data de azi
        String sql = """
        SELECT u.nume, p.id 
        FROM programari p 
        JOIN utilizatori u ON p.id_donator = u.id 
        WHERE p.status = 'PROGRAMAT' 
        AND p.data_programare = CURRENT_DATE
        ORDER BY p.ora_programare ASC
        """;

        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // Formatăm string-ul exact cum îl așteaptă controllerul pentru split("ID: ")
                donatori.add(rs.getString("nume") + " | ID: " + rs.getLong("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return donatori;
    }
}