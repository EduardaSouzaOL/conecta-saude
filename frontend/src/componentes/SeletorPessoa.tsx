import { usePessoaAtiva } from '../sessao/usePessoaAtiva';
import { primeiroNome } from '../util/texto';

/** Troca de caderneta entre a pessoa logada e os dependentes (RF02). */
export function SeletorPessoa() {
  const { familia, pessoa, escolherPessoa, eu } = usePessoaAtiva();
  if (familia.length < 2) return null;
  return (
    <div className="seletor-pessoa" role="radiogroup" aria-label="De quem é a caderneta">
      {familia.map((p) => {
        const ativo = p.id === pessoa?.id;
        const rotulo = p.id === eu.data?.pessoa.id ? 'Você' : primeiroNome(p.nome);
        return (
          <button
            key={p.id}
            type="button"
            role="radio"
            aria-checked={ativo}
            className={ativo ? 'chip chip-ativo' : 'chip'}
            onClick={() => escolherPessoa(p.id)}
          >
            <span className="chip-avatar" aria-hidden="true">
              {primeiroNome(p.nome).charAt(0)}
            </span>
            {rotulo}
          </button>
        );
      })}
    </div>
  );
}
