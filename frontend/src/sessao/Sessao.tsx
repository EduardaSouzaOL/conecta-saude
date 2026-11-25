import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { definirToken } from '../api/cliente';

/**
 * Estado da sessão, só em memória (RNF04: nada de dado de saúde no aparelho).
 * - token: login do Conecta (D15). Em dev e na demonstração, "dev-pessoa-<id>".
 * - pessoaAtiva: de quem a caderneta está aberta (a logada ou um dependente).
 * - lembretesVistos: ids dos lembretes já vistos (contrato: "visto" só em memória).
 */
interface Sessao {
  token: string | null;
  entrar: (token: string) => void;
  sair: () => void;
  pessoaAtiva: number | null;
  escolherPessoa: (id: number) => void;
  lembretesVistos: ReadonlySet<string>;
  marcarVistos: (ids: string[]) => void;
}

const Contexto = createContext<Sessao | null>(null);

export function ProvedorSessao({ tokenInicial, children }: { tokenInicial: string | null; children: ReactNode }) {
  const cliente = useQueryClient();
  const [token, setToken] = useState<string | null>(() => {
    definirToken(tokenInicial);
    return tokenInicial;
  });
  const [pessoaAtiva, setPessoaAtiva] = useState<number | null>(null);
  const [lembretesVistos, setVistos] = useState<ReadonlySet<string>>(new Set());

  const entrar = useCallback(
    (novo: string) => {
      definirToken(novo);
      setToken(novo);
      setPessoaAtiva(null);
      setVistos(new Set());
      cliente.clear();
    },
    [cliente],
  );

  const sair = useCallback(() => {
    definirToken(null);
    setToken(null);
    setPessoaAtiva(null);
    setVistos(new Set());
    cliente.clear();
  }, [cliente]);

  const marcarVistos = useCallback((ids: string[]) => {
    setVistos((atual) => (ids.every((i) => atual.has(i)) ? atual : new Set([...atual, ...ids])));
  }, []);

  const valor = useMemo(
    () => ({ token, entrar, sair, pessoaAtiva, escolherPessoa: setPessoaAtiva, lembretesVistos, marcarVistos }),
    [token, entrar, sair, pessoaAtiva, lembretesVistos, marcarVistos],
  );
  return <Contexto.Provider value={valor}>{children}</Contexto.Provider>;
}

export function useSessao(): Sessao {
  const s = useContext(Contexto);
  if (!s) throw new Error('useSessao fora do ProvedorSessao');
  return s;
}
