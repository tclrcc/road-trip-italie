package com.tony.roadtrip.controller;

import com.tony.roadtrip.model.ItineraryDay;
import com.tony.roadtrip.repository.ItineraryRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class TripController {

    private final ItineraryRepository repository;

    public TripController(ItineraryRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/")
    public String viewHomePage(Model model) {
        // 1. Récupérer tous les jours triés
        List<ItineraryDay> trip = repository.findAllByOrderByDateAsc();
        model.addAttribute("days", trip);

        if (!trip.isEmpty()) {
            // 2. Calculer le budget total estimé
            double totalCost = trip.stream()
                    .mapToDouble(day -> day.getEstimatedCost() != null ? day.getEstimatedCost() : 0)
                    .sum();
            model.addAttribute("totalBudget", totalCost);

            // 3. Récupérer les dates (Début et Fin)
            LocalDate startDate = trip.getFirst().getDate();
            LocalDate endDate = trip.getLast().getDate();
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);

            // Calculer la durée
            long duration = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            model.addAttribute("duration", duration);

            // 4. Récupérer la liste des Hubs (Villes) UNIQUES
            // On utilise un Stream pour prendre les noms, supprimer les doublons et en faire une liste
            List<String> cities = trip.stream()
                    .map(ItineraryDay::getHubLocation) // Prend seulement le nom du Hub
                    .distinct()                        // Supprime les doublons (ex: garde un seul "Rome")
                    .collect(Collectors.toList());
            model.addAttribute("cities", cities);
        }

        return "index";
    }
}
