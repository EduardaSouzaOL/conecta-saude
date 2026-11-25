import { ErroApi } from '../api/cliente';
import { IconeAlerta } from './Icones';

export function Carregando({ texto = 'Carregando…' }: { texto?: string }) {
  return (
    <div className="estado" role="status" aria-live="polite">
      <span className="spinner" aria-hidden="true" />
      <span>{texto}</span>
    </div>
  );
}

function mensagemDoErro(erro: unknown): string {
  if (erro instanceof ErroApi) {
    if (erro.status === 0) return 'Sem conexão com a internet. Confira a conexão e tente de novo.';
    if (erro.status === 401) return 'Sua sessão terminou. Entre de novo no Conecta Recife.';
    if (erro.status === 403) return 'Você não tem acesso aos dados desta pessoa.';
    if (erro.status === 404) return 'Não encontramos esta informação.';
  }
  return 'Não foi possível carregar agora. Tente de novo em alguns minutos.';
}

export function Erro({ erro, tentarDeNovo }: { erro: unknown; tentarDeNovo?: () => void }) {
  return (
    <div className="estado estado-erro" role="alert">
      <IconeAlerta />
      <p>{mensagemDoErro(erro)}</p>
      {tentarDeNovo && (
        <button type="button" className="botao botao-secundario" onClick={tentarDeNovo}>
          Tentar de novo
        </button>
      )}
    </div>
  );
}

export function Vazio({ children }: { children: React.ReactNode }) {
  return <div className="estado estado-vazio">{children}</div>;
}
