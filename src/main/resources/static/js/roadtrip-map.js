document.addEventListener("DOMContentLoaded", function() {
    if (!document.getElementById('map')) return;

    // 1. Fond de carte "Voyager" (Plus épuré et design que l'OSM par défaut)
    var map = L.map('map').setView([44.5, 10.0], 6);
    L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>',
        subdomains: 'abcd',
        maxZoom: 19
    }).addTo(map);

    // Récupérer tous les éléments HTML ayant la classe 'accommodation-card'
    var cards = document.querySelectorAll('.accommodation-card');
    var waypoints = []; // Pour stocker les coordonnées et tracer la ligne

    cards.forEach(function(card) {
        var lat = parseFloat(card.getAttribute('data-lat'));
        var lng = parseFloat(card.getAttribute('data-lng'));
        var name = card.getAttribute('data-name');
        var step = card.getAttribute('data-step'); // "0", "1", "2"...
        var type = card.getAttribute('data-type'); // "START" ou "HUB"

        if (!isNaN(lat) && !isNaN(lng)) {
            // Ajouter aux waypoints pour la ligne, en s'assurant de l'ordre via l'index step
            // On stocke un objet pour pouvoir trier après si l'ordre HTML n'est pas bon
            waypoints.push({ lat: lat, lng: lng, step: parseInt(step) });

            // --- CRÉATION DE L'ICÔNE CSS ---
            var cssClass = (type === 'START') ? 'marker-start' : 'marker-hub';

            // Si c'est le départ, on met une icône maison/drapeau, sinon le numéro
            var content = (type === 'START') ? '<i class="fas fa-flag"></i>' : step;

            var customIcon = L.divIcon({
                className: 'custom-div-icon ' + cssClass,
                html: `<div class='marker-pin'></div><span class='marker-number'>${content}</span>`,
                iconSize: [30, 42],
                iconAnchor: [15, 42]
            });

            L.marker([lat, lng], { icon: customIcon }).addTo(map)
                .bindPopup(`<b>${name}</b>`);
        }
    });

    // 2. Tracer la ligne (Polyline)
    // On trie les points par numéro d'étape pour être sûr de l'ordre
    waypoints.sort((a, b) => a.step - b.step);

    // On extrait juste les coords [lat, lng] pour Leaflet
    var latLngs = waypoints.map(wp => [wp.lat, wp.lng]);

    if (latLngs.length > 1) {
        var polyline = L.polyline(latLngs, {
            color: '#3B82F6',   // Bleu moderne
            weight: 4,          // Épaisseur
            opacity: 0.7,
            dashArray: '10, 10' // Pointillés pour l'effet "Road Trip"
        }).addTo(map);

        // Ajuster le zoom pour tout voir
        map.fitBounds(polyline.getBounds().pad(0.1));
    }
});
