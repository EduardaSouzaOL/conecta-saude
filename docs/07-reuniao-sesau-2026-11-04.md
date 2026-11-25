# 07 · Reunião com a SESAU — 04/11/2026

**Tema:** apresentação dos protótipos de vacinação para o Conecta Recife
**Duração:** 1h25min (gravação `Gravacao_cop_1.m4a`)
**Participantes:** equipe Biobits (CESAR School) e profissionais da Secretaria de Saúde do Recife (PNI, rede de frio, coordenação de transformação digital em saúde).
Pessoas citadas: **Rodrigo** (coordenação de transformação digital, equipe de Gustavo), **Gustavo** (Saúde Digital), **Ivo** (ponto focal), Nath/Nathália, Thay, Camila, Homero, Nino, Duda.

> A transcrição completa continua no arquivo original. Este é um resumo organizado por tema.

## Em uma frase

A SESAU **aprovou a ideia do protótipo do cidadão** ("pela primeira vez eu olho para alguma coisa e digo 'hum'") e **descartou o app do ACS** e o dashboard de gestão, porque já são cobertos pelo e-SUS Território e pelo Alumiar.

## O que foi apresentado

| Protótipo | Descrição | Recepção |
|---|---|---|
| **1. Cidadão: Minha vacinação** | Doses por situação, painel-resumo, mapa de UBS próximas com a vacina | ✅ **Aprovado com ajustes.** "Trazer para o Conecta Recife o cartão vacinal sem ser o cartão vacinal" |
| **2. ACS: roteiro de visitas** | Panorama da microárea, lista priorizada, rota, registro da visita | ❌ Descartado. Gera retrabalho (duplica o e-SUS Território) e isola a vacina do processo do ACS, que visita por vários motivos (ex.: gestante). Ideia repassada ao Alumiar como "app do ACS" |
| **3. Gestão: dashboard** | Visualização dos dados gerados pelo app | ❌ "O Alumiar já deu conta" |

## Feedback sobre o protótipo do cidadão

- ✅ Linguagem clara, layout muito elogiado, e "parte de uma dor real".
- ✅ **Painel-resumo** ("três atrasadas, doze disponíveis") foi muito elogiado.
- ✅ **Mapa de salas de vacina próximas**: "a gente não pode perder essa sacada". O Conecta já tem mapa, endereço e localização do usuário; é só juntar.
- 🔧 **Retirar/repensar "não posso tomar"**: mensagem negativa; indicação e idade mínima variam por situação.
- 🔧 **Aprazamento** depende do profissional: há exceções clínicas (HPV em imunossuprimido = 3 doses; vítimas de violência sexual a partir de 2 anos, sem expor CID). O app não pode prometer precisão total.
- 🔧 **Não pode ser outro app**: tem que entrar no Conecta Recife.
- 🔧 Precisa de **manutenção contínua**: regras e notas técnicas mudam com frequência.
- ⚠️ "Estamos rivalizando com o app do Ministério": cuidado com PDF/documento oficial.

## Bases de dados (debate central)

- PEC e-SUS: mais fácil (via Emprel), mas não tem doses da média/alta complexidade e marcaria como atrasado quem já tomou.
- **RNDS**: consolida tudo. **Conclusão: usar a RNDS** para a funcionalidade do cidadão. Exige autorização do Ministério, e na Covid isso demorou.
- O município alimenta o nacional e recebe de volta via RNDS. Alternativa: "espelho" do Meu SUS Digital dentro do Conecta.
- **De-para** será "bem extenso". A SESAU pode enviar a regra de entrada.

## Transcrição de cartões: a grande descoberta

- Muita gente tomou a vacina, mas a dose **não está no sistema**. Exemplo: HPV em 2026 com 92% (meninas) e 88% (meninos) no registro local, mas só **54% no Alumiar**. "Falta vacinar 50% da população que já foi vacinada; só falta botar a informação no sistema." O Ministério só paga (cofinanciamento) o que é registrado.
  > ⚠️ Esses percentuais são **fala da reunião**, não dado validado. A auditoria de 24/11/2026 não achou fonte publicada para "92%/88% × 54%"; o Recife publicou 81,82% (meninas) e 68,74% (meninos) até jul/2025. Ver [13](13-indicadores-validados.md).
