import { useEffect, useRef } from 'react';
import { useNavigate } from 'react-router';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import type { Ponto } from '../api/consultas';
import type { UnidadeNoMapa } from '../api/tipos';

const RECIFE: L.LatLngTuple = [-8.0476, -34.877];

function pino(cor: string) {
  return L.divIcon({
    className: 'pino',
    html: `<span style="background:${cor}"></span>`,
    iconSize: [28, 28],
    iconAnchor: [14, 28],
  });
}

/** Mapa (Leaflet + OpenStreetMap). A lista abaixo dele traz as mesmas unidades, para quem não usa o mapa. */
export function MapaUnidades({ unidades, ponto }: { unidades: UnidadeNoMapa[]; ponto: Ponto | null }) {
  const elemento = useRef<HTMLDivElement>(null);
  const mapa = useRef<L.Map | null>(null);
  const camada = useRef<L.LayerGroup | null>(null);
  const navegar = useNavigate();

  useEffect(() => {
    if (!elemento.current || mapa.current) return;
    const m = L.map(elemento.current, { zoomControl: false, attributionControl: true }).setView(RECIFE, 12);
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap',
    }).addTo(m);
    L.control.zoom({ position: 'bottomright' }).addTo(m);
    camada.current = L.layerGroup().addTo(m);
    mapa.current = m;
    return () => {
      m.remove();
      mapa.current = null;
    };
  }, []);

  useEffect(() => {
    const m = mapa.current;
    const c = camada.current;
    if (!m || !c) return;
    c.clearLayers();
    const pontos: L.LatLngTuple[] = [];
    for (const u of unidades) {
      if (u.latitude == null || u.longitude == null) continue;
      const cor = u.disponibilidadeVacina === 'DISPONIVEL' ? '#1f8a4c' : u.disponibilidadeVacina ? '#b54708' : '#d92d20';
      const marcador = L.marker([u.latitude, u.longitude], { icon: pino(cor), title: u.nome, alt: u.nome });
      marcador.bindTooltip(u.nome);
      marcador.on('click', () => navegar(`/unidades/${u.id}`));
      marcador.addTo(c);
      pontos.push([u.latitude, u.longitude]);
    }
    if (ponto) {
      L.circleMarker([ponto.lat, ponto.lng], { radius: 8, color: '#fff', weight: 3, fillColor: '#1565d8', fillOpacity: 1 })
        .bindTooltip('Você está aqui')
        .addTo(c);
      pontos.push([ponto.lat, ponto.lng]);
    }
    if (pontos.length > 0) m.fitBounds(L.latLngBounds(pontos), { padding: [24, 24], maxZoom: 15 });
  }, [unidades, ponto, navegar]);

  return <div ref={elemento} className="mapa" role="region" aria-label="Mapa das unidades de saúde" />;
}
