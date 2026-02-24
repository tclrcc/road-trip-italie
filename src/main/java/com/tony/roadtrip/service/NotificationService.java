package com.tony.roadtrip.service;

import com.tony.roadtrip.model.PackingItem;
import com.tony.roadtrip.repository.PackingItemRepository;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;
    private final PackingItemRepository packingRepository;

    @Value("${roadtrip.emails.tony}")
    private String emailTony;

    @Value("${roadtrip.emails.copine}")
    private String emailCopine;

    private static final LocalDate DEPARTURE_DATE = LocalDate.of(2026, 5, 16);
    private static final List<Long> REMINDER_DAYS = List.of(30L, 15L, 7L, 2L);

    // S'exécute tous les jours à 09h00
    @Scheduled(cron = "0 */2 * * * ?")
    public void checkAndSendPackingReminders() {
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), DEPARTURE_DATE);

        if (REMINDER_DAYS.contains(daysLeft)) {
            log.info("J-{} avant le départ ! Envoi du récapitulatif des valises...", daysLeft);
            sendPackingEmail(daysLeft);
        }
    }

    private void sendPackingEmail(long daysLeft) {
        try {
            List<PackingItem> allItems = packingRepository.findAll();
            List<PackingItem> missingItems = allItems.stream()
                    .filter(item -> !item.isPacked()) // Suppose que tu as un booléen isPacked
                    .toList();

            long packedCount = allItems.size() - missingItems.size();
            int progress = allItems.isEmpty() ? 0 : (int) ((packedCount * 100) / allItems.size());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(new String[]{emailTony, emailCopine});
            helper.setSubject("🎒 J-" + daysLeft + " : Point sur les valises pour l'Italie !");

            // Construction du corps de l'email en HTML
            StringBuilder htmlMsg = new StringBuilder();
            htmlMsg.append("<h2 style='color:#0d6efd;'>Préparation du Road Trip en Italie</h2>");
            htmlMsg.append("<p>Salut à vous deux ! Le départ est dans <strong>").append(daysLeft).append(" jours</strong>.</p>");
            htmlMsg.append("<p>Avancement des valises : <strong>").append(progress).append("%</strong> (")
                    .append(packedCount).append("/").append(allItems.size()).append(" objets prêts).</p>");

            if (missingItems.isEmpty()) {
                htmlMsg.append("<h3 style='color:#198754;'>🎉 Tout est prêt ! Vos valises sont bouclées.</h3>");
            } else {
                htmlMsg.append("<h3 style='color:#dc3545;'>⚠️ Il reste encore ces éléments à préparer :</h3><ul>");
                for (PackingItem item : missingItems) {
                    htmlMsg.append("<li>").append(item.getName())
                            .append(" <em>(").append(item.getCategory() != null ? item.getCategory().name() : "Général").append(")</em></li>");
                }
                htmlMsg.append("</ul>");
            }

            htmlMsg.append("<br><a href='http://localhost:8080/packing' style='background-color:#0d6efd;color:white;padding:10px 20px;text-decoration:none;border-radius:5px;'>Ouvrir l'application</a>");

            helper.setText(htmlMsg.toString(), true);
            mailSender.send(message);

            log.info("Email de rappel des valises envoyé avec succès !");
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email des valises", e);
        }
    }
}
