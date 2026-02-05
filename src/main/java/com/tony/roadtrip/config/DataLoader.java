package com.tony.roadtrip.config;

import com.tony.roadtrip.model.Accommodation;
import com.tony.roadtrip.model.ItineraryDay;
import com.tony.roadtrip.repository.AccommodationRepository;
import com.tony.roadtrip.repository.ItineraryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class DataLoader {

    @Bean CommandLineRunner initDatabase(ItineraryRepository itineraryRepository,
                                            AccommodationRepository accommodationRepository) {
        return args -> {
            // --- 1. CRÉATION DES LOGEMENTS (Tes Airbnbs) ---

            // Logement 1 - Levanto Home
            Accommodation hubLigurie = new Accommodation();
            hubLigurie.setName("Hub Ligurie : Airbnb Levanto");
            hubLigurie.setLocation("Via Guido Semenza, 15 Stanza con cucina e servizi, Levanto, Ligurie 19015, Italie");
            hubLigurie.setAirbnbLink("https://www.airbnb.fr/trips/v1/reservation-details/ro/RESERVATION2_CHECKIN/HMBJBNS9TK");
            hubLigurie.setCheckInTime("16:00");
            hubLigurie.setCheckOutTime("10:00");
            hubLigurie.setCost(364.86);
            hubLigurie.setPaid(false);
            // Enregistrement du logement
            accommodationRepository.save(hubLigurie);

            // HUB 2 : TOSCANE (Scandicci)
            Accommodation hubToscane = new Accommodation();
            hubToscane.setName("Hub Toscane : Airbnb Scandicci");
            hubToscane.setLocation("Via Pietro Fanfani, 43, Florence, Tuscany 50127");
            hubToscane.setAirbnbLink("https://www.airbnb.fr/trips/v1/reservation-details/ro/RESERVATION2_CHECKIN/HMCTQHSXQJ");
            hubToscane.setCheckInTime("11:00");
            hubToscane.setCheckOutTime("11:00");
            hubToscane.setCost(512.86);
            hubToscane.setPaid(true);
            // Enregistrement du logement
            accommodationRepository.save(hubToscane);

            // HUB 3 : ROME (Périphérie)
            Accommodation hubRome = new Accommodation();
            hubRome.setName("Hub Latium : Rome Anagnina/Cinecittà");
            hubRome.setLocation("Via Giusto Fontanini, 13, Rome, Lazio 00173");
            hubRome.setAirbnbLink("https://www.airbnb.fr/trips/v1/reservation-details/ro/RESERVATION2_CHECKIN/HMKPTW52RJ");
            hubToscane.setCheckInTime("15:00");
            hubToscane.setCheckOutTime("10:00");
            hubRome.setCost(482.43);
            hubRome.setPaid(true);
            // Enregistrement du logement
            accommodationRepository.save(hubRome);

            // HUB 4 : ÉMILIE-ROMAGNE (Modène)
            Accommodation hubEmilie = new Accommodation();
            hubEmilie.setName("Hub Emilia-Romagna : Modène");
            hubEmilie.setLocation("Via 11 Settembre, 14, Saliceto Buzzalino, Emilia-Romagna 41011");
            hubEmilie.setAirbnbLink("https://www.airbnb.fr/trips/v1/reservation-details/ro/RESERVATION2_CHECKIN/HMCPKJH2X4");
            hubToscane.setCheckInTime("16:00");
            hubToscane.setCheckOutTime("10:00");
            hubEmilie.setCost(196.46);
            hubEmilie.setPaid(false);
            // Enregistrement du logement
            accommodationRepository.save(hubEmilie);

            // HUB 5 : LACS (Côme/Menaggio)
            Accommodation hubLacs = new Accommodation();
            hubLacs.setName("Hub Lacs : Menaggio / Tremezzina");
            hubLacs.setLocation("Via Avvocato Romolo Quaglino, 6 , Tremezzo, CO 22019");
            hubLacs.setAirbnbLink("https://www.airbnb.fr/trips/v1/reservation-details/ro/RESERVATION2_CHECKIN/HMZTKPBDK4");
            hubLacs.setCost(418.65);
            hubLacs.setPaid(false);
            // Enregistrement du logement
            accommodationRepository.save(hubLacs);

            // ==========================================
            // 2. CRÉATION DE L'ITINÉRAIRE (21 Jours)
            // ==========================================

            // --- PHASE 1 : TRAVERSÉE & CINQUE TERRE ---
            createDay(itineraryRepository, LocalDate.of(2026, 5, 16),
                    "J1: Route vers la Ligurie", "Levanto",
                    "Route Lagnieu -> Levanto via Col du Mont Cenis (Ouvert le 8 mai).",
                    "CRITIQUE: Sortir Modane D1006. Éviter Tunnel Fréjus (Eco 55€).",
                    150.0, false, hubLigurie);

            createDay(itineraryRepository, LocalDate.of(2026, 5, 17),
                    "J2: Immersion Cinque Terre", "Levanto",
                    "Train + Randonnée Vernazza-Monterosso (Sentiero Azzurro).",
                    "Achat Cinque Terre Card Treno (~40€ couple). Voiture reste au parking.",
                    80.0, false, hubLigurie);

            createDay(itineraryRepository, LocalDate.of(2026, 5, 18),
                    "J3: Exploration Maritime / Détente", "Levanto",
                    "Bateau navette pour vue mer ou rando vélo vers Bonassola (Tunnels).",
                    "Dîner à Levanto (moins cher que les villages).",
                    70.0, false, hubLigurie);

            // --- PHASE 2 : TOSCANE ---
            createDay(itineraryRepository, LocalDate.of(2026, 5, 19),
                    "J4: Transfert via Pise", "Scandicci",
                    "Route vers Florence. Arrêt Pise (Piazza dei Miracoli).",
                    "Parking Pise: Via Pietrasantina (Gratuit+Navette). Florence: Villa Costanza IMPÉRATIF.",
                    60.0, true, hubToscane);

            createDay(itineraryRepository, LocalDate.of(2026, 5, 20),
                    "J5: Florence Historique", "Scandicci",
                    "Tram T1 -> Centre. Duomo, Piazza della Signoria, Ponte Vecchio.",
                    "Déjeuner Mercato Centrale (San Lorenzo). Accès Cathédrale gratuit, Coupole payante.",
                    70.0, true, hubToscane);

            createDay(itineraryRepository, LocalDate.of(2026, 5, 21),
                    "J6: Florence Musées", "Scandicci",
                    "Galerie des Offices (Uffizi) ou Accademia (David).",
                    "Réserver Offices des mois à l'avance ! Vue gratuite: Giardino delle Rose.",
                    90.0, true, hubToscane);

            createDay(itineraryRepository, LocalDate.of(2026, 5, 22),
                    "J7: Sienne & Chianti", "Scandicci",
                    "Route SR222 Chiantigiana. Arrêt Greve in Chianti + Sienne.",
                    "Sienne: Parking Santa Caterina (Hors ZTL).",
                    80.0, false, hubToscane);

            createDay(itineraryRepository, LocalDate.of(2026, 5, 23),
                    "J8: San Gimignano & Volterra", "Scandicci",
                    "Villes médiévales et tours.",
                    "Arriver à San Gimignano avant 9h30 pour éviter la foule.",
                    70.0, false, hubToscane);

            createDay(itineraryRepository, LocalDate.of(2026, 5, 24),
                    "J9: Val d'Orcia ou Repos", "Scandicci",
                    "Optionnel: Pienza/Montepulciano ou repos piscine agriturismo.",
                    "Préparation physique avant Rome.",
                    50.0, false, hubToscane);

            // --- PHASE 3 : ROME ---
            createDay(itineraryRepository, LocalDate.of(2026, 5, 25),
                    "J10: Cap sur Rome", "Rome Est",
                    "Autoroute A1 vers Rome (3h). Installation Hub Anagnina.",
                    "Ne pas entrer dans Rome centre en voiture. Utiliser Métro A.",
                    100.0, true, hubRome);

            createDay(itineraryRepository, LocalDate.of(2026, 5, 26),
                    "J11: Le Vatican", "Rome Est",
                    "Musées du Vatican + Chapelle Sixtine + Basilique St Pierre.",
                    "RESA OBLIGATOIRE 60j avant (Mars 2026). Y aller très tôt.",
                    120.0, true, hubRome);

            createDay(itineraryRepository, LocalDate.of(2026, 5, 27),
                    "J12: Rome Antique", "Rome Est",
                    "Colisée, Forum Romain, Palatin.",
                    "Billet Full Experience (Arène). Resa 30j avant.",
                    100.0, true, hubRome);

            createDay(itineraryRepository, LocalDate.of(2026, 5, 28),
                    "J13: Rome Baroque", "Rome Est",
                    "Marche: Navona, Panthéon, Trevi, Espagne.",
                    "Déjeuner Pizza al taglio. Panthéon payant le WE (Resa).",
                    70.0, true, hubRome);

            createDay(itineraryRepository, LocalDate.of(2026, 5, 29),
                    "J14: Trastevere & Aventin", "Rome Est",
                    "Jardin des Orangers, quartier Trastevere le soir.",
                    "Dîner typique: Carbonara/Cacio e Pepe.",
                    80.0, true, hubRome);

            // --- PHASE 4 : MOTEURS & GASTRONOMIE ---
            createDay(itineraryRepository, LocalDate.of(2026, 5, 30),
                    "J15: Remontée vers Modène", "Modène",
                    "Longue route A1 (4h30).",
                    "Check-in Agriturismo.",
                    120.0, false, hubEmilie);

            createDay(itineraryRepository, LocalDate.of(2026, 5, 31),
                    "J16: Ferrari & Vinaigre", "Modène",
                    "Musée Ferrari Maranello + Vinaigrerie traditionnelle.",
                    "Pass Ferrari combiné ~38€. Dégustation vinaigre souvent gratuite.",
                    100.0, false, hubEmilie);

            createDay(itineraryRepository, LocalDate.of(2026, 6, 1),
                    "J17: Bologne la Rouge", "Modène",
                    "Visite centre historique Bologne ou Parme.",
                    "ATTENTION ZTL BOLOGNE. Parking Tanari + Bus.",
                    70.0, true, hubEmilie);

            // --- PHASE 5 : LACS & RETOUR ---
            createDay(itineraryRepository, LocalDate.of(2026, 6, 2),
                    "J18: Direction Lac de Côme", "Menaggio",
                    "Férié en Italie (Festa della Repubblica). Route vers Menaggio.",
                    "Trafic dense probable. Rive Ouest mieux exposée.",
                    80.0, false, hubLacs);

            createDay(itineraryRepository, LocalDate.of(2026, 6, 3),
                    "J19: Villa Balbianello", "Menaggio",
                    "Bateau vers Lenno. Visite Villa (Star Wars).",
                    "Pass Bateau Centre Lac.",
                    90.0, false, hubLacs);

            createDay(itineraryRepository, LocalDate.of(2026, 6, 4),
                    "J20: Bellagio & Varenna", "Menaggio",
                    "La perle du lac et la promenade des amoureux.",
                    "Dernier vrai dîner italien.",
                    100.0, false, hubLacs);

            createDay(itineraryRepository, LocalDate.of(2026, 6, 5),
                    "J21: Retour en France", "Domicile",
                    "Menaggio -> Lugano -> Gothard -> Lagnieu.",
                    "Vignette Suisse 40 CHF obligatoire ou détour par Splügen.",
                    150.0, false, null); // Plus de logement le soir

            System.out.println("--- DONNÉES DE VOYAGE 2026 CHARGÉES ---");
        };
    }

    // Méthode utilitaire pour alléger le code
    private void createDay(ItineraryRepository repo, LocalDate date, String title,
            String hub, String activity, String tip,
            Double budget, boolean ztl, Accommodation accom) {
        ItineraryDay day = new ItineraryDay();
        day.setDate(date);
        day.setTitle(title);
        day.setHubLocation(hub);
        day.setMainActivity(activity);
        day.setLogisticsTip(tip);
        day.setDailyBudget(budget);
        day.setWarningZTL(ztl);
        day.setAccommodation(accom);
        repo.save(day);
    }
}
