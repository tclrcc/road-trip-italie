package com.tony.roadtrip.controller;

import com.tony.roadtrip.dto.WeatherInfo;
import com.tony.roadtrip.model.Accommodation;
import com.tony.roadtrip.model.Activity;
import com.tony.roadtrip.model.ItineraryDay;
import com.tony.roadtrip.model.TripDocument;
import com.tony.roadtrip.repository.AccommodationRepository;
import com.tony.roadtrip.repository.ActivityRepository;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class TripController {

    private final ItineraryRepository repository;
    private final DocumentRepository documentRepository;
    private final AccommodationRepository accommodationRepository;
    private final ActivityRepository activityRepository;
    private final WeatherService weatherService;

    // --- 1. DASHBOARD (Accueil) ---
    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("activePage", "home");

        List<ItineraryDay> trip = repository.findAllByOrderByDateAsc();
        if (trip.isEmpty()) return "dashboard";

        // Calculs basiques pour les widgets
        LocalDate startDate = trip.getFirst().getDate();
        long daysBeforeStart = ChronoUnit.DAYS.between(LocalDate.now(), startDate);
        model.addAttribute("daysBeforeStart", daysBeforeStart);
        model.addAttribute("duration", ChronoUnit.DAYS.between(startDate, trip.getLast().getDate()) + 1);

        // Widget Budget
        double dailyCosts = trip.stream()
                .mapToDouble(d -> d.getDailyBudget() != null ? d.getDailyBudget() : 0).sum();

        List<Accommodation> accs = accommodationRepository.findAll();
        double accCosts = accs.stream()
                .mapToDouble(a -> a.getCost() != null ? a.getCost() : 0).sum();
        double paidAcc = accs.stream()
                .filter(Accommodation::isPaid)
                .mapToDouble(a -> a.getCost() != null ? a.getCost() : 0).sum();

        // NOUVEAU : Calcul du coût total des activités
        double activityCosts = trip.stream()
                .filter(d -> d.getActivities() != null)
                .flatMap(d -> d.getActivities().stream())
                .mapToDouble(a -> a.getPrice() != null ? a.getPrice() : 0.0)
                .sum();

        // NOUVEAU : Calcul des activités déjà payées (ex: billets pris en avance sur le net)
        double paidActivities = trip.stream()
                .filter(d -> d.getActivities() != null)
                .flatMap(d -> d.getActivities().stream())
                .filter(Activity::isPaid)
                .mapToDouble(a -> a.getPrice() != null ? a.getPrice() : 0.0)
                .sum();

        // Mise à jour des totaux globaux
        double totalBudget = dailyCosts + accCosts + activityCosts;
        double totalPaid = paidAcc + paidActivities;
        double remainingToPay = totalBudget - totalPaid;

        model.addAttribute("activityCosts", activityCosts);
        model.addAttribute("paidActivities", paidActivities); // Nouvelle variable
        model.addAttribute("totalBudget", totalBudget);
        model.addAttribute("paidAmount", totalPaid); // Remplace paidAcc
        model.addAttribute("remainingToPay", remainingToPay);

        // Widget Prochaine étape (Première date future)
        ItineraryDay nextStop = trip.stream()
                .filter(d -> d.getDate().isAfter(LocalDate.now().minusDays(1)))
                .findFirst()
                .orElse(trip.getFirst());
        model.addAttribute("nextStop", nextStop);

        // Météo pour le widget nextStop
        if (nextStop.getAccommodation() != null) {
            model.addAttribute("weather", weatherService.getWeatherForDate(
                    nextStop.getAccommodation().getLatitude(),
                    nextStop.getAccommodation().getLongitude(),
                    nextStop.getDate()));
        }

        return "dashboard"; // Nouvelle vue
    }

    // --- 2. PAGE ITINÉRAIRE ---
    @GetMapping("/itinerary")
    public String itinerary(Model model) {
        model.addAttribute("activePage", "itinerary");
        List<ItineraryDay> trip = repository.findAllByOrderByDateAsc();
        model.addAttribute("days", trip);

        // Météo pour toute la timeline
        Map<Long, WeatherInfo> weatherMap = new HashMap<>();
        for (ItineraryDay day : trip) {
            if (day.getAccommodation() != null) {
                weatherMap.put(day.getId(), weatherService.getWeatherForDate(
                        day.getAccommodation().getLatitude(),
                        day.getAccommodation().getLongitude(),
                        day.getDate()));
            }
        }
        model.addAttribute("weatherMap", weatherMap);

        return "itinerary"; // Vue extraite de l'ancien index
    }

    // --- 3. PAGE CARTE ---
    @GetMapping("/map")
    public String mapPage(Model model) {
        model.addAttribute("activePage", "map");
        model.addAttribute("accommodations", accommodationRepository.findAll());
        return "map_page";
    }

    // --- 4. PAGE DOCUMENTS ---
    @GetMapping("/documents")
    public String documentsPage(Model model) {
        model.addAttribute("activePage", "docs");
        model.addAttribute("documents", documentRepository.findAll());
        return "documents";
    }

    // --- ACTIONS (Upload/Download) inchangées ---
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
            log.error("Erreur upload", e);
        }
        return "redirect:/documents"; // Redirige vers la page docs
    }

    @GetMapping("/document/{id}")
    public ResponseEntity<ByteArrayResource> downloadDocument(@PathVariable Long id) {
        TripDocument doc = documentRepository.findById(id).orElse(null);
        if (doc == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getName() + "\"")
                .body(new ByteArrayResource(doc.getContent()));
    }

    @GetMapping("/deleteDoc/{id}")
    public String deleteDoc(@PathVariable Long id) {
        documentRepository.deleteById(id);
        return "redirect:/documents";
    }

    @PostMapping("/itinerary/{dayId}/addActivity")
    public String addActivity(@PathVariable Long dayId,
            @RequestParam String name,
            @RequestParam(required = false) Double price,
            @RequestParam(required = false) String bookingUrl,
            @RequestParam(required = false) String isPaid) {

        ItineraryDay day = repository.findById(dayId).orElseThrow();

        Activity activity = new Activity();
        activity.setName(name);
        activity.setPrice(price);
        activity.setBookingUrl(bookingUrl);
        activity.setPaid("on".equals(isPaid)); // Checkbox renvoie "on" si cochée
        activity.setItineraryDay(day);

        activityRepository.save(activity);

        return "redirect:/itinerary"; // Recharge la page pour voir l'ajout
    }

    // Optionnel : Suppression d'activité
    @GetMapping("/activity/delete/{id}")
    public String deleteActivity(@PathVariable Long id) {
        activityRepository.deleteById(id);
        return "redirect:/itinerary";
    }
}
