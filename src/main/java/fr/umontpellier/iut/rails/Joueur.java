package fr.umontpellier.iut.rails;

import java.util.*;
import java.util.stream.Collectors;

public class Joueur {

    /**
     * Les couleurs possibles pour les joueurs (pour l'interface graphique)
     */
    public static enum Couleur {
        JAUNE, ROUGE, BLEU, VERT, ROSE;
    }

    /**
     * Jeu auquel le joueur est rattaché
     */
    private Jeu jeu;
    /**
     * Nom du joueur
     */
    private String nom;
    /**
     * CouleurWagon du joueur (pour représentation sur le plateau)
     */
    private Couleur couleur;
    /**
     * Nombre de gares que le joueur peut encore poser sur le plateau
     */
    private int nbGares;
    /**
     * Nombre de wagons que le joueur peut encore poser sur le plateau
     */
    private int nbWagons;
    /**
     * Liste des missions à réaliser pendant la partie
     */
    private List<Destination> destinations;
    /**
     * Liste des cartes que le joueur a en main
     */
    private List<CouleurWagon> cartesWagon;
    /**
     * Liste temporaire de cartes wagon que le joueur est en train de jouer pour
     * payer la capture d'une route ou la construction d'une gare
     */
    private List<CouleurWagon> cartesWagonPosees;
    /**
     * Score courant du joueur (somme des valeurs des routes capturées)
     */
    private int score;

    public Joueur(String nom, Jeu jeu, Joueur.Couleur couleur) {
        this.nom = nom;
        this.jeu = jeu;
        this.couleur = couleur;
        nbGares = 3;
        nbWagons = 45;
        cartesWagon = new ArrayList<>();
        cartesWagonPosees = new ArrayList<>();
        destinations = new ArrayList<>();
        score = 12; // chaque gare non utilisée vaut 4 points
    }

    public String getNom() {
        return nom;
    }

    public Couleur getCouleur() {
        return couleur;
    }

    public int getNbWagons() {
        return nbWagons;
    }

    public Jeu getJeu() {
        return jeu;
    }

    public List<CouleurWagon> getCartesWagonPosees() {
        return cartesWagonPosees;
    }

    public List<CouleurWagon> getCartesWagon() {
        return cartesWagon;
    }

    public List<Destination> getDestinations() {
        return destinations;
    }

    public int getScore() {
        return score;
    }

    public int getNbGares() {
        return nbGares;
    }

    public void addScore(int score) {
        this.score += score;
    }

    /**
     * Attend une entrée de la part du joueur (au clavier ou sur la websocket) et
     * renvoie le choix du joueur.
     * <p>
     * Cette méthode lit les entrées du jeu ({@code Jeu.lireligne()}) jusqu'à ce
     * qu'un choix valide (un élément de {@code choix} ou de {@code boutons} ou
     * éventuellement la chaîne vide si l'utilisateur est autorisé à passer) soit
     * reçu.
     * Lorsqu'un choix valide est obtenu, il est renvoyé par la fonction.
     * <p>
     * Si l'ensemble des choix valides ({@code choix} + {@code boutons}) ne comporte
     * qu'un seul élément et que {@code canPass} est faux, l'unique choix valide est
     * automatiquement renvoyé sans lire l'entrée de l'utilisateur.
     * <p>
     * Si l'ensemble des choix est vide, la chaîne vide ("") est automatiquement
     * renvoyée par la méthode (indépendamment de la valeur de {@code canPass}).
     * <p>
     * Exemple d'utilisation pour demander à un joueur de répondre à une question
     * par "oui" ou "non" :
     * <p>
     * {@code
     * List<String> choix = Arrays.asList("Oui", "Non");
     * String input = choisir("Voulez vous faire ceci ?", choix, new ArrayList<>(), false);
     * }
     * <p>
     * <p>
     * Si par contre on voulait proposer les réponses à l'aide de boutons, on
     * pourrait utiliser :
     * <p>
     * {@code
     * List<String> boutons = Arrays.asList("1", "2", "3");
     * String input = choisir("Choisissez un nombre.", new ArrayList<>(), boutons, false);
     * }
     *
     * @param instruction message à afficher à l'écran pour indiquer au joueur la
     *                    nature du choix qui est attendu
     * @param choix       une collection de chaînes de caractères correspondant aux
     *                    choix valides attendus du joueur
     * @param boutons     une collection de chaînes de caractères correspondant aux
     *                    choix valides attendus du joueur qui doivent être
     *                    représentés par des boutons sur l'interface graphique.
     * @param peutPasser  booléen indiquant si le joueur a le droit de passer sans
     *                    faire de choix. S'il est autorisé à passer, c'est la
     *                    chaîne de caractères vide ("") qui signifie qu'il désire
     *                    passer.
     * @return le choix de l'utilisateur (un élément de {@code choix}, ou de
     * {@code boutons} ou la chaîne vide)
     */
    public String choisir(String instruction, Collection<String> choix, Collection<String> boutons,
                          boolean peutPasser) {
        // on retire les doublons de la liste des choix
        HashSet<String> choixDistincts = new HashSet<>();
        choixDistincts.addAll(choix);
        choixDistincts.addAll(boutons);

        // Aucun choix disponible
        if (choixDistincts.isEmpty()) {
            return "";
        } else {
            // Un seul choix possible (renvoyer cet unique élément)
            if (choixDistincts.size() == 1 && !peutPasser)
                return choixDistincts.iterator().next();
            else {
                String entree;
                // Lit l'entrée de l'utilisateur jusqu'à obtenir un choix valide
                while (true) {
                    jeu.prompt(instruction, boutons, peutPasser);
                    entree = jeu.lireLigne();
                    // si une réponse valide est obtenue, elle est renvoyée
                    if (choixDistincts.contains(entree) || (peutPasser && entree.equals("")))
                        return entree;
                }
            }
        }
    }


