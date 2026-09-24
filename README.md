<div align="center">

  <img src="https://nuvio.tv/assets/nuvio-app-logo-wordmark.webp" alt="NuvioAA" width="320" />

  <h1>NuvioAA — Android Auto Media & Video Streamer</h1>

  <p>
    Aplicativo de mídia e streaming de vídeo de código aberto com suporte total ao <b>Android Auto</b>.
    <br />
    Navegue por catálogos de addons (Stremio/Real-Debrid), assista a filmes e séries diretamente na tela do carro com legendas, retomada de progresso e controles de mídia.
  </p>

  [Download NuvioAA.apk](https://github.com/ginjeira/NuvioAA/releases/latest) · [Repositório GitHub](https://github.com/ginjeira/NuvioAA)

</div>

---

> [!NOTE]
> **Fork Não Oficial**: Este repositório é uma bifurcação (*fork*) não oficial do projeto original [NuvioMobile](https://github.com/NuvioMedia/NuvioMobile), desenvolvido de forma independente com o único propósito de adicionar suporte nativo ao **Android Auto (NuvioAA)**. Não possui afiliação oficial com a equipa de desenvolvimento do Nuvio.

---

## 🚗 Funcionalidades no Android Auto (NuvioAA)

- **Navegação em Catálogos de Addons**: Explore categorias, filmes, séries e coleções dos seus addons do Stremio diretamente no ecrã do carro.
- **Retoma de Progresso (*Continue Watching*)**: Retome a reprodução automaticamente do ponto exato onde parou.
- **Suporte a Legendas**: Renderização nativa de legendas no ecrã do carro com preferência para Português (`pt`).
- **Controles de Mídia & Foco de Áudio**: Integração total com a barra de mídia do sistema do carro (`MediaSession` / `Media3`) com comandos de Play, Pause e Avançar/Recuar.
- **Execução com Navegação Lado a Lado**: Compatível com o layout Coolwalk, permitindo navegação enquanto o Waze ou Google Maps roda na outra janela.

---

## ⚠️ Nota Importante sobre Autenticação

Devido a alterações no identificador do pacote (`applicationId` personalizado para `com.nuvio.auto`) e à necessidade de chaves de API / URIs de redirecionamento OAuth oficiais (Supabase, Trakt, Simkl) associadas ao projeto original, **as funcionalidades de autenticação e login direta podem não estar operacionais** nesta versão compilada de forma independente, sendo necessária orientação ou suporte da equipa oficial do Nuvio para alinhar os clientes de API e redirecionamentos externos.

---

## 📥 Obter o NuvioAA (`NuvioAA.apk`)

Faça o download da versão compilada mais recente diretamente no GitHub Releases:

👉 **[Download NuvioAA.apk no GitHub Releases](https://github.com/ginjeira/NuvioAA/releases/latest)**

---

## 📲 Guia de Instalação no Telemóvel e Carro

Para que aplicativos de terceiros como o NuvioAA sejam reconhecidos pelo Android Auto sem serem bloqueados pelo sistema, recomendamos a instalação utilizando o **KingInstaller** com permissão **Shizuku**:

### Requisitos:
1. **Shizuku** (disponível na Google Play Store para dar permissões de sistema sem root).
2. **KingInstaller** (ou AAAD / AA-Store).

### Passo a Passo:
1. Abra o **Shizuku** e inicie o serviço (via depuração sem fios ou ADB).
2. Abra o **KingInstaller** e autorize o acesso via Shizuku.
3. No KingInstaller, selecione o ficheiro **`NuvioAA.apk`** descarregado.
4. Marque a opção **"LineageOS / Overwrite package"** (se aplicável) e clique em **Install**.
5. No telemóvel, abra o app **Android Auto**:
   - Vá em **Configurações** ➔ toque 10 vezes em "Versão" para ativar as **Configurações de desenvolvedor**.
   - No menu de desenvolvedor, ative a opção **"Fontes desconhecidas"** (*Unknown sources*).
   - Defina o **"Modo do aplicativo"** (*Application mode*) para **Desenvolvedor** (*Developer*).
6. Ligue o telemóvel ao carro (ou DHU) e abra o **NuvioAuto**!

---

## 🛠️ Como Compilar a Partir do Código-Fonte

### Requisitos
- Android Studio Ladybug ou superior
- JDK 17 / JDK 21
- Android SDK com suporte para API 34+

### Passo a Passo de Compilação

1. Clone o repositório:
   ```bash
   git clone https://github.com/ginjeira/NuvioAA.git
   cd NuvioAA
   ```

2. Compile o APK usando o Gradle:
   ```bash
   ./gradlew :androidApp:assembleFullDebug
   ```

3. O ficheiro APK compilado será gerado em:
   `androidApp/build/outputs/apk/full/debug/androidApp-full-debug.apk`

---

## 👥 Créditos & Agradecimentos

Este projeto é uma adaptação comunitária (*fork*) do **[Nuvio](https://nuvio.tv)**. 
Todo o mérito da aplicação principal, arquitetura Kotlin Multiplatform, suporte a addons, scrapers e design pertence inteiramente à **[equipa oficial do Nuvio (`NuvioMedia`)](https://github.com/NuvioMedia)**. 

O único contributo deste repositório (*NuvioAA*) foi a integração e adaptação do módulo nativo para o **Android Auto**.

---

## 📄 Licença

[GNU General Public License v3.0](./LICENSE)
