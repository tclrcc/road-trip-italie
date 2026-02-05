document.addEventListener("DOMContentLoaded", function() {
    // Initialiser la carte centrée sur l'Italie (Coordonnées approx du centre)
    var map = L.map('map').setView([43.5, 11.0], 6);

    // Ajouter le fond de carte OpenStreetMap
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; OpenStreetMap contributors'
    }).addTo(map);

    // Récupérer tous les éléments HTML ayant la classe 'accommodation-card'
    var cards = document.querySelectorAll('.accommodation-card');
    var markers = [];

    cards.forEach(function(card) {
        // Lire les attributs de données (th:data-...)
        var lat = card.getAttribute('data-lat');
        var lng = card.getAttribute('data-lng');
        var name = card.getAttribute('data-name');

        // Si on a bien des coordonnées, on crée un marqueur
        if (lat && lng && lat !== 'null' && lng !== 'null') {
            var marker = L.marker([lat, lng]).addTo(map)
                .bindPopup("<b>" + name + "</b>");
            markers.push(marker);
        }
    });

    // Ajuster le zoom pour voir tous les points si on en a
    if (markers.length > 0) {
        var group = new L.featureGroup(markers);
        map.fitBounds(group.getBounds().pad(0.1));
    }
});
