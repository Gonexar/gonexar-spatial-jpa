# Como publicar uma versão no Maven Central

Os três módulos (`gonexar-spatial-core`, `gonexar-spatial-postgis` e
`gonexar-spatial-h2gis`) são publicados sob o namespace `com.gonexar` no
[Central Portal](https://central.sonatype.com), usando o plugin
[`com.vanniktech.maven.publish`](https://vanniktech.github.io/gradle-maven-publish-plugin/).

## Secrets necessários

Configure no repositório (ou na organização) em *Settings → Secrets and variables → Actions*:

| Secret | Conteúdo |
|---|---|
| `MAVEN_CENTRAL_USERNAME` | Usuário do *user token* gerado em central.sonatype.com → *View Account* → *Generate User Token* |
| `MAVEN_CENTRAL_PASSWORD` | Senha do mesmo *user token* |
| `GPG_PRIVATE_KEY` | Chave privada GPG em ASCII armor (`gpg --armor --export-secret-keys <KEY_ID>`) |
| `GPG_PASSPHRASE` | Senha da chave GPG |

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
