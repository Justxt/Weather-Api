# Diseño GitOps con Argo CD

## Objetivo

Argo CD será el único controlador que modifica los recursos de Weather-Api en Minikube. GitHub Actions conservará las responsabilidades de integración continua, análisis DevSecOps, publicación en GHCR, firma Cosign y promoción del digest en Git.

## Flujo aprobado

1. Un cambio en `main` supera Maven, Checkstyle, JUnit, JaCoCo, Gitleaks, Semgrep y Checkov.
2. Se construye una única imagen, se analiza con Trivy y OWASP ZAP, se publica en GHCR y se firma con Cosign mediante GitHub OIDC.
3. El job de promoción reemplaza únicamente `images[].digest` en `k8s/base/kustomization.yaml` y crea un commit automático en `main`.
4. Argo CD observa `main`, renderiza `k8s/base` con Kustomize y sincroniza el clúster con `prune` y `selfHeal` habilitados.
5. El runner autoalojado espera que la aplicación alcance la revisión Git promovida, `Synced` y `Healthy`; después comprueba que el Deployment usa exactamente el digest firmado.
6. Se ejecutan pruebas de salud y funcionamiento y se publican evidencias como artefacto de GitHub Actions.

## Propiedad y límites

- GitHub Actions no ejecutará `kubectl set image`, `kubectl rollout restart` ni aplicará directamente `k8s/base`.
- El único `kubectl apply` permitido en entrega continua será el bootstrap declarativo de `argocd/application.yaml` y, si hace falta, la instalación reproducible del controlador.
- `argocd/application.yaml` define la fuente, destino, sincronización automática, poda y autorreparación.
- El manifiesto oficial de Argo CD se descargará desde una versión fija y se verificará con SHA-256 antes de aplicarse.

## Recuperación y evidencia

Un timeout, una revisión distinta, un estado no saludable, un digest diferente o un smoke test fallido detendrá el job. El artefacto final contendrá el estado de la Application, recursos, imagen desplegada, revisión observada, salud y respuesta funcional. La demostración de `selfHeal` modificará temporalmente el número de réplicas; Argo CD deberá restaurar el valor declarado en Git.

