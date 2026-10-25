# Craftaria Utils

Mod client Fabric pour Minecraft 1.21.11. Créateur : **OrsonBlockTV**.

## Installation

1. Installe Fabric Loader pour Minecraft 1.21.11 avec Fabric API.
2. Lance `./gradlew build` (ou `gradlew.bat build` sous Windows).
3. Copie `build/libs/craftaria-utils-1.4.0.jar` dans le dossier `mods`.
4. Dans Minecraft, ouvre **Options → Contrôles → Raccourcis divers** pour attribuer les touches `/four`, connexion et cuisson.

## Fonctions

- **Craftaria Utils** apparaît au-dessus de Déconnexion dans le menu Échap.
- La réparation automatique envoie `/repair` lorsque l’outil tenu a perdu au moins la moitié de sa durabilité.
- La touche `/four` envoie la commande `/four`.
- La touche de connexion affiche une confirmation avant d’envoyer `/login <mot de passe>`.
- La touche de cuisson ouvre ton inventaire : un clic marque les objets en vert sans les déplacer. Le bouton **Cuire** sous l’inventaire lance le traitement. Pour chaque objet sélectionné, le mod le place dans la main active et envoie `/four`. Le délai entre les commandes se règle avec un curseur de **0,1 à 5 secondes**, par pas de 0,1 seconde, et est sauvegardé automatiquement.

Le mot de passe est conservé localement dans `config/craftaria-utils.json` et masqué dans le champ de saisie.

La cuisson utilise une case vide de la barre rapide, remet chaque pile dans sa case d’origine, puis rétablit la case tenue avant le démarrage. Si la barre rapide est pleine, un message demande de libérer une case.

Le menu d’accueil personnalisé utilise le fond `image/fondbase.png`, un logo transparent et les boutons Se connecter (play.craftaria.fr), Menu de base Minecraft, Options et Quitter. Le menu et le fond sont conservés pendant la connexion, avec le libellé Connexion… et une action Annuler.

Le menu choisi est conservé pour la session : une déconnexion renvoie vers le menu personnalisé, sauf si le joueur a choisi le menu Minecraft classique. Celui-ci contient un bouton pour revenir au menu personnalisé.

Le panneau en haut à droite affiche les joueurs connectés et le ping, actualisés toutes les 30 secondes. Animation de recherche et icône hors ligne de Minecraft ; barres colorées selon la latence.

Les réglages utilisent un menu dédié avec quatre sections. Les raccourcis clavier et souris se modifient directement dans la section Raccourcis, avec synchronisation et sauvegarde dans les contrôles Minecraft. Échap retire une affectation ; les conflits sont signalés.
