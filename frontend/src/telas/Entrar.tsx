import { useNavigate } from 'react-router';
import { loginsDemo } from '../demo/buscaDemo';
import { Cabecalho } from '../componentes/Layout';
import { IconePessoa, IconeSeta } from '../componentes/Icones';
import { useSessao } from '../sessao/Sessao';

/** Só na demonstração: escolhe a família sintética, no lugar do login do Conecta. */
export function Entrar() {
  const { entrar } = useSessao();
  const navegar = useNavigate();
  return (
    <>
      <Cabecalho titulo="Escolha a família" voltar />
      <section className="secao">
        <p className="intro">
          Na demonstração, escolha quem está usando o app. Todas as pessoas são fictícias (dados sintéticos).
        </p>
        <ul className="lista-cartoes">
          {loginsDemo.map((l) => (
            <li key={l.id}>
              <button
                type="button"
                className="cartao cartao-link botao-cartao"
                onClick={() => {
                  entrar(`dev-pessoa-${l.id}`);
                  navegar('/');
                }}
              >
                <IconePessoa className="icone-cartao" />
                <div>
                  <strong>{l.nome}</strong>
                </div>
                <IconeSeta />
              </button>
            </li>
          ))}
        </ul>
      </section>
    </>
  );
}
