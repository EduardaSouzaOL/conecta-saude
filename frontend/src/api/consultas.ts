import { useQuery } from '@tanstack/react-query';
import { api, exigir } from './cliente';

export function useEu() {
  return useQuery({ queryKey: ['eu'], queryFn: () => exigir(api.GET('/v1/eu')) });
}

export function useLembretes() {
  return useQuery({ queryKey: ['eu', 'lembretes'], queryFn: () => exigir(api.GET('/v1/eu/lembretes')) });
}

export function useSituacao(idPessoa: number | undefined) {
  return useQuery({
    queryKey: ['situacao', idPessoa],
    enabled: idPessoa != null,
    queryFn: () => exigir(api.GET('/v1/pessoas/{idPessoa}/situacao-vacinal', { params: { path: { idPessoa: idPessoa! } } })),
  });
}

export function useRegistros(idPessoa: number | undefined) {
  return useQuery({
    queryKey: ['registros', idPessoa],
    enabled: idPessoa != null,
    queryFn: () => exigir(api.GET('/v1/pessoas/{idPessoa}/registros', { params: { path: { idPessoa: idPessoa! } } })),
  });
}

export function useDetalheDose(idPessoa: number, idDose: number) {
  return useQuery({
    queryKey: ['dose', idPessoa, idDose],
    queryFn: () =>
      exigir(api.GET('/v1/pessoas/{idPessoa}/doses/{idDose}', { params: { path: { idPessoa, idDose } } })),
  });
}

export function useCampanhas(idPessoa: number | undefined) {
  return useQuery({
    queryKey: ['campanhas', idPessoa],
    enabled: idPessoa != null,
    queryFn: () => exigir(api.GET('/v1/campanhas', { params: { query: { vigentes: true, idPessoa } } })),
  });
}

export function useVacinas() {
  return useQuery({ queryKey: ['vacinas'], staleTime: Infinity, queryFn: () => exigir(api.GET('/v1/vacinas')) });
}

export interface Ponto {
  lat: number;
  lng: number;
}

export function useUnidades(vacina: number | undefined, ponto: Ponto | null) {
  return useQuery({
    queryKey: ['unidades', vacina, ponto?.lat, ponto?.lng],
    queryFn: () =>
      exigir(api.GET('/v1/unidades', { params: { query: { vacina, lat: ponto?.lat, lng: ponto?.lng } } })),
  });
}

export function useUnidade(idUnidade: number) {
  return useQuery({
    queryKey: ['unidade', idUnidade],
    queryFn: () => exigir(api.GET('/v1/unidades/{idUnidade}', { params: { path: { idUnidade } } })),
  });
}
