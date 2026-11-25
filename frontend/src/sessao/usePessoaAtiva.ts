import { useEu } from '../api/consultas';
import { useSessao } from './Sessao';

/** Pessoa cuja caderneta está aberta: a escolhida ou, por padrão, a logada. */
export function usePessoaAtiva() {
  const eu = useEu();
  const { pessoaAtiva, escolherPessoa } = useSessao();
  const familia = eu.data ? [eu.data.pessoa, ...eu.data.dependentes.map((d) => d.pessoa)] : [];
  const pessoa = familia.find((p) => p.id === pessoaAtiva) ?? familia[0];
  const logada = pessoa != null && pessoa.id === eu.data?.pessoa.id;
  return { eu, familia, pessoa, logada, escolherPessoa };
}
