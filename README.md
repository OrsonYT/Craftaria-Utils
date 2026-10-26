# Craftaria Utils

Mod client Fabric pour Minecraft 1.21.11. Créateur : **OrsonBlockTV**.

## Installation

1. Installe Fabric Loader pour Minecraft 1.21.11 avec Fabric API.
2. Lance `./gradlew build` (ou `gradlew.bat build` sous Windows).
3. Copie le dernier JAR de `build/libs` dans le dossier `mods`.
4. Dans Minecraft, ouvre **Options → Contrôles → Raccourcis divers** pour attribuer les touches `/four`, connexion et cuisson.

## Fonctions

- **Craftaria Utils** apparaît au-dessus de Déconnexion dans le menu Échap.
- La réparation automatique envoie `/repair` au seuil de durabilité restante choisi dans le menu Réparation (1 à 100 %, 50 % par défaut).
- La touche `/four` envoie la commande `/four`.
- La touche de connexion affiche une confirmation avant d’envoyer `/login <mot de passe>`.
- La touche de cuisson ouvre ton inventaire : un clic marque les objets en vert sans les déplacer. Le bouton **Cuire** sous l’inventaire lance le traitement. Pour chaque objet sélectionné, le mod le place dans la main active et envoie `/four`. Le délai entre les commandes se règle avec un curseur de **0,1 à 5 secondes**, par pas de 0,1 seconde, et est sauvegardé automatiquement.

Le mot de passe est conservé localement dans `config/craftaria-utils.json` et masqué dans le champ de saisie.

La cuisson utilise une case vide de la barre rapide, remet chaque pile dans sa case d’origine, puis rétablit la case tenue avant le démarrage. Si la barre rapide est pleine, un objet est déplacé temporairement vers une case vide de l’inventaire puis remis à sa place.

Le menu d’accueil personnalisé utilise le fond `image/fondbase.png`, un logo transparent et les boutons Se connecter (play.craftaria.fr), Menu de base Minecraft, Options et Quitter. Le menu et le fond sont conservés pendant la connexion, avec le libellé Connexion… et une action Annuler.

Le menu choisi est conservé pour la session : une déconnexion renvoie vers le menu personnalisé, sauf si le joueur a choisi le menu Minecraft classique. Celui-ci contient un bouton pour revenir au menu personnalisé.

Le panneau en haut à droite affiche les joueurs connectés et le ping, actualisés toutes les 30 secondes. Animation de recherche et icône hors ligne de Minecraft ; barres colorées selon la latence.

Les réglages utilisent un menu dédié avec quatre sections. Les raccourcis clavier et souris se modifient directement dans la section Raccourcis, avec synchronisation et sauvegarde dans les contrôles Minecraft. Échap retire une affectation ; les conflits sont signalés.

La connexion au serveur réutilise la fiche enregistrée dans `servers.dat`, y compris la préférence du pack de ressources. Si aucune fiche n’existe, une entrée de connexion directe est créée pour permettre à Minecraft de sauvegarder le choix lors de la première demande.

Un bouton Craftaria Utils sur toute la largeur de la grille est disponible dans les options Minecraft, sous les données de télémétrie et les crédits et attributions.

Le bouton des options utilise le bandeau fourni dans `BoutonGrand.png`, avec cadrage des marges et prise en charge du survol et du clavier.

L’onglet **Wallpaper** permet de choisir une image PNG ou JPG dans le dossier `Wallpaper` créé à la racine du dossier Minecraft de l’instance. Utilise **Ouvrir le dossier**, ajoute une image, puis **Actualiser** et clique sur une vignette pour la sélectionner. La galerie comporte plusieurs pages si nécessaire et se parcourt aussi avec la molette. Le choix est sauvegardé et s’applique au menu d’accueil, aux réglages, à la connexion, au chargement du terrain et à la reconfiguration. Le logo Mojang reste affiché au démarrage ; les rechargements de packs de ressources utilisent le fond choisi et le logo Craftaria.

La cuisson par lots réserve une case vide de l’inventaire, y compris lorsque la barre rapide est pleine. Une progression avec icône de four indique la pile en cours.

Les fonds inclus sont organisés sous `src/main/resources/assets/wallpaper/<catégorie>`. Chaque catégorie définit `categoryName` et `displayOrder` dans `categories.json`. Les images, éventuellement rangées dans des sous-dossiers, utilisent le `info.json` de leur dossier : `displayName`, `shortDescription`, `author` et `displayOrder` (facultatif). `info.json` fournit uniquement les informations affichées ; le fichier image est détecté dans le dossier, quel que soit son nom. Les fichiers `exemple_*.json` ne sont pas utilisés.

La catégorie `Default` conserve une seule image et apparaît en premier ; les autres catégories suivent leur `displayOrder`. Cette image est également le fond initial et le remplacement automatique lorsqu’une sélection disparaît ou devient illisible après une mise à jour. Les anciens choix inclus sont migrés par nom de fichier si possible. Les images personnelles restent disponibles dans le dossier `Wallpaper` de l’instance.

Un bouton avec un livre apparaît au-dessus de la saisie du chat. Il ouvre les raccourcis de partage `#inv`, `#item`, `#ec`, `#bal`, `#sk`, `#guild` et `#gbank`, illustrés par des icônes. Un clic insère le tag au curseur dans le brouillon ; Entrée envoie le message normalement. Échap ferme le petit menu.

Le bouton **Téléportation**, à côté du partage dans le chat, propose `/tpa`, `/tpahere`, `/rtp`, `/warp`, `/home`, `/spawn`, `/casino` et `/crate`. Les demandes vers un joueur passent par une liste avec têtes, recherche par pseudo et pages ; la liste se rafraîchit pendant son ouverture. Le brouillon du chat est conservé en revenant de la liste. Les autres actions exécutent directement leur commande.

Le bouton **Général** du chat regroupe les commandes utiles, avec `/shop` pour le shop entre joueurs, les quêtes, compétences, collections, enderchest, kits, statistiques et liens du serveur. La molette fait défiler les menus de commandes.

**Téléportation → Ajouter une téléportation rapide** ouvre la même page que **Réglages → Homes rapides**. Indique le nom d’un home déjà créé sur le serveur, choisis un item comme icône puis ajoute le raccourci. Il est sauvegardé localement et apparaît dans le menu Téléportation pour lancer `/home <nom>`. Cette page permet aussi de modifier ou retirer les raccourcis ; elle ne crée ni ne supprime les homes du serveur.
