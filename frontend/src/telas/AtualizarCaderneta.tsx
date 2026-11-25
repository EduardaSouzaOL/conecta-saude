import { Link } from 'react-router';
import { Cabecalho } from '../componentes/Layout';

/** Como atualizar a caderneta (RF08): o caminho é a unidade de saúde. */
export function AtualizarCaderneta() {
  return (
    <>
      <Cabecalho titulo="Atualizar a caderneta" voltar />
      <section className="secao texto-corrido">
        <h2 className="titulo-secao">Tem dose no cartão físico que não aparece aqui?</h2>
        <ol className="passos">
          <li>Separe o seu cartão de vacina (ou o da criança) e um documento com foto.</li>
          <li>Vá a qualquer unidade de saúde com sala de vacina.</li>
          <li>Peça para transcrever as doses do cartão para o sistema.</li>
          <li>As doses podem levar até 14 dias para aparecer aqui.</li>
        </ol>
        <Link to="/mapa" className="botao botao-primario">Encontrar uma unidade perto de mim</Link>
      </section>
    </>
  );
}
