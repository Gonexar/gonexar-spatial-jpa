# Como publicar uma versão no Maven Central

Os três módulos (`gonexar-spatial-core`, `gonexar-spatial-postgis` e
`gonexar-spatial-h2gis`) são publicados sob o namespace `com.gonexar` no
[Central Portal](https://central.sonatype.com), usando o plugin
[`com.vanniktech.maven.publish`](https://vanniktech.github.io/gradle-maven-publish-plugin/).

## Secrets necessários

São os mesmos do `gonexar-android-sdk`. O ideal é defini-los no nível da
organização (*Organization settings → Secrets and variables → Actions*), para
servirem aos dois repositórios; também funcionam como secrets do repositório.

| Secret | Conteúdo |
|---|---|
| `CENTRAL_TOKEN` | `base64("usuário:senha")` do *user token* gerado em central.sonatype.com → *View Account* → *Generate User Token* (ex.: `printf 'usuario:senha' \| base64`) |
| `SIGNING_KEY` | Chave privada GPG em ASCII armor (`gpg --armor --export-secret-keys <KEY_ID>`) |
| `SIGNING_PASSWORD` | Senha da chave GPG |

O workflow decodifica o `CENTRAL_TOKEN` e repassa usuário e senha ao plugin.

A chave pública precisa estar em um keyserver aceito pelo Central
(ex.: `gpg --keyserver keyserver.ubuntu.com --send-keys <KEY_ID>`).

## Passo a passo

1. Ajuste `version` em `build.gradle.kts` e registre a versão no `CHANGELOG.md`.
2. Faça o merge da branch de release em `master`.
3. Crie e envie a tag com o mesmo número da versão:
   ```bash
   git tag v0.3.0-alpha
   git push origin v0.3.0-alpha
   ```
4. O workflow **Publish to Maven Central** confere se a tag bate com a versão,
   roda o build, assina e envia o bundle para o Central Portal.
5. Em <https://central.sonatype.com/publishing/deployments>, confira o deployment
   (fica em *Validated*) e clique em **Publish**. Depois disso a versão é
   imutável: não dá para sobrescrever nem apagar.

## Publicar a partir da máquina local

Coloque as credenciais em `~/.gradle/gradle.properties` (nunca no repositório):

```properties
mavenCentralUsername=...
mavenCentralPassword=...
signingInMemoryKey=-----BEGIN PGP PRIVATE KEY BLOCK-----\n...
signingInMemoryKeyPassword=...
```

E rode:

```bash
./gradlew publishToMavenCentral
```

Sem `signingInMemoryKey`, os artefatos não são assinados (útil para
`publishToMavenLocal`), e o Central recusa o upload.
