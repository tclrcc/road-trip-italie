document.addEventListener("DOMContentLoaded", function() {
    const mapElement = document.getElementById('map');
    if (!mapElement) return;

    // --- 1. INITIALISATION DE LA CARTE ---
    var map = L.map('map').setView([42.5, 12.5], 6);

    L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
        attribution: '&copy; OpenStreetMap, &copy; CARTO',
        maxZoom: 19
    }).addTo(map);

    const routeColors = ['#0d6efd', '#198754', '#fd7e14', '#6610f2', '#d63384', '#0dcaf0'];

    // --- 2. RÉCUPÉRATION DES DONNÉES ET CRÉATION DES MARQUEURS ---
    var cards = document.querySelectorAll('.accommodation-card');
    var waypoints = [];
    var markersGroup = L.featureGroup();
    var latlngsForBounds = [];

    cards.forEach(function(card) {
        var lat = parseFloat(card.getAttribute('data-lat'));
        var lng = parseFloat(card.getAttribute('data-lng'));
        var name = card.getAttribute('data-name');
        var step = parseInt(card.getAttribute('data-step'));
        var type = card.getAttribute('data-type');
        var link = card.getAttribute('data-link'); // Lien airbnb

        if (!isNaN(lat) && !isNaN(lng)) {
            waypoints.push({ lat: lat, lng: lng, step: step, name: name });
            latlngsForBounds.push([lat, lng]);

            // Styles CSS des Marqueurs (récupérés depuis le HTML)
            var cssClass = (type === 'START') ? 'marker-start' : (type === 'END') ? 'marker-end' : 'marker-hub';
            var content = (type === 'START') ? '<i class="fas fa-home"></i>' :
                (type === 'END')   ? '<i class="fas fa-flag-checkered"></i>' : step;

            var customIcon = L.divIcon({
                className: 'custom-div-icon ' + cssClass,
                html: `<div class='marker-pin'></div><div class='marker-content'>${content}</div>`,
                iconSize: [34, 44],
                iconAnchor: [17, 44],
                popupAnchor: [0, -40]
            });

            // Construction de la Popup
            var btnHtml = link ? `<a href="${link}" target="_blank" class="btn btn-sm btn-outline-danger mt-2 w-100 fw-bold"><i class="fab fa-airbnb"></i> Logement</a>` : '';
            var popupContent = `<div class="text-center px-2 py-1">
                                    <h6 class="fw-bold mb-1">${name}</h6>
                                    <span class="badge bg-light text-dark border mb-2">Étape ${step}</span>
                                    ${btnHtml}
                                </div>`;

            var marker = L.marker([lat, lng], { icon: customIcon }).bindPopup(popupContent);
            marker.addTo(markersGroup);

            // Synchronisation : Clic sur la liste -> Zoom sur la carte
            card.addEventListener('click', function() {
                // Gestion du style de la carte active
                cards.forEach(c => c.classList.remove('active-hub'));
                this.classList.add('active-hub');

                // Animation de la carte
                map.flyTo([lat, lng], 14, { animate: true, duration: 1.5 });
                setTimeout(() => marker.openPopup(), 1500); // Ouvre la popup à la fin de l'animation
            });
        }
    });

    markersGroup.addTo(map);

    // --- 3. AUTO-CADRAGE DE L'ITALIE ---
    // Ajuste automatiquement le zoom pour que tous les points soient visibles
    if (latlngsForBounds.length > 1) {
        map.fitBounds(L.latLngBounds(latlngsForBounds), { padding: [50, 50] });
    } else if (latlngsForBounds.length === 1) {
        map.setView(latlngsForBounds[0], 12);
    }

    // --- 4. ROUTING SEGMENT PAR SEGMENT (Inchangé mais optimisé visuellement) ---
    waypoints.sort((a, b) => a.step - b.step);

    if (waypoints.length > 1) {
        for (let i = 0; i < waypoints.length - 1; i++) {
            fetchRouteSegment(waypoints[i], waypoints[i+1], routeColors[i % routeColors.length], i);
        }
    }

    function fetchRouteSegment(start, end, color, index) {
        const coords = `${start.lng},${start.lat};${end.lng},${end.lat}`;
        const osrmUrl = `https://router.project-osrm.org/route/v1/driving/${coords}?overview=full&geometries=geojson`;

        fetch(osrmUrl)
            .then(res => res.json())
            .then(data => {
                if (data.code === 'Ok' && data.routes.length > 0) {
                    const routeGeoJSON = data.routes[0].geometry;
                    const distKm = (data.routes[0].distance / 1000).toFixed(0);
                    const timeH = Math.floor(data.routes[0].duration / 3600);
                    const timeM = Math.round((data.routes[0].duration % 3600) / 60);
                    const timeStr = (timeH > 0 ? `${timeH}h ` : '') + `${timeM}min`;

                    const routeLayer = L.geoJSON(routeGeoJSON, {
                        style: { color: color, weight: 5, opacity: 0.8, lineCap: 'round', lineJoin: 'round' }
                    }).addTo(map);

                    routeLayer.bindTooltip(
                        `<div class="text-center">
                            <strong class="text-uppercase small">Trajet ${index + 1}</strong><br>
                            ${start.name} <i class="fas fa-arrow-right mx-1"></i> ${end.name}<br>
                            <span class="badge bg-light text-dark mt-1"><i class="fas fa-car-side"></i> ${distKm} km • <i class="fas fa-clock"></i> ${timeStr}</span>
                         </div>`,
                        { sticky: true, className: 'shadow-sm border-0 rounded p-2' }
                    );

                    routeLayer.on('mouseover', function () { this.setStyle({ weight: 8, opacity: 1 }); });
                    routeLayer.on('mouseout', function () { this.setStyle({ weight: 5, opacity: 0.8 }); });
                } else {
                    drawFallbackLine(start, end, color);
                }
            })
            .catch(() => drawFallbackLine(start, end, color));
    }

    function drawFallbackLine(start, end, color) {
        L.polyline([[start.lat, start.lng], [end.lat, end.lng]], {
            color: color, weight: 4, dashArray: '10, 10'
        }).addTo(map);
    }
});
