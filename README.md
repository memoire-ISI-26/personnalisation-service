# personnalisation-service

Ce microservice est responsable de la **recommandation et de la personnalisation** des services pour chaque client. Il fait le pont entre le système de microservices Spring Boot et les résultats d'analyses Big Data traitées via Apache Spark (Scala) sur Hadoop HDFS.

## ⚙️ Rôle et Fonctionnalités

- **Extraction des profils d'usages** : Permet au client de visualiser les services qu'il utilise le plus souvent (voix, internet, services à valeur ajoutée) pour lui suggérer des pass appropriés.
- **Intégration Big Data** :
  - Un traitement Spark s'exécute périodiquement sur HDFS pour agréger et nettoyer les logs d'usages.
  - Les résultats de cette agrégation sont stockés sous forme de fichiers JSON sur HDFS.
  - Une API Python (exécutée sur le port `8000`) sert d'interface d'accès à ces fichiers HDFS.
  - Le `personnalisation-service` appelle cette API Python via un client Feign (`PythonPersonalizationProxy`).
- **Optimisation des performances (Cache)** :
  - Intègre `@Cacheable` de Spring sur les profils d'usages pour éviter de solliciter l'API Python et le cluster Hadoop à chaque requête.
  - Les requêtes subséquentes pour un même client lisent directement les données depuis le cache.

---

## 🔌 Configuration et Endpoints

- **Port par défaut** : `8401`
- **Technologie** : Spring Boot, Netflix Eureka Client, Feign Client (Python API), Spring Cache
- **Lien API Python** : `http://localhost:8000` (en local) ou `http://host.docker.internal:8000` (dans le conteneur Docker).

### Endpoints exposés :

#### 1. Consulter mes usages personnalisés
* **URL** : `GET /personnalisation/usages`
* **En-tête requis** : `X-User-Phone` (automatiquement injecté par la Gateway via le token JWT).
* **Réponse (200 OK)** : Un objet JSON contenant les catégories d'usages, les modes et les services les plus consultés.

#### 2. Consulter les usages par MSISDN (Numéro)
* **URL** : `GET /personnalisation/usages/{msisdn}`
* **Règles de sécurité** :
  - Un utilisateur possédant le rôle `CLIENT` ne peut consulter que ses propres usages. S'il tente de renseigner un autre numéro dans l'URL, le service renvoie une erreur `403 Forbidden`.
  - Les administrateurs peuvent interroger n'importe quel numéro.

---

## 🔗 Architecture d'intégration Python/Spark

Le flux d'accès aux données de personnalisation est le suivant :

1. **Agrégation** : Le script Scala Spark (`MainExportApi.scala`) lit les données d'usages brutes sur HDFS (`hdfs://localhost:9000/usage/`), les joint avec les référentiels de services (`usages_maxit.csv`), les agrège par numéro de client, et exporte le résultat final en JSON sur HDFS.
2. **Exposition Python** : Une API Python lit ces fichiers JSON sur HDFS et expose une route `/api/v1/usages/{msisdn}`.
3. **Appel Feign** : Le `personnalisation-service` interroge cette route via OpenFeign pour retourner les données formatées au client.
