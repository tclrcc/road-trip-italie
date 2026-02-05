package com.tony.roadtrip.controller;

import com.tony.roadtrip.model.Accommodation;
import com.tony.roadtrip.model.ItineraryDay;
import com.tony.roadtrip.repository.ItineraryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class TripController {
    private final ItineraryRepository repository;

    @GetMapping("/")
    public String viewHomePage(Model model) {
        // 1. Récupérer tous les jours triés par date
        List<ItineraryDay> trip = repository.findAllByOrderByDateAsc();
        model.addAttribute("days", trip);

        if (!trip.isEmpty()) {
            // 2. Gestion des Dates et Durée
            LocalDate startDate = trip.getFirst().getDate();
            LocalDate endDate = trip.getLast().getDate();
            long duration = ChronoUnit.DAYS.between(startDate, endDate) + 1;

            // Calcul du compte à rebours (J-XX)
            long daysBeforeStart = ChronoUnit.DAYS.between(LocalDate.now(), startDate);

            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("duration", duration);
            model.addAttribute("daysBeforeStart", daysBeforeStart);

            // 3. Gestion des Logements (Hubs) UNIQUES
            // On filtre pour ne garder que les logements distincts (pas de doublons)
            List<Accommodation> uniqueAccommodations = trip.stream()
                    .map(ItineraryDay::getAccommodation)
                    .filter(Objects::nonNull)
                    .distinct() // Grâce au @Data de Lombok, le equals() fonctionne sur l'ID
                    .collect(Collectors.toList());
            model.addAttribute("accommodations", uniqueAccommodations);

            // 4. Calcul du Budget Intelligent
            // A. Budget Quotidien (Activités + Essence + Bouffe)
            double dailyCosts = trip.stream()
                    .mapToDouble(day -> day.getDailyBudget() != null ? day.getDailyBudget() : 0)
                    .sum();

            // B. Budget Logement (Somme des coûts des hubs uniques)
            double accommodationCosts = uniqueAccommodations.stream()
                    .mapToDouble(acc -> acc.getCost() != null ? acc.getCost() : 0)
                    .sum();

            // C. Budget Payé vs À Payer
            double paidAmount = uniqueAccommodations.stream()
                    .filter(Accommodation::isPaid)
                    .mapToDouble(acc -> acc.getCost() != null ? acc.getCost() : 0)
                    .sum();

            double totalEstimated = dailyCosts + accommodationCosts;
            double remainingToPay = totalEstimated - paidAmount;

            model.addAttribute("totalBudget", totalEstimated);
            model.addAttribute("paidAmount", paidAmount);
            model.addAttribute("remainingToPay", remainingToPay);
        }

        return "index";
    }
}
