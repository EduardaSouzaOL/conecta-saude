import { useState } from 'react';
import { useNavigate } from 'react-router';
import { loginsDemo, type LoginDemo } from '../demo/buscaDemo';
import { useSessao } from '../sessao/Sessao';
import { idade } from '../util/datas';
import { primeiroNome } from '../util/texto';

const HOJE = '2026-11-24'; // data de referência da base sintética

function perfil(l: LoginDemo): string {
  const anos = idade(l.dataNascimento, HOJE);
  if (l.dependentes.length === 0) return anos;
  const filhos = l.dependentes.map((d) => `${primeiroNome(d.nome)} (${idade(d.dataNascimento, HOJE)})`);
  const lista = filhos.length > 1 ? `${filhos.slice(0, -1).join(', ')} e ${filhos.at(-1)}` : filhos[0];
  return `${anos} · responsável por ${lista}`;
}

/**
 * Entrada da demonstração (protótipo "Entrar Como"): no lugar do login do Conecta (CPF ou gov.br),
 * escolhe uma das pessoas fictícias da base sintética. Nenhum CPF é pedido ou mostrado.
 */
export function Entrar() {
  const { entrar } = useSessao();
  const navegar = useNavigate();
  const [escolhido, setEscolhido] = useState<number>(loginsDemo[0]!.id);

  function fazerLogin() {
    entrar(`dev-pessoa-${escolhido}`);
    navegar('/conecta', { replace: true });
  }

  return (
    <div className="app">
      <main className="conteudo entrar">
        <div className="entrar-marca" aria-hidden="true">
          <span className="entrar-logo">ConectaSaúde</span>
          <span className="entrar-cidade">Recife</span>
        </div>
        <h1>Bem-vindo(a)</h1>
        <form
          className="entrar-form"
          onSubmit={(e) => {
            e.preventDefault();
            fazerLogin();
          }}
        >
          <fieldset>
            <legend>Entrar como (demonstração)</legend>
            <ul className="personas">
              {loginsDemo.map((l) => (
                <li key={l.id}>
                  <label className={l.id === escolhido ? 'persona persona-ativa' : 'persona'}>
                    <input
                      type="radio"
                      name="persona"
                      value={l.id}
                      checked={l.id === escolhido}
                      onChange={() => setEscolhido(l.id)}
                    />
                    <span className="persona-avatar" aria-hidden="true">{primeiroNome(l.nome).charAt(0)}</span>
                    <span className="persona-texto">
                      <strong>{l.nome}</strong>
                      <span>{perfil(l)}</span>
                    </span>
                  </label>
                </li>
              ))}
            </ul>
          </fieldset>
          <button type="submit" className="botao botao-primario">Fazer Login</button>
          <p className="entrar-nota">
            Pessoas fictícias, com dados sintéticos. No app de verdade, a entrada é pelo login do Conecta Recife.
          </p>
        </form>
      </main>
    </div>
  );
}
