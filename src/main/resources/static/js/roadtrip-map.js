document.addEventListener("DOMContentLoaded", function() {
    const mapElement = document.getElementById('map');
    if (!mapElement) return;

    // --- 1. INITIALISATION DE LA CARTE ---
    var map = L.map('map').setView([42.5, 12.5], 6);

    L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
        attribution: '&copy; OpenStreetMap, &copy; CARTO',
        maxZoom: 19
    }).addTo(map);

    // --- 2. PALETTE DE COULEURS (Rotation) ---
    // Une liste de couleurs modernes et distinctes pour chaque tronçon
    const routeColors = [
        '#0d6efd', // Bleu (Bootstrap Primary)
        '#198754', // Vert (Success)
        '#fd7e14', // Orange
        '#6610f2', // Violet
        '#d63384', // Rose
        '#0dcaf0', // Cyan
        '#ffc107', // Jaune
        '#20c997'  // Teal
    ];

    // --- 3. RÉCUPÉRATION DES DONNÉES ---
    var cards = document.querySelectorAll('.accommodation-card');
    var waypoints = [];
    var markersGroup = L.featureGroup();

    cards.forEach(function(card) {
        var lat = parseFloat(card.getAttribute('data-lat'));
        var lng = parseFloat(card.getAttribute('data-lng'));
        var name = card.getAttribute('data-name');
        var step = parseInt(card.getAttribute('data-step'));
        var type = card.getAttribute('data-type');

        if (!isNaN(lat) && !isNaN(lng)) {
            waypoints.push({ lat: lat, lng: lng, step: step, name: name });

            // Styles Marqueurs
            var cssClass = (type === 'START' || type === 'END') ? 'marker-start' : 'marker-hub';
            var content = (type === 'START') ? '<i class="fas fa-flag"></i>' :
                (type === 'END')   ? '<i class="fas fa-flag-checkered"></i>' : step;

            var customIcon = L.divIcon({
                className: 'custom-div-icon ' + cssClass,
                html: `<div class='marker-pin'></div><span class='marker-number'>${content}</span>`,
                iconSize: [30, 42],
                iconAnchor: [15, 42],
                popupAnchor: [0, -35]
            });

            var marker = L.marker([lat, lng], { icon: customIcon })
                .bindPopup(`<b>${name}</b><br><span class="text-muted">Étape ${step}</span>`);

            marker.addTo(markersGroup);

            // Interaction Click Liste -> Carte
            card.addEventListener('click', function() {
                cards.forEach(c => c.classList.remove('border-primary', 'bg-light'));
                this.classList.add('border-primary', 'bg-light');
                map.flyTo([lat, lng], 13, { animate: true, duration: 1.5 });
                marker.openPopup();
            });
        }
    });

    markersGroup.addTo(map);

    // --- 4. ROUTING SEGMENT PAR SEGMENT ---
    waypoints.sort((a, b) => a.step - b.step);

    if (waypoints.length > 1) {
        // On boucle sur chaque paire de points (A->B, B->C, C->D...)
        for (let i = 0; i < waypoints.length - 1; i++) {
            const start = waypoints[i];
            const end = waypoints[i+1];
            const color = routeColors[i % routeColors.length]; // Rotation des couleurs

            fetchRouteSegment(start, end, color, i);
        }
    } else if (waypoints.length === 1) {
        map.setView([waypoints[0].lat, waypoints[0].lng], 10);
    }

    // Fonction dédiée pour récupérer et tracer un segment
    function fetchRouteSegment(start, end, color, index) {
        // OSRM attend "Longitude,Latitude"
        const coords = `${start.lng},${start.lat};${end.lng},${end.lat}`;
        const osrmUrl = `https://router.project-osrm.org/route/v1/driving/${coords}?overview=full&geometries=geojson`;

        fetch(osrmUrl)
            .then(res => res.json())
            .then(data => {
                if (data.code === 'Ok' && data.routes && data.routes.length > 0) {
                    const routeGeoJSON = data.routes[0].geometry;

                    // Calculs infos
                    const distKm = (data.routes[0].distance / 1000).toFixed(0);
                    const timeH = Math.floor(data.routes[0].duration / 3600);
                    const timeM = Math.round((data.routes[0].duration % 3600) / 60);
                    const timeStr = (timeH > 0 ? `${timeH}h` : '') + `${timeM}min`;

                    // Tracer la ligne colorée
                    const routeLayer = L.geoJSON(routeGeoJSON, {
                        style: {
                            color: color,
                            weight: 6,           // Un peu plus épais pour le style
                            opacity: 0.8,
                            lineCap: 'round',
                            lineJoin: 'round'
                        }
                    }).addTo(map);

                    // Tooltip au survol de la route (UX +++)
                    routeLayer.bindTooltip(
                        `<div style="text-align:center;">
                            <b>Trajet ${index + 1}</b><br>
                            ${start.name} ➝ ${end.name}<br>
                            <i class="fas fa-road"></i> ${distKm} km • <i class="fas fa-clock"></i> ${timeStr}
                         </div>`,
                        { sticky: true, className: 'route-tooltip' }
                    );

                    // Effet de survol (Highlight)
                    routeLayer.on('mouseover', function () { this.setStyle({ weight: 9, opacity: 1 }); });
                    routeLayer.on('mouseout', function () { this.setStyle({ weight: 6, opacity: 0.8 }); });

                    // Ajuster le zoom pour tout voir (à la fin du chargement du premier segment seulement, pour éviter que ça saute)
                    if (index === 0) {
                        // On ne force pas le bounds à chaque fois sinon ça clignote,
                        // on laisse l'utilisateur explorer ou on fait un fitBounds global à la fin si besoin.
                    }

                } else {
                    drawFallbackLine(start, end, color);
                }
            })
            .catch(err => {
                console.error(`Erreur segment ${index}:`, err);
                drawFallbackLine(start, end, color);
            });
    }

    function drawFallbackLine(start, end, color) {
        L.polyline([[start.lat, start.lng], [end.lat, end.lng]], {
            color: color,
            weight: 4,
            dashArray: '10, 10'
        }).addTo(map);
    }

    // --- 5. GÉOLOCALISATION ---
    var locateControl = L.Control.extend({
        options: { position: 'topright' },
        onAdd: function(map) {
            var container = L.DomUtil.create('div', 'leaflet-bar leaflet-control leaflet-control-custom');
            Object.assign(container.style, {
                backgroundColor: 'white', width: '34px', height: '34px', cursor: 'pointer',
                display: 'flex', alignItems: 'center', justifyContent: 'center'
            });
            container.innerHTML = '<i class="fas fa-crosshairs" style="font-size:18px; color:#333;"></i>';
            container.title = "Ma position";
            container.onclick = () => map.locate({setView: true, maxZoom: 12});
            return container;
        }
    });
    map.addControl(new locateControl());

    map.on('locationfound', e => {
        L.circle(e.latlng, e.accuracy / 2).addTo(map);
        L.marker(e.latlng).addTo(map).bindPopup("Vous êtes ici").openPopup();
    });

    map.on('locationerror', e => alert("Loc impossible : " + e.message));
});
