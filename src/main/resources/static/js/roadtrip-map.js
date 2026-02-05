document.addEventListener("DOMContentLoaded", function() {
    if (!document.getElementById('map')) return;

    var map = L.map('map').setView([44.5, 10.0], 6);

    L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
        attribution: '&copy; OpenStreetMap, &copy; CARTO',
        maxZoom: 19
    }).addTo(map);

    // Récupérer tous les éléments HTML ayant la classe 'accommodation-card'
    var cards = document.querySelectorAll('.accommodation-card');
    var waypoints = [];

    cards.forEach(function(card) {
        var lat = parseFloat(card.getAttribute('data-lat'));
        var lng = parseFloat(card.getAttribute('data-lng'));
        var name = card.getAttribute('data-name');
        var step = parseInt(card.getAttribute('data-step')); // Convertir en Entier
        var type = card.getAttribute('data-type');

        if (!isNaN(lat) && !isNaN(lng)) {
            waypoints.push({ lat: lat, lng: lng, step: step });

            // Choix du style
            var cssClass = (type === 'START') ? 'marker-start' : 'marker-hub';
            // Contenu : Drapeau pour le départ, Chiffre pour les hubs
            var content = (type === 'START') ? '<i class="fas fa-flag"></i>' : step;

            var customIcon = L.divIcon({
                className: 'custom-div-icon ' + cssClass,
                html: `<div class='marker-pin'></div><span class='marker-number'>${content}</span>`,
                iconSize: [30, 42],
                iconAnchor: [15, 42],
                popupAnchor: [0, -35]
            });

            L.marker([lat, lng], { icon: customIcon }).addTo(map)
                .bindPopup(`<b>${name}</b>`);
        }
    });

    // TRÈS IMPORTANT : Trier les points par numéro d'étape (0, 1, 2...)
    // Cela garantit que la ligne ne fait pas des zigzags bizarres
    waypoints.sort((a, b) => a.step - b.step);

    var latLngs = waypoints.map(wp => [wp.lat, wp.lng]);

    if (latLngs.length > 1) {
        var polyline = L.polyline(latLngs, {
            color: '#3B82F6',
            weight: 4,
            opacity: 0.8,
            dashArray: '10, 10',
            lineCap: 'round'
        }).addTo(map);

        map.fitBounds(polyline.getBounds().pad(0.1));
    }
});
