import { useEffect, useRef, type ReactNode } from 'react';
import { useNavigate } from 'react-router';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import type { Ponto } from '../api/consultas';
import type { UnidadeNoMapa } from '../api/tipos';

const RECIFE: L.LatLngTuple = [-8.0476, -34.877];

function pino(cor: string, selecionado: boolean) {
  const t = selecionado ? 40 : 32;
  return L.divIcon({
    className: 'pino',
    html: `<span style="background:${cor};width:${t}px;height:${t}px"><svg viewBox="0 0 24 24" width="${t / 2}" height="${t / 2}" fill="none" stroke="#fff" stroke-width="2.5" stroke-linecap="round"><path d="M12 6v12M6 12h12"/></svg></span>`,
    iconSize: [t, t],
    iconAnchor: [t / 2, t],
  });
}

/**
 * Mapa (Leaflet + OpenStreetMap). A lista da tela traz as mesmas unidades, para quem não usa o mapa.
 * Com `aoSelecionar`, tocar no pino seleciona a unidade; sem, abre o detalhe dela.
 */
export function MapaUnidades({
  unidades,
  ponto,
  selecionada,
  aoSelecionar,
  className = 'mapa',
  children,
}: {
  unidades: UnidadeNoMapa[];
  ponto: Ponto | null;
  selecionada?: number | null;
  aoSelecionar?: (id: number) => void;
  className?: string;
  children?: ReactNode;
}) {
  const elemento = useRef<HTMLDivElement>(null);
  const mapa = useRef<L.Map | null>(null);
  const camada = useRef<L.LayerGroup | null>(null);
  const navegar = useNavigate();

  useEffect(() => {
    if (!elemento.current || mapa.current) return;
    const m = L.map(elemento.current, { zoomControl: false, attributionControl: true }).setView(RECIFE, 12);
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19, attribution: '© OpenStreetMap' }).addTo(m);
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
      const cor = u.disponibilidadeVacina && u.disponibilidadeVacina !== 'DISPONIVEL' ? '#98a2b3' : '#e5484d';
      const marcador = L.marker([u.latitude, u.longitude], {
        icon: pino(cor, u.id === selecionada),
        title: u.nome,
        alt: u.nome,
        zIndexOffset: u.id === selecionada ? 1000 : 0,
      });
      marcador.on('click', () => (aoSelecionar ? aoSelecionar(u.id) : navegar(`/unidades/${u.id}`)));
      marcador.addTo(c);
      pontos.push([u.latitude, u.longitude]);
    }
    if (ponto) {
      L.circleMarker([ponto.lat, ponto.lng], { radius: 8, color: '#fff', weight: 3, fillColor: '#1d6fe0', fillOpacity: 1 })
        .bindTooltip('Você está aqui')
        .addTo(c);
      pontos.push([ponto.lat, ponto.lng]);
    }
    if (pontos.length > 0) m.fitBounds(L.latLngBounds(pontos), { padding: [28, 28], maxZoom: 15 });
  }, [unidades, ponto, selecionada, aoSelecionar, navegar]);

  return (
    <div className="mapa-moldura">
      <div ref={elemento} className={className} role="region" aria-label="Mapa das unidades de saúde" />
      {children}
    </div>
  );
}
