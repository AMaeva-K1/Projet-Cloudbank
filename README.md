# Projet Selenium Cucumber CloudBank

Automatisation des tests fonctionnels de l'application bancaire **CloudBank** avec Selenium WebDriver, Cucumber (BDD) et TestNG, avec génération de rapports Allure.

Les scénarios sont écrits en Gherkin et pilotent un navigateur Chrome qui reproduit les actions d'un utilisateur : connexion, création d'un compte de gestion, puis gestion complète des fournisseurs (création, modification, duplication, consultation, suppression).

## Fonctionnalités testées

| Feature | Fichier | Scénarios |
|---|---|---|
| Connexion | `login.feature` | Connexion avec des identifiants valides, arrivée sur l'accueil puis déconnexion |
| Compte de gestion | `ManagementAccountForm.feature` | Connexion, puis création d'un compte de gestion (chapitre, compte, intitulé) |
| Fournisseurs | `ProviderForm.feature` | Création, modification, duplication, consultation et suppression d'un fournisseur |

Le scénario de création de fournisseur remplit le formulaire complet : nom, pays, ville, téléphone, adresse, capital, boîte postale, capacité juridique, secteur d'activité, compte comptable associé et dirigeants.

## Stack technique

- **Java 8**
- **Maven** pour la build et les dépendances
- **Selenium WebDriver 4.17** avec ChromeDriver
- **Cucumber 7.15** (Gherkin) intégré à **TestNG 7.8**
- **Allure** (`allure-cucumber7-jvm`) pour les rapports de test
- **maven-cucumber-reporting** pour un rapport HTML Cucumber complémentaire
- **Simple Java Mail** pour l'envoi du rapport par e-mail

## Architecture

Le projet suit le pattern **Page Object Model** avec `PageFactory`, et partage une seule instance du navigateur entre tous les scénarios grâce à un **Singleton**.

```
src/
├── main/java/manager/
│   └── WebDriverSingleton.java        # Instance unique du ChromeDriver
└── test/
    ├── java/
    │   ├── pages/                      # Page Objects (localisation des éléments)
    │   │   ├── LoginPage.java
    │   │   ├── BoutonsNavigation.java  # Menus, boutons communs, boîtes de dialogue
    │   │   ├── CreateCompteGestionPage.java
    │   │   └── CreationFournisseurPage.java
    │   ├── stepDefinitions/            # Implémentation des étapes Gherkin
    │   │   ├── LoginSteps.java
    │   │   ├── CreationCompteGestionSteps.java
    │   │   └── CreationFournisseurSteps.java
    │   ├── runner/
    │   │   └── TestRunner.java         # Point d'entrée Cucumber + TestNG
    │   └── utils/
    │       ├── GenerateReport.java     # Lance "allure serve" en fin de suite
    │       ├── EmailWithAttachment.java# Envoi du rapport par e-mail
    │       └── refreshProject.java     # Lance "mvn clean"
    └── resources/Features/             # Scénarios Gherkin (.feature)
```

**Déroulement d'une exécution :**

1. `@BeforeSuite` : ouverture de Chrome via le singleton et chargement de CloudBank.
2. Exécution des features déclarées dans `TestRunner` (compte de gestion puis fournisseurs).
3. `@AfterSuite` : génération et ouverture du rapport Allure.

## Prérequis

- JDK 8 ou supérieur
- Maven 3.x
- Google Chrome (Selenium Manager télécharge automatiquement le driver adapté)
- [Allure Commandline](https://allurereport.org/docs/install/) installé et présent dans le `PATH`
- Un accès réseau à l'instance CloudBank testée

## Configuration

Avant de lancer les tests, vérifie ces points :

- **URL de l'application** : définie dans `runner/TestRunner.java`

  ```java
  driver.get("http://192.168.1.201:5303/cloudbank/index.bank");
  ```

- **Identifiants de connexion** : définis dans la table `Examples` des fichiers `.feature`.
- **Jeux de données** : les noms de fournisseurs, comptes et dirigeants sont saisis en dur dans les step definitions. Modifie-les si une donnée existe déjà dans la base, sinon la création échouera.
- **Features à exécuter** : liste `features` de l'annotation `@CucumberOptions` dans `TestRunner.java`.

## Lancer les tests

```bash
git clone https://github.com/AMaeva-K1/Projet-Cloudbank.git
cd Projet-Cloudbank
mvn clean test
```

Les tests peuvent aussi être lancés depuis Eclipse ou IntelliJ en exécutant `TestRunner.java` comme test TestNG.

## Rapports

- **Allure** : les résultats bruts sont écrits dans `allure-results/`. Le rapport s'ouvre automatiquement en fin de suite. Pour l'ouvrir à la main :

  ```bash
  allure serve allure-results
  ```

- **Rapport HTML Cucumber** : généré pendant la phase `verify` dans `target/cucumber-report-html/`.

  ```bash
  mvn clean verify
  ```

## Envoi du rapport par e-mail

`utils/EmailWithAttachment` peut envoyer le rapport par e-mail. Aucun identifiant n'est stocké dans le code. Pour l'utiliser :

```bash
cp mail.properties.example mail.properties
# puis remplir mail.properties (serveur SMTP, identifiants, destinataire)
```

`mail.properties` est ignoré par Git. Chaque paramètre peut aussi être passé par variable d'environnement (`SMTP_HOST`, `SMTP_LOGIN`, `SMTP_PASSWORD`, `MAIL_TO_ADDRESS`, etc.), qui est alors prioritaire. L'envoi est activé en décommentant `EmailWithAttachment.sendMail()` dans `TestRunner`.

## Branches

| Branche | Contenu |
|---|---|
| `main` | **Version de référence** : gestion complète des fournisseurs, compte de gestion, rapports Allure, envoi du rapport par e-mail |
| `master` | Version intermédiaire : fournisseurs, compte de gestion, rapport Allure, premier envoi d'e-mail |
| `driver-manage-with-singleton` | Introduction du singleton pour partager le WebDriver |
| `version1` | Première version : connexion et création de fournisseur avec Page Factory |

## Limites connues

- `GenerateReport` et `refreshProject` utilisent `cmd /c` et ne fonctionnent donc que sous **Windows**.
- `refreshProject` et `EmailWithAttachment` contiennent des chemins locaux en dur à adapter.
- Les données de test sont codées en dur, ce qui empêche de rejouer une création sans les modifier.
- L'attente implicite est réglée à 400 secondes, ce qui peut rendre un échec très long à détecter.

## Auteurs

**Ange Maéva KOUOMO**