- Quem pode transcrever: **médico, enfermeiro ou técnico**. Basta foto/cartão, com vacina, dose, data e lote. Na prática, há **resistência** dos profissionais ("transcrição não é aqui não").
- Uma tentativa com o Google (leitura de imagem) travou porque exigiria o CBO do profissional.
- Depois de tomar ou transcrever, a dose pode levar cerca de **14 dias** para aparecer no Meu SUS Digital.
- **Ideia aprovada:** aviso no app ("Tem dose no cartão físico que não aparece aqui? Procure a unidade mais próxima") + mapa + possível notificação pelo WhatsApp. "Iria facilitar muito."
- Ideia levantada: registrar quando a unidade recusa a transcrição e mostrar o telefone da ouvidoria. A SESAU achou válido, mas pediu cuidado para não colocar "o usuário como inimigo do profissional".

## Estoque e mapa

- SIES só enxerga estoque até o distrito; nas salas, não há tempo real.
- Atenção básica não debita a dose aplicada no estoque; média/alta complexidade sim.
- Muitos motivos de perda (validade, quebra, descongelamento, corpo estranho, transferência); monodose × multidose.
- Distribuição **mensal**, conforme a cota do Ministério. Ex.: com 30% da cota, ficam cerca de 30 salas de referência em vez de 170. BCG passou 5 meses só em salas de referência.
- O Conecta Medicamentos chegou a mostrar estoque, mas tirou por problemas.
- **Recomendação:** começar mostrando as unidades próximas ("pode tomar em qualquer ponto"); depois, uma **funcionalidade para a Prefeitura informar mensalmente** quais unidades têm cada vacina, separando "toda a rede" das "salas de referência".

## WhatsApp

Já existe envio de mensagem de vacinação pelo WhatsApp, **só para crianças**. Expandir é viável e "muito mais acessível".

## Processo e encaminhamentos

- O projeto é acadêmico (disciplina do Ivo): ideação/prototipação → feedback → **implementação em ambiente estudantil** (app mobile, feito pela Duda). A eventual adoção real depende de contratação pela Prefeitura depois.
- Prazo de cerca de 2 meses. A SESAU alertou que a autorização da RNDS pode não sair a tempo, então definir a base (PEC × RNDS) e mostrar à Emprel.
- **Canal oficial de contato: via Ivo** (ponto focal), com a Camila repassando internamente.
- A SESAU pediu um **protótipo navegável fácil de compartilhar** (link do Figma / QR Code) para mostrar a Nino, Homero e Gustavo. O QR Code falhou na reunião, e a equipe ficou de enviar.
- Equipe se ofereceu para mostrar a evolução (ex.: semanalmente). A SESAU pediu que, quando houver algo mais elaborado, marquem nova reunião.
- **Convite:** mesa técnica do COP no **Rec'n'Play** com a consultoria Áquila (observatório de dados da cidade), à tarde das 15h às 16h30, dia a confirmar. Convite virá pelo WhatsApp/Ivo.

## Citações-chave

> "Esses sistemas não se comunicam. Esse é o grande problema para a gente."

> "Pela primeira vez eu olho para alguma coisa e eu digo 'hum'."

> "A ideia [do primeiro] eu gostei mais, no sentido de a gente poder trazer para o Conecta Recife o cartão vacinal sem ser o cartão vacinal."

> "Se já é uma ferramenta para o cidadão entender se ele está em atraso […] a gente não pode considerar só a base do PEC […] a gente tem que usar a Rede Nacional de Dados em Saúde."

> "O aprazamento depende da avaliação do profissional."

> "Ao Recife está faltando vacinar 50% da população que já foi vacinada; só falta botar a informação dentro do sistema."
