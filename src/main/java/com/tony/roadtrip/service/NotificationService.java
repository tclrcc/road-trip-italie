package com.tony.roadtrip.service;

import com.tony.roadtrip.repository.ItineraryRepository;
import com.tony.roadtrip.repository.PackingItemRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender emailSender;
    private final PackingItemRepository packingRepo;
    private final ItineraryRepository itineraryRepo;

    @Value("${spring.mail.username}")
    private String senderEmail;

    // Vérifie tous les matins à 08h00
    @Scheduled(cron = "0 0 8 * * *")
    public void checkAndSendReminders() {
        // 1. Récupérer la date de départ (J1)
        var firstDayOpt = itineraryRepo.findAllByOrderByDateAsc().stream().findFirst();

        if (firstDayOpt.isPresent()) {
            LocalDate startTrip = firstDayOpt.get().getDate();
            long daysBefore = ChronoUnit.DAYS.between(LocalDate.now(), startTrip);

            // Envoi à J-14, J-7 et J-1
            if (daysBefore == 14 || daysBefore == 7 || daysBefore == 1) {
                long missingEssentials = packingRepo.countByIsPackedFalseAndIsEssentialTrue();

                if (missingEssentials > 0) {
                    sendEmail(daysBefore, missingEssentials);
                }
            }
        }
    }

    private void sendEmail(long daysLeft, long missingCount) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo("tonycoloricchio01@gmail.com"); // À configurer ou récupérer du profil user
            helper.setSubject("🇮🇹 RoadTrip J-" + daysLeft + " : Alerte Valise !");

            String htmlContent = "<h3>Ciao ! 👋</h3>"
                    + "<p>Le départ approche, c'est dans <strong>" + daysLeft + " jours</strong>.</p>"
                    + "<p style='color:red; font-weight:bold;'>⚠️ Attention, il te manque encore " + missingCount + " objets essentiels !</p>"
                    + "<p>Pense à vérifier ta liste sur l'application.</p>"
                    + "<br/><p><em>Buon viaggio !</em> 🍕</p>";

            helper.setText(htmlContent, true);
            emailSender.send(message);
            log.info("Mail de rappel envoyé !");

        } catch (MessagingException e) {
            log.error("Erreur envoi mail", e);
        }
    }
}