    /**
     * Affiche un message dans le log du jeu (visible sur l'interface graphique)
     *
     * @param message le message à afficher (peut contenir des balises html pour la
     *                mise en forme)
     */
    public void log(String message) {
        jeu.log(message);
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("\n");
        joiner.add(String.format("=== %s (%d pts) ===", nom, score));
        joiner.add(String.format("  Gares: %d, Wagons: %d", nbGares, nbWagons));
        joiner.add("  Destinations: "
                + destinations.stream().map(Destination::toString).collect(Collectors.joining(", ")));
        joiner.add("  Cartes wagon: " + CouleurWagon.listToString(cartesWagon));
        return joiner.toString();
    }

    /**
     * @return une chaîne de caractères contenant le nom du joueur, avec des balises
     * HTML pour être mis en forme dans le log
     */
    public String toLog() {
        return String.format("<span class=\"joueur\">%s</span>", nom);
    }

    /**
     * Renvoie une représentation du joueur sous la forme d'un objet Java simple
     * (POJO)
     */
    public Object asPOJO() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("nom", nom);
        data.put("couleur", couleur);
        data.put("score", score);
        data.put("nbGares", nbGares);
        data.put("nbWagons", nbWagons);
        data.put("estJoueurCourant", this == jeu.getJoueurCourant());
        data.put("destinations", destinations.stream().map(Destination::asPOJO).collect(Collectors.toList()));
        data.put("cartesWagon", cartesWagon.stream().sorted().map(CouleurWagon::name).collect(Collectors.toList()));
        data.put("cartesWagonPosees",
                cartesWagonPosees.stream().sorted().map(CouleurWagon::name).collect(Collectors.toList()));
        return data;
    }

    /**
     * Propose une liste de cartes destinations, parmi lesquelles le joueur doit en
     * garder un nombre minimum n.
     * <p>
     * Tant que le nombre de destinations proposées est strictement supérieur à n,
     * le joueur peut choisir une des destinations qu'il retire de la liste des
     * choix, ou passer (en renvoyant la chaîne de caractères vide).
     * <p>
     * Les destinations qui ne sont pas écartées sont ajoutées à la liste des
     * destinations du joueur. Les destinations écartées sont renvoyées par la
     * fonction.
     *
     * @param destinationsPossibles liste de destinations proposées parmi lesquelles
     *                              le joueur peut choisir d'en écarter certaines
     * @param n                     nombre minimum de destinations que le joueur
     *                              doit garder
     * @return liste des destinations qui n'ont pas été gardées par le joueur
     */
    public List<Destination> choisirDestinations(List<Destination> destinationsPossibles, int n) {
        ArrayList<String> choix=new ArrayList<>();
        ArrayList<Destination> destinationsDefausse=new ArrayList<>();
        for(int i=0; i<destinationsPossibles.size(); i++){
            choix.add(destinationsPossibles.get(i).getNom());
        }
        boolean choixEnCours=true;
        while(choixEnCours && choix.size()>n){
            String choixCarteDefausse= choisir("choisissez une carte a retirer",choix,choix,true);
            if(choixCarteDefausse.equals("")){
                choixEnCours=false;
            }
            else{
                for(int i=0;i<choix.size();i++){
                    if (choix.get(i).equals(choixCarteDefausse)){
                        destinationsDefausse.add(destinationsPossibles.get(i));
                        choix.remove(i);
                        destinationsPossibles.remove(i);
                    }
                }
            }
        }
        destinations.addAll(destinationsPossibles);
        return destinationsDefausse;
    }

    public void augmenteScore(int bonus){
        score+=bonus;
    }


    public void retirerWagon(){
        nbWagons--;
    }

    public ArrayList<String> nomRoutes(){
        ArrayList<String> listRoute=new ArrayList<>();
        for (Route r: jeu.getRoutes()){
            if(r.valide(this)){
                listRoute.add(r.getNom());
            }
        }
        return listRoute;
    }

    private ArrayList<String> nomVilles(){
        ArrayList<String> listVille=new ArrayList<>();
        for (Ville v: jeu.getVilles()){
            if(v.getProprietaire()==null){
                listVille.add(v.getNom());
            }
        }
        return listVille;
    }

    private ArrayList<String> couleursVisibles(){
        ArrayList<String> couleurs=new ArrayList<>();
        for(CouleurWagon c : CouleurWagon.getCouleursSimples()){
            if(jeu.getCartesWagonVisibles().contains(c)){
                couleurs.add(c.name());
            }
        }
        if(jeu.getCartesWagonVisibles().contains(CouleurWagon.LOCOMOTIVE)){
            couleurs.add(CouleurWagon.LOCOMOTIVE.name());
        }
        return couleurs;
    }

    public Ville getVilleParNom(String nom) {
        for (Ville ville : jeu.getVilles()) {
            if (ville.getNom().equals(nom)) {
                return ville;
            }
        }
        return null;
    }

    public Route getRouteParNom(String nom) {
        for (Route route : jeu.getRoutes()) {
            if (route.getNom().equals(nom)) {
                return route;
            }
        }
        return null;
    }

    /**
     * Exécute un tour de jeu du joueur.
     * <p>
     * Cette méthode attend que le joueur choisisse une des options suivantes :
     * - le nom d'une carte wagon face visible à prendre ;
     * - le nom "GRIS" pour piocher une carte wagon face cachée s'il reste des
     * cartes à piocher dans la pile de pioche ou dans la pile de défausse ;
     * - la chaîne "destinations" pour piocher des cartes destination ;
     * - le nom d'une ville sur laquelle il peut construire une gare (ville non
     * prise par un autre joueur, le joueur a encore des gares en réserve et assez
     * de cartes wagon pour construire la gare) ;
     * - le nom d'une route que le joueur peut capturer (pas déjà capturée, assez de
     * wagons et assez de cartes wagon) ;
     * - la chaîne de caractères vide pour passer son tour
     * <p>
     * Lorsqu'un choix valide est reçu, l'action est exécutée (il est possible que
     * l'action nécessite d'autres choix de la part de l'utilisateur, comme "choisir les cartes wagon à défausser pour capturer une route" ou
     * "construire une gare", "choisir les destinations à défausser", etc.)
     */

    public void jouerTour() {
        ArrayList<String> choix=new ArrayList<>();
        ArrayList<String> villes=nomVilles();
        ArrayList<String> routes=nomRoutes();
        ArrayList<String> couleurs=couleursVisibles();
        if(!jeu.getPileDestinations().isEmpty()){
            choix.add("destinations");
        }
        choix.add("GRIS");
        choix.addAll(routes);
        if(nbGares>0){
            choix.addAll(villes);
        }
        choix.addAll(couleurs);
        String c=choisir("Que voulez vous faire ?",choix,new ArrayList<String>(),true);
        if(c.equals("destinations")){
            ArrayList<Destination> destinationsPossibles=new ArrayList<>();
            for(int i=0;i<3;i++){
                destinationsPossibles.add(jeu.piocherDestination());
            }
            List<Destination> destinationsDefausse=choisirDestinations(destinationsPossibles,1);
            jeu.getPileDestinations().addAll(destinationsDefausse);
        }
        else if(c.equals("GRIS")){
            cartesWagon.add(jeu.piocherCarteWagon());
            ArrayList<String> choix2=new ArrayList<>();
            choix2.add("GRIS");
            couleurs.remove("LOCOMOTIVE");
            choix2.addAll(couleurs);
            String c2=choisir("Choisissez une seconde carte",choix2,new ArrayList<String>(),true);
            if(c2.equals("GRIS")) {
                cartesWagon.add(jeu.piocherCarteWagon());
            }
            else if(!c2.equals("")){
                jeu.retirerCarteWagonVisible(CouleurWagon.valueOf(c2));
            }
        }
        else if(couleurs.contains(c)){
            jeu.retirerCarteWagonVisible(CouleurWagon.valueOf(c));
            cartesWagon.add(CouleurWagon.valueOf(c));
            couleurs=couleursVisibles();
            if(!c.equals(CouleurWagon.LOCOMOTIVE.name())){
                ArrayList<String> choix2=new ArrayList<>();
                choix2.add("GRIS");
                couleurs.remove(CouleurWagon.LOCOMOTIVE.name());
                choix2.addAll(couleurs);
                String c2=choisir("Choisissez une seconde carte",choix2,new ArrayList<String>(),true);
                if(c2.equals("GRIS")) {
                    cartesWagon.add(jeu.piocherCarteWagon());
                }
                else if(!c2.equals("")){
                    jeu.retirerCarteWagonVisible(CouleurWagon.valueOf(c2));
                }
            }
        }
        else if(nbGares>0 && villes.contains(c)){
            ArrayList<String> couleursDeck = new ArrayList<>();
            ArrayList<CouleurWagon> couleurSimple = CouleurWagon.getCouleursSimples();
            boolean peutConstruire=true;
            for (CouleurWagon couleur : couleurSimple) {
                if (Collections.frequency(cartesWagon, c) >= 3 - nbGares + 1 || cartesWagon.contains(couleur) && (Collections.frequency(cartesWagon, couleur) + Collections.frequency(cartesWagon, CouleurWagon.LOCOMOTIVE) >= 3 - nbGares + 1)) {
                    couleursDeck.add(couleur.name());
                }
            }
            if(!couleursDeck.isEmpty() && getVilleParNom(c).getProprietaire()==null) {
                for (int i = 0; i < 3 - nbGares + 1; i++) {
                    if (cartesWagon.contains(CouleurWagon.LOCOMOTIVE)) {
                        couleursDeck.add(CouleurWagon.LOCOMOTIVE.name());
                    }
                    String couleurGare = choisir("Choisissez une couleur de carte que vous voulez utiliser pour la gare", couleursDeck, couleursDeck, false);
                    jeu.defausserCarteWagon(CouleurWagon.valueOf(couleurGare));
                    cartesWagon.remove(CouleurWagon.valueOf(couleurGare));
                    couleursDeck.clear();
                    if (cartesWagon.contains(CouleurWagon.valueOf(couleurGare))) {
                        couleursDeck.add(couleurGare);
                    }
                }
            }
            else{peutConstruire=false;}
            if(peutConstruire){
                getVilleParNom(c).setProprietaire(this);
                nbGares--;
                score -= 4;
            }
        }
        else if(routes.contains(c)){
            int i=0;
            boolean bon=true;
            Route route=getRouteParNom(c);
            score+=route.comptePoints();
            while(i<route.getLongueur() && bon){
                if(route.getCouleur()==CouleurWagon.GRIS){
                    ArrayList<String> couleursPossede=route.possedeCouleur(this);
                    if(Collections.frequency(cartesWagon,CouleurWagon.LOCOMOTIVE)>0){
                        couleursPossede.add(CouleurWagon.LOCOMOTIVE.name());
                    }
                    String choixCouleurRoute=choisir("Choississez une couleur",couleursPossede,couleursPossede,false);
                    if(!route.capturer(this,CouleurWagon.valueOf(choixCouleurRoute))){
                        bon=false;
                    }
                }
                else{
                    ArrayList<String> couleurJouable=new ArrayList<>();
                    if(cartesWagon.contains(route.getCouleur())){
                        couleurJouable.add(route.getCouleur().name());
                    }
                    if(cartesWagon.contains(CouleurWagon.LOCOMOTIVE)){
                        couleurJouable.add(CouleurWagon.LOCOMOTIVE.name());
                    }
                    String choixCouleurRoute=choisir("Choississez une couleur ",couleurJouable,couleurJouable,false);
                    if(!route.capturer(this,CouleurWagon.valueOf(choixCouleurRoute))){
                        bon=false;
                    }
                }
                i++;
            }
            if(bon){
                route.setProprietaire(this);
            }
        }
    }
}
