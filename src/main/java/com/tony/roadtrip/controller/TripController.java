package com.tony.roadtrip.controller;

import com.tony.roadtrip.model.Accommodation;
import com.tony.roadtrip.model.ItineraryDay;
import com.tony.roadtrip.model.TripDocument;
import com.tony.roadtrip.repository.DocumentRepository;
import com.tony.roadtrip.repository.ItineraryRepository;
import lombok.RequiredArgsConstructor;
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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class TripController {
    private final ItineraryRepository repository;
    private final DocumentRepository documentRepository;

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
            e.printStackTrace();
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

    // --- NOUVEAU : Endpoint pour SUPPRIMER un fichier (Optionnel mais utile) ---
    @GetMapping("/deleteDoc/{id}")
    public String deleteDocument(@PathVariable Long id) {
        documentRepository.deleteById(id);
        return "redirect:/";
    }
}
