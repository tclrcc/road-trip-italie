package com.tony.roadtrip.config;

import com.tony.roadtrip.model.ItineraryDay;
import com.tony.roadtrip.repository.ItineraryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.Month;

@Configuration
public class DataLoader {

    @Bean CommandLineRunner initDatabase(ItineraryRepository repository) {
        return args -> {
            // Jour 1 : Départ
            ItineraryDay j1 = new ItineraryDay();
            j1.setDate(LocalDate.of(2026, Month.MAY, 16));
            j1.setTitle("Traversée Alpine vers la Ligurie");
            j1.setHubLocation("Hub 1: Levanto");
            j1.setMainActivity("Route Lagnieu -> Levanto via Col du Mont Cenis (Ouvert le 8 mai).");
            j1.setLogisticsTip("CRITIQUE: Sortir à Modane, D1006. Éviter Tunnel Fréjus (55€).");
            j1.setEstimatedCost(150.0); // Carburant + Péage
            j1.setWarningZTL(false);
            repository.save(j1);

            // Jour 2 : Cinque Terre
            ItineraryDay j2 = new ItineraryDay();
            j2.setDate(LocalDate.of(2026, Month.MAY, 17));
            j2.setTitle("Immersion Cinque Terre");
            j2.setHubLocation("Levanto");
            j2.setMainActivity("Train + Randonnée Vernazza-Monterosso. Acheter Cinque Terre Treno Card.");
            j2.setLogisticsTip("La voiture reste garée à Levanto !");
            j2.setEstimatedCost(80.0); // Carte train + repas
            j2.setWarningZTL(false);
            repository.save(j2);

            // Jour 4 : Transfert Florence (Exemple ZTL)
            ItineraryDay j4 = new ItineraryDay();
            j4.setDate(LocalDate.of(2026, Month.MAY, 19));
            j4.setTitle("Direction la Toscane via Pise");
            j4.setHubLocation("Hub 2: Scandicci");
            j4.setMainActivity("Arrêt Pise (Piazza dei Miracoli) puis route vers Florence.");
            j4.setLogisticsTip("Parking Pise: Via Pietrasantina (Gratuit). Florence: Villa Costanza IMPÉRATIF.");
            j4.setWarningZTL(true); // Alerte ZTL activée !
            repository.save(j4);

            System.out.println("Itinéraire chargé avec succès !");
        };
    }
}
