import { Link, useNavigate } from 'react-router';
import { useEu, useLembretes, useSituacao } from '../api/consultas';
import type { Lembrete } from '../api/tipos';
import { CartaoVacina } from '../componentes/CartaoVacina';
import { Carregando, Erro } from '../componentes/Estado';
import { IconeCheck, IconeDocumento, IconeInfo, IconeMapa, IconePessoa, IconeVacina } from '../componentes/Icones';
import { useSessao } from '../sessao/Sessao';
import { dosesDaVacina } from '../util/doses';
import { primeiroNome } from '../util/texto';

/**
 * Simulação da home do Conecta Recife (protótipo "Home - Seu Antônio"): é por aqui que a pessoa
 * chega ao módulo. O bloco "Acompanhe suas vacinações" é o nosso, personalizado pela carteira.
 */
export function HomeConecta() {
  const eu = useEu();
  const { sair } = useSessao();
  const navegar = useNavigate();

  return (
    <div className="app">
      <div className="topo-app">
        <div className="topo-marca">
          <strong>ConectaSaúde</strong>
          <span>Recife</span>
        </div>
        <button
          type="button"
          className="topo-demo"
          onClick={() => {
            sair();
            navegar('/entrar', { replace: true });
          }}
        >
          <span>Sair</span>
          <IconePessoa />
        </button>
      </div>
      <main className="conteudo">
        {eu.isPending && <Carregando />}
        {eu.isError && <Erro erro={eu.error} tentarDeNovo={() => eu.refetch()} />}
        {eu.data && (
          <>
            <section className="secao">
              <p className="saudacao">Olá, {primeiroNome(eu.data.pessoa.nome)}!</p>
            </section>
            <nav className="secao atalhos" aria-label="Serviços">
              <Link to="/" className="atalho atalho-ativo">
                <span className="atalho-icone"><IconeVacina /></span>
                Vacinação
              </Link>
              <Link to="/mapa" className="atalho">
                <span className="atalho-icone"><IconeMapa /></span>
                Onde vacinar
              </Link>
              <Link to="/historico" className="atalho">
                <span className="atalho-icone"><IconeDocumento /></span>
                Histórico
              </Link>
              <Link to="/lembretes" className="atalho">
                <span className="atalho-icone"><IconeInfo /></span>
                Lembretes
              </Link>
            </nav>
            <section className="secao pilha" aria-labelledby="titulo-acompanhe">
              <h2 id="titulo-acompanhe" className="titulo-secao">Acompanhe suas vacinações</h2>
              <Acompanhe />
            </section>
            <p className="secao detalhe nota-simulacao">
              Esta tela simula a página inicial do Conecta Recife, onde o módulo de vacinação aparece.
            </p>
          </>
        )}
      </main>
    </div>
  );
}

/** A dose mais urgente da família: o primeiro lembrete de dose (atrasada antes de disponível). */
function Acompanhe() {
  const eu = useEu();
  const lembretes = useLembretes();
  const lembrete: Lembrete | undefined = lembretes.data?.lembretes.find((l) => l.tipo === 'DOSE');
  const situacao = useSituacao(lembrete?.pessoa.id);
  const { escolherPessoa } = useSessao();

  if (lembretes.isPending) return <Carregando />;
  if (lembretes.isError) return <Erro erro={lembretes.error} tentarDeNovo={() => lembretes.refetch()} />;
  if (!lembrete) {
    return (
      <Link to="/" className="cartao cartao-link">
        <IconeCheck className="icone-cartao" />
        <div>
          <strong>Nenhuma dose atrasada ou para tomar agora</strong>
          <p>Veja a sua carteira de vacinação.</p>
        </div>
      </Link>
    );
  }
  const dose = situacao.data?.doses.find((d) => d.idDose === lembrete.idDose);
  if (!situacao.data || !dose) return <Carregando />;
  const deOutraPessoa = lembrete.pessoa.id !== eu.data?.pessoa.id;
  const outros = lembretes.data!.lembretes.filter((l) => l.tipo === 'DOSE').length - 1;
  return (
    <div className="pilha" onClickCapture={() => escolherPessoa(lembrete.pessoa.id)}>
      {deOutraPessoa && <p className="eyebrow">Carteira de {primeiroNome(lembrete.pessoa.nome)}</p>}
      <CartaoVacina idPessoa={lembrete.pessoa.id} dosesDaAba={[dose]} todasDaVacina={dosesDaVacina(situacao.data.doses, dose.vacina.id)} />
      {outros > 0 && (
        <Link to="/lembretes" className="link ver-mais">
          Mais {outros} {outros === 1 ? 'lembrete' : 'lembretes'} de vacina
        </Link>
      )}
    </div>
  );
}
