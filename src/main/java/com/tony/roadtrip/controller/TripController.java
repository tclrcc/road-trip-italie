package com.tony.roadtrip.controller;

import com.tony.roadtrip.dto.WeatherInfo;
import com.tony.roadtrip.model.Accommodation;
import com.tony.roadtrip.model.ItineraryDay;
import com.tony.roadtrip.model.TripDocument;
import com.tony.roadtrip.repository.AccommodationRepository;
import com.tony.roadtrip.repository.DocumentRepository;
import com.tony.roadtrip.repository.ItineraryRepository;
import com.tony.roadtrip.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class TripController {
    private final ItineraryRepository repository;
    private final DocumentRepository documentRepository;
    private final AccommodationRepository accommodationRepository;
    private final WeatherService weatherService;

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

            // 4. Calcul du Budget Intelligent
            // A. Budget Quotidien (Activités + Essence + Bouffe)
            double dailyCosts = trip.stream()
                    .mapToDouble(day -> day.getDailyBudget() != null ? day.getDailyBudget() : 0)
                    .sum();

            List<Accommodation> allAccommodations = accommodationRepository.findAll();
            model.addAttribute("accommodations", allAccommodations);

            // B. Budget Logement (Somme des coûts des hubs uniques)
            double accommodationCosts = allAccommodations.stream()
                    .mapToDouble(acc -> acc.getCost() != null ? acc.getCost() : 0)
                    .sum();

            // C. Budget Payé vs À Payer
            double paidAmount = allAccommodations.stream()
                    .filter(Accommodation::isPaid)
                    .mapToDouble(acc -> acc.getCost() != null ? acc.getCost() : 0)
                    .sum();

            double totalEstimated = dailyCosts + accommodationCosts;

            model.addAttribute("totalBudget", totalEstimated);
            model.addAttribute("paidAmount", paidAmount);
            model.addAttribute("remainingToPay", totalEstimated - paidAmount);

            // --- NOUVEAU : CHARGEMENT MÉTÉO ---
            Map<Long, WeatherInfo> weatherMap = new HashMap<>();

            for (ItineraryDay day : trip) {
                // On utilise les coords du logement si dispo, sinon coords par défaut (Rome par ex) ou on skip
                Double lat = 41.9028;
                Double lon = 12.4964;

                if (day.getAccommodation() != null) {
                    lat = day.getAccommodation().getLatitude();
                    lon = day.getAccommodation().getLongitude();
                } else {
                    // TODO: Ajouter lat/lon dans ItineraryDay pour les jours sans logement fixe
                    // Pour l'instant on utilise une lat fix pour la démo si null
                }

                if(lat != null && lon != null) {
                    WeatherInfo info = weatherService.getWeatherForDate(lat, lon, day.getDate());
                    weatherMap.put(day.getId(), info);
                }
            }
            model.addAttribute("weatherMap", weatherMap);
        }

        List<TripDocument> docs = documentRepository.findAll();
        model.addAttribute("documents", docs);

        return "index";
    }

    // --- NOUVEAU : Endpoint pour UPLOADER un fichier ---
    @PostMapping("/uploadDoc")
    public String uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            if (!file.isEmpty()) {
                TripDocument doc = new TripDocument();
                doc.setName(file.getOriginalFilename());
                doc.setType(file.getContentType());
                doc.setContent(file.getBytes());
                documentRepository.save(doc);
            }
        } catch (IOException e) {
            log.error("Erreur lors upload du fichier : {}", e.getMessage());
        }
        return "redirect:/";
    }

    // --- NOUVEAU : Endpoint pour TÉLÉCHARGER/VOIR un fichier ---
    @GetMapping("/document/{id}")
    public ResponseEntity<ByteArrayResource> downloadDocument(@PathVariable Long id) {
        TripDocument doc = documentRepository.findById(id).orElse(null);
        if (doc == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getName() + "\"")
                .body(new ByteArrayResource(doc.getContent()));
    }

    // --- NOUVEAU : Endpoint pour SUPPRIMER un fichier (Optionnel, mais utile) ---
    @GetMapping("/deleteDoc/{id}")
    public String deleteDocument(@PathVariable Long id) {
        documentRepository.deleteById(id);
        return "redirect:/";
    }
}
