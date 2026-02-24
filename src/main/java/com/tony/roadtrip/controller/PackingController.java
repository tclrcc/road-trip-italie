package com.tony.roadtrip.controller;

import com.tony.roadtrip.model.ItemCategory;
import com.tony.roadtrip.model.PackingItem;
import com.tony.roadtrip.repository.PackingItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/packing")
@RequiredArgsConstructor
public class PackingController {
    // Repositories
    private final PackingItemRepository packingRepository;

    @GetMapping
    public String viewPackingList(Model model) {
        model.addAttribute("activePage", "packing"); // Pour allumer le menu "Valise" dans la barre de nav

        var items = packingRepository.findAllByOrderByCategoryAscNameAsc();

        // On groupe par catégorie pour l'affichage facile dans la vue
        Map<ItemCategory, java.util.List<PackingItem>> itemsByCategory = items.stream()
                .collect(Collectors.groupingBy(PackingItem::getCategory));

        model.addAttribute("itemsByCategory", itemsByCategory);
        model.addAttribute("categories", ItemCategory.values());
        model.addAttribute("newItem", new PackingItem()); // Pour le formulaire d'ajout

        // NOUVEAU : Calcul propre de la progression pour la jauge
        model.addAttribute("totalItems", items.size());
        model.addAttribute("packedItems", items.stream().filter(PackingItem::isPacked).count());

        return "packing";
    }

    // Ajout rapide d'un item
    @PostMapping("/add")
    public String addItem(@ModelAttribute PackingItem newItem) {
        newItem.setPacked(false); // Par défaut non emballé
        packingRepository.save(newItem);
        return "redirect:/packing";
    }

    // API AJAX pour cocher/décocher sans recharger la page
    @PostMapping("/toggle/{id}")
    public String toggleItem(@PathVariable Long id) {
        PackingItem item = packingRepository.findById(id).orElseThrow();
        item.setPacked(!item.isPacked()); // Inverse l'état
        packingRepository.save(item);
        return "redirect:/packing";
    }

    @GetMapping("/delete/{id}")
    public String deleteItem(@PathVariable Long id) {
        packingRepository.deleteById(id);
        return "redirect:/packing";
    }
}
